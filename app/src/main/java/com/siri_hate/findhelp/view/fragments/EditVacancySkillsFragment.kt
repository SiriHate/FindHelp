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
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancySkillsListApdater
import com.siri_hate.findhelp.viewmodel.fragments.EditVacancySkillsViewModel

class EditVacancySkillsFragment : Fragment() {

    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var createButton: Button
    private lateinit var controller: NavController
    private val viewModel by lazy { ViewModelProvider(this)[EditVacancySkillsViewModel::class.java] }

    companion object {
        const val DOCUMENT_ID_KEY = "document_id"
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
        skillsRecyclerView = view.findViewById(R.id.edit_vacancy_skills_fragment_list)
        createButton = view.findViewById(R.id.edit_vacancy_skills_fragment_create_button)

        controller = findNavController()
        skillsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val documentId = arguments?.getString(DOCUMENT_ID_KEY) ?: ""

        val skillsAdapter = CreateAndEditVacancySkillsListApdater(requireContext(), viewModel.db, documentId, emptyList()) {
            viewModel.onCheckboxSelected()
        }
        skillsRecyclerView.adapter = skillsAdapter

        // Наблюдаем за изменениями списка навыков и обновляем RecyclerView
        viewModel.skillsList.observe(viewLifecycleOwner) { skillsList ->
            skillsAdapter.updateSkillsList(skillsList)
        }

        // Загрузка списка навыков из Firestore
        viewModel.loadSkillsList(documentId)

        // Настройка кнопки создания вакансии
        createButton.setOnClickListener {
            if (viewModel.onCreateButtonClicked()) {
                val bundle = Bundle().apply {
                    putString(DOCUMENT_ID_KEY, documentId)
                }
                controller.navigate(R.id.action_editVacancySecondFragment_to_vacancyCardFragment, bundle)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Необходимо выбрать хотя бы один навык",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}



