package com.siri_hate.findhelp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.OrganizerPageActivity
import com.siri_hate.findhelp.view.adapters.CreateVacancySkillsListApdater


class NewVacancySecondFragment : Fragment() {

    private lateinit var newVacancySecondFragmentList: ListView
    private lateinit var adapter: CreateVacancySkillsListApdater
    private lateinit var newVacancySecondFragmentCreateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.new_vacancy_second_fragment, container, false)
        newVacancySecondFragmentList = view.findViewById(R.id.new_vacancy_second_fragment_list)
        newVacancySecondFragmentCreateButton =
            view.findViewById(R.id.new_vacancy_second_fragment_create_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val vacancyID = arguments?.getString("vacancy_id") ?: ""

        adapter = CreateVacancySkillsListApdater(requireContext(), db, vacancyID, emptyList())
        newVacancySecondFragmentList.adapter = adapter

        db.collection("vacancies_list").document(vacancyID).get()
            .addOnSuccessListener { documentSnapshot ->
                val skillsMap = documentSnapshot.get("vacancy_skills_list") as? Map<String, Boolean> ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                adapter.updateSkillsList(skillsList)
            }

        newVacancySecondFragmentCreateButton.setOnClickListener {
            requireActivity().finish()
            startActivity(Intent(requireActivity(), OrganizerPageActivity::class.java))
        }
    }
}