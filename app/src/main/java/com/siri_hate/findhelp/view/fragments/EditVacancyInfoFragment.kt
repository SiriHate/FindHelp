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
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.Skill
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter

class EditVacancyInfoFragment : Fragment() {

    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var documentId: String
    private lateinit var nameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var db: FirebaseFirestore

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(DOCUMENT_ID_KEY, "") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_vacancy_info, container, false)

        db = FirebaseFirestore.getInstance()

        initViews(view)
        getVacancyData()
        getSkillsListData()

        continueButton.setOnClickListener {
            if (areInputsValid() && validateSkillsList()) {
                updateVacancyData()
                val bundle = Bundle()
                bundle.putString(DOCUMENT_ID_KEY, documentId)
                findNavController().navigate(
                    R.id.action_editVacancyMainFragment_to_vacancyCardFragment,
                    bundle
                )
            }
        }

        return view
    }

    private fun areInputsValid(): Boolean {
        var isValid = true

        if (nameEditText.text.isNullOrEmpty()) {
            nameEditText.error = "Введите название вакансии!"
            isValid = false
        }

        if (cityEditText.text.isNullOrEmpty()) {
            cityEditText.error = "Введите город вакансии!"
            isValid = false
        }

        if (descriptionEditText.text.isNullOrEmpty()) {
            descriptionEditText.error = "Введите описание вакансии!"
            isValid = false
        }

        return isValid
    }

    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.edit_vacancy_info_fragment_name_input)
        cityEditText = view.findViewById(R.id.edit_vacancy_info_fragment_city_input)
        descriptionEditText = view.findViewById(R.id.edit_vacancy_info_fragment_description_input)
        continueButton = view.findViewById(R.id.edit_vacancy_info_fragment_continue_button)
        recyclerView = view.findViewById(R.id.edit_vacancy_fragment_skills_list)
    }

    private fun getVacancyData() {
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                nameEditText.setText(documentSnapshot.getString("vacancy_name"))
                cityEditText.setText(documentSnapshot.getString("vacancy_city"))
                descriptionEditText.setText(documentSnapshot.getString("vacancy_description"))
            }
    }

    private fun getSkillsListData() {
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get("vacancy_skills_list") as? Map<String, Boolean>
                val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()
                adapter = CreateAndEditVacancyAdapter(requireContext(), skillsList)
                recyclerView.adapter = adapter
            }
    }

    private fun validateSkillsList(): Boolean {
        val selectedSkills = adapter.getSkills().filter { it.isChecked }
        if (selectedSkills.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один навык!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getSelectedSkillsMap(): Map<String, Boolean> {
        val skillsMap = mutableMapOf<String, Boolean>()
        adapter.getSkills().forEach { skill ->
            skillsMap[skill.name] = skill.isChecked
        }
        return skillsMap
    }

    private fun updateVacancyData() {
        val vacancy = hashMapOf(
            "vacancy_name" to nameEditText.text.toString(),
            "vacancy_city" to cityEditText.text.toString(),
            "vacancy_description" to descriptionEditText.text.toString(),
            "vacancy_skills_list" to getSelectedSkillsMap()
        )

        db.collection("vacancies_list").document(documentId)
            .update(vacancy)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Вакансия успешно изменена!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Ошибка изменения вакансии: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


