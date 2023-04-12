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

    // объявление переменных
    private lateinit var editVacancySecondFragmentList: RecyclerView
    private lateinit var adapter: CreateAndEditVacancySkillsListApdater
    private lateinit var editVacancySecondFragmentCreateButton: Button
    private lateinit var controller: NavController
    private var isAtLeastOneCheckboxSelected = false
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // создание view
        val view = inflater.inflate(R.layout.fragment_edit_vacancy_skills, container, false)
        editVacancySecondFragmentList = view.findViewById(R.id.edit_vacancy_second_fragment_list)
        editVacancySecondFragmentCreateButton =
            view.findViewById(R.id.edit_vacancy_second_fragment_create_button)
        controller = findNavController()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // получение id документа
        val documentId = arguments?.getString("document_id") ?: ""

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        editVacancySecondFragmentList.layoutManager = layoutManager
        // создание адаптера
        adapter = CreateAndEditVacancySkillsListApdater(requireContext(), db, documentId, emptyList()) {
            isAtLeastOneCheckboxSelected = true
        }
        editVacancySecondFragmentList.adapter = adapter

        // загрузка списка навыков в адаптер из Firestore
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get("vacancy_skills_list") as? Map<String, Boolean>
                    ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                adapter.updateSkillsList(skillsList)
            }

        // установка обработчика нажатия на кнопку
        editVacancySecondFragmentCreateButton.setOnClickListener {
            // проверка, что хотя бы один чекбокс выбран
            if (!isAtLeastOneCheckboxSelected) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо выбрать хотя бы один навык",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                val bundle = Bundle()
                bundle.putString("document_id", documentId)
                controller.navigate(R.id.action_editVacancySecondFragment_to_vacancyCardFragment, bundle)
            }
        }

    }
}
