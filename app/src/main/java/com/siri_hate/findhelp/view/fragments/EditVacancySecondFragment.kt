package com.siri_hate.findhelp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.ModeratorPageActivity
import com.siri_hate.findhelp.view.activities.OrganizerPageActivity
import com.siri_hate.findhelp.view.adapters.CreateVacancySkillsListApdater

class EditVacancySecondFragment : Fragment() {

    // объявление переменных
    private lateinit var editVacancySecondFragmentList: ListView
    private lateinit var adapter: CreateVacancySkillsListApdater
    private lateinit var editVacancySecondFragmentCreateButton: Button
    private var isAtLeastOneCheckboxSelected = false
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserEmail = auth.currentUser?.email ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // создание view
        val view = inflater.inflate(R.layout.edit_vacancy_second_fragment, container, false)
        editVacancySecondFragmentList = view.findViewById(R.id.edit_vacancy_second_fragment_list)
        editVacancySecondFragmentCreateButton =
            view.findViewById(R.id.edit_vacancy_second_fragment_create_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // получение id документа
        val documentId = requireActivity().intent.getStringExtra("document_id") ?: ""

        // создание адаптера
        adapter = CreateVacancySkillsListApdater(requireContext(), db, documentId, emptyList()) {
            isAtLeastOneCheckboxSelected = true
        }
        editVacancySecondFragmentList.adapter = adapter

        // загрузка списка навыков в адаптер из Firestore
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
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
            }

            // определение типа пользователя и переход на соответствующую страницу
            db.collection("user_rights").document(currentUserEmail).get()
                .addOnSuccessListener { documentSnapshot ->
                    when (documentSnapshot.get("userType") as? String ?: "") {
                        "moderator" -> {
                            requireActivity().finish()
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    ModeratorPageActivity::class.java
                                )
                            )
                        }
                        "organizer" -> {
                            requireActivity().finish()
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    OrganizerPageActivity::class.java
                                )
                            )
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Не удалось определить тип пользователя",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось определить тип пользователя",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
