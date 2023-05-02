package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentEditVacancyBinding
import com.siri_hate.findhelp.model.models.Skill
import com.siri_hate.findhelp.model.models.Vacancy
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter

class EditVacancyFragment : Fragment() {

    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var documentId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentEditVacancyBinding

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
    ): View {
        binding = FragmentEditVacancyBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding.editVacancyMainLayout.visibility = View.GONE
        binding.editVacancyPageLoadingProgressBar.visibility = View.VISIBLE

        getVacancyData()
        getSkillsListData()

        binding.editVacancyInfoFragmentEditButton.setOnClickListener {
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

        return binding.root
    }

    private fun areInputsValid(): Boolean {
        var isValid = true

        if (binding.editVacancyFragmentNameInput.text.isNullOrEmpty()) {
            binding.editVacancyFragmentNameInput.error = "Введите название вакансии!"
            isValid = false
        }

        if (binding.editVacancyFragmentCityInput.text.isNullOrEmpty()) {
            binding.editVacancyFragmentCityInput.error = "Введите город вакансии!"
            isValid = false
        }

        if (binding.editVacancyFragmentDescriptionInput.text.isNullOrEmpty()) {
            binding.editVacancyFragmentDescriptionInput.error = "Введите описание вакансии!"
            isValid = false
        }

        return isValid
    }
    private fun getVacancyData() {
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                binding.editVacancyFragmentNameInput.setText(documentSnapshot.getString("vacancy_name"))
                binding.editVacancyFragmentCityInput.setText(documentSnapshot.getString("vacancy_city"))
                binding.editVacancyFragmentDescriptionInput.setText(documentSnapshot.getString("vacancy_description"))
            }
    }

    private fun getSkillsListData() {
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get("vacancy_skills_list") as? Map<String, Boolean>
                val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()

                if (skillsList.isEmpty()) {
                    binding.editVacancyFragmentSkillsList.visibility = View.GONE
                    binding.editVacancyEmptyListMessage.visibility = View.VISIBLE
                } else {
                    adapter = CreateAndEditVacancyAdapter(requireContext(), skillsList)
                    binding.editVacancyFragmentSkillsList.adapter = adapter
                    binding.editVacancyEmptyListMessage.visibility = View.GONE

                    binding.editVacancyMainLayout.visibility = View.VISIBLE
                    binding.editVacancyPageLoadingProgressBar.visibility = View.GONE
                }
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
        val vacancy = Vacancy(
            vacancy_name = binding.editVacancyFragmentNameInput.text.toString(),
            vacancy_city = binding.editVacancyFragmentCityInput.text.toString(),
            vacancy_description = binding.editVacancyFragmentDescriptionInput.text.toString(),
            vacancy_skills_list = getSelectedSkillsMap()
        )

        db.collection("vacancies_list").document(documentId)
            .update(vacancy.toMap())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Вакансия успешно изменена!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Ошибка изменения вакансии: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


