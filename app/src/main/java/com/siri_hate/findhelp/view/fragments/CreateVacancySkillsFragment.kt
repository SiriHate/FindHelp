package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancySkillsListApdater
import com.siri_hate.findhelp.viewmodel.fragments.CreateVacancySkillsViewModel


class CreateVacancySkillsFragment : Fragment() {

    private lateinit var newVacancySecondFragmentList: RecyclerView
    private lateinit var adapter: CreateAndEditVacancySkillsListApdater
    private lateinit var newVacancySecondFragmentCreateButton: Button
    private lateinit var controller: NavController

    private var isAtLeastOneCheckboxSelected = false
    private lateinit var documentId: String

    private lateinit var viewModel: CreateVacancySkillsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_vacancy_skills, container, false)
        newVacancySecondFragmentList = view.findViewById(R.id.create_vacancy_skills_fragment_list)
        newVacancySecondFragmentCreateButton = view.findViewById(R.id.create_vacancy_skills_fragment_create_button)
        controller = findNavController()
        documentId = arguments?.getString("document_id") ?: ""
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CreateVacancySkillsViewModel::class.java]
        setUpAdapter()

        val db = FirebaseFirestore.getInstance()
        viewModel.fetchSkillsListFromFirestore(db, documentId)

        // Handle button click event
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

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        newVacancySecondFragmentList.layoutManager = layoutManager
        adapter = CreateAndEditVacancySkillsListApdater(requireContext(), db, documentId, emptyList()) {
            isAtLeastOneCheckboxSelected = true
        }
        viewModel.skillsList.observe(viewLifecycleOwner) { skillsList ->
            adapter.updateSkillsList(skillsList)
        }
        newVacancySecondFragmentList.adapter = adapter
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