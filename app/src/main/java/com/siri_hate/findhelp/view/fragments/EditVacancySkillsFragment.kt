package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancySkillsListApdater

class EditVacancySkillsFragment : Fragment() {

    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var createButton: Button
    private lateinit var controller: NavController
    private var isAtLeastOneCheckboxSelected = false
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val DOCUMENT_ID_KEY = "document_id"
        const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_vacancy_skills, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView
        skillsRecyclerView = view.findViewById(R.id.edit_vacancy_second_fragment_list)
        createButton = view.findViewById(R.id.edit_vacancy_second_fragment_create_button)

        controller = findNavController()
        skillsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val documentId = arguments?.getString(DOCUMENT_ID_KEY) ?: ""

        val skillsAdapter = CreateAndEditVacancySkillsListApdater(requireContext(), db, documentId, emptyList()) {
            isAtLeastOneCheckboxSelected = true
        }
        skillsRecyclerView.adapter = skillsAdapter

        // Загрузка списка навыков из Firestore
        db.collection(VACANCIES_LIST_COLLECTION).document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get(VACANCY_SKILLS_LIST_FIELD) as? Map<String, Boolean>
                    ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                skillsAdapter.updateSkillsList(skillsList)
            }

        // Настройка кнопки создания вакансии
        createButton.setOnClickListener {
            if (!isAtLeastOneCheckboxSelected) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо выбрать хотя бы один навык",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val bundle = Bundle().apply {
                    putString(DOCUMENT_ID_KEY, documentId)
                }
                controller.navigate(R.id.action_editVacancySecondFragment_to_vacancyCardFragment, bundle)
            }
        }
    }
}



