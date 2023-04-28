package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.Skill
import com.siri_hate.findhelp.model.Vacancy
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter


class CreateVacancyFragment : Fragment() {

    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var recyclerViewList: RecyclerView
    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var continueButton: Button

    private lateinit var db: FirebaseFirestore

    companion object {
        private const val SKILLS_COLLECTION = "skills"
        private const val ORGANIZATION_INFO_COLLECTION = "organization_info"
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_vacancy, container, false)

        db = FirebaseFirestore.getInstance()

        initViews(view)
        setupLisListeners()
        getInitData()

        return view
    }

    private fun initViews(view: View) {
        recyclerViewList = view.findViewById(R.id.create_vacancy_skills_list)
        nameInput = view.findViewById(R.id.create_vacancy_info_fragment_name_input)
        cityInput = view.findViewById(R.id.create_vacancy_info_fragment_city_input)
        descriptionInput = view.findViewById(R.id.create_vacancy_info_fragment_description_input)
        continueButton = view.findViewById(R.id.create_vacancy_info_fragment_continue_button)
    }

    private fun setupLisListeners() {
        continueButton.setOnClickListener {
            if (validateInputs() && validateSkills()) {
                createVacancy()
            }
        }
    }

    private fun getInitData() {
        db.collection("init_data").document("base_skills_init").get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get(SKILLS_COLLECTION) as? Map<String, Boolean>
                val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()
                adapter = CreateAndEditVacancyAdapter(requireContext(), skillsList)
                recyclerViewList.adapter = adapter
            }
    }

    private fun createVacancy() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        db.collection(ORGANIZATION_INFO_COLLECTION).document(currentUserEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val contactPerson = documentSnapshot.getString("contact_person") ?: ""
                val orgName = documentSnapshot.getString("organization_name") ?: ""
                val orgPhone = documentSnapshot.getString("organization_phone") ?: ""

                val vacancy = Vacancy(
                    creator_email = currentUserEmail,
                    contact_person = contactPerson,
                    organization_name = orgName,
                    organization_phone = orgPhone,
                    vacancy_name = nameInput.text.toString(),
                    vacancy_city = cityInput.text.toString(),
                    vacancy_description = descriptionInput.text.toString(),
                    vacancy_skills_list = getSelectedSkillsMap()
                )

                db.collection(VACANCIES_LIST_COLLECTION)
                    .add(vacancy)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
                    }
                    .addOnFailureListener {}
            }
            .addOnFailureListener {}
    }

    private fun getSelectedSkillsMap(): Map<String, Boolean> {
        val skillsMap = mutableMapOf<String, Boolean>()
        adapter.getSkills().forEach { skill ->
            skillsMap[skill.name] = skill.isChecked
        }
        return skillsMap
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        if (nameInput.text.isBlank()) {
            nameInput.error = "Введите имя вакансии!"
            isValid = false
        }
        if (cityInput.text.isBlank()) {
            cityInput.error = "Введите город вакансии!"
            isValid = false
        }
        if (descriptionInput.text.isBlank()) {
            descriptionInput.error = "Введите описание вакансии!"
            isValid = false
        }
        return isValid
    }

    private fun validateSkills(): Boolean {
        val selectedSkills = adapter.getSkills().filter { it.isChecked }
        if (selectedSkills.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один навык!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
