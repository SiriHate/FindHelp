package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentCreateVacancyBinding
import com.siri_hate.findhelp.model.models.Skill
import com.siri_hate.findhelp.model.models.Vacancy
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter


class CreateVacancyFragment : Fragment() {

    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentCreateVacancyBinding

    companion object {
        private const val SKILLS_COLLECTION = "skills"
        private const val ORGANIZATION_INFO_COLLECTION = "organization_info"
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateVacancyBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()

        setupLisListeners()
        getInitData()

        return binding.root
    }

    private fun setupLisListeners() {
        binding.createVacancyFragmentCreateButton.setOnClickListener {
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

                if (skillsList.isEmpty()) {
                    binding.createVacancyFragmentSkillsList.visibility = View.GONE
                    binding.createVacancyEmptyListMessage.visibility = View.VISIBLE
                } else {
                    adapter = CreateAndEditVacancyAdapter(requireContext(), skillsList)
                    binding.createVacancyFragmentSkillsList.adapter = adapter
                    binding.createVacancyEmptyListMessage.visibility = View.GONE
                }
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
                    vacancy_name = binding.createVacancyFragmentNameInput.text.toString(),
                    vacancy_city = binding.createVacancyFragmentCityInput.text.toString(),
                    vacancy_description = binding.createVacancyFragmentDescriptionInput.text.toString(),
                    vacancy_skills_list = getSelectedSkillsMap()
                )

                db.collection(VACANCIES_LIST_COLLECTION)
                    .add(vacancy)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Вакансия успешно добавлена!", Toast.LENGTH_SHORT).show()
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
        if (binding.createVacancyFragmentNameInput.text.isBlank()) {
            binding.createVacancyFragmentNameInput.error = "Введите имя вакансии!"
            isValid = false
        }
        if (binding.createVacancyFragmentCityInput.text.isBlank()) {
            binding.createVacancyFragmentCityInput.error = "Введите город вакансии!"
            isValid = false
        }
        if (binding.createVacancyFragmentDescriptionInput.text.isBlank()) {
            binding.createVacancyFragmentDescriptionInput.error = "Введите описание вакансии!"
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
