package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.CreateVacancySkillsListApdater


class CreateVacancySecondFragment : Fragment() {
    private lateinit var newVacancySecondFragmentList: ListView
    private lateinit var adapter: CreateVacancySkillsListApdater
    private lateinit var newVacancySecondFragmentCreateButton: Button
    private var isAtLeastOneCheckboxSelected = false
    private lateinit var controller: NavController
    private lateinit var documentId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_vacancy_skills, container, false)
        newVacancySecondFragmentList = view.findViewById(R.id.new_vacancy_second_fragment_list)
        newVacancySecondFragmentCreateButton =
            view.findViewById(R.id.new_vacancy_second_fragment_create_button)
        controller = findNavController()
        documentId = arguments?.getString("document_id") ?: ""
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAdapter()

        val db = FirebaseFirestore.getInstance()
        fetchSkillsListFromFirestore(db, documentId)

        newVacancySecondFragmentCreateButton.setOnClickListener {
            if (!isAtLeastOneCheckboxSelected) {
                showNoSkillsSelectedToast()
            } else {
                finishCurrentActivity()
            }
        }
    }

    private fun setUpAdapter() {
        val db = FirebaseFirestore.getInstance()

        adapter = CreateVacancySkillsListApdater(requireContext(), db, documentId, emptyList()) {
            isAtLeastOneCheckboxSelected = true
        }
        newVacancySecondFragmentList.adapter = adapter
    }

    private fun fetchSkillsListFromFirestore(db: FirebaseFirestore, documentId: String) {
        db.collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get("vacancy_skills_list") as? Map<String, Boolean>
                    ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                adapter.updateSkillsList(skillsList)
            }
    }

    private fun showNoSkillsSelectedToast() {
        Toast.makeText(
            requireContext(),
            "Необходимо выбрать хотя бы один навык",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun finishCurrentActivity() {
        controller.navigate(R.id.action_createVacancySecondFragment_to_organizerPageFragment)
    }
}