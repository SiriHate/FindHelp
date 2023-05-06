package com.siri_hate.findhelp.ui.viewmodels.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentCreateVacancyBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.adapters.CreateVacancyAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.CreateVacancyViewModelFactory

class CreateVacancyFragment : Fragment() {

    private lateinit var adapter: CreateVacancyAdapter
    private lateinit var binding: FragmentCreateVacancyBinding

    private val viewModel: CreateVacancyViewModel by viewModels {
        CreateVacancyViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateVacancyBinding.inflate(inflater, container, false)

        setupListeners()
        observeSkillsList()

        return binding.root
    }

    private fun observeSkillsList() {
        viewModel.skillsList.observe(viewLifecycleOwner) { skillsList ->
            if (skillsList.isEmpty()) {
                binding.createVacancyFragmentSkillsList.visibility = View.GONE
                binding.createVacancyEmptyListMessage.visibility = View.VISIBLE
            } else {
                adapter = CreateVacancyAdapter(requireContext(), skillsList)
                binding.createVacancyFragmentSkillsList.adapter = adapter
                binding.createVacancyEmptyListMessage.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.createVacancyFragmentCreateButton.setOnClickListener {
            if (validateInputs() && validateSkills()) {
                createVacancy()
            }
        }
    }

    private fun createVacancy() {
        val name = binding.createVacancyFragmentNameInput.text.toString()
        val city = binding.createVacancyFragmentCityInput.text.toString()
        val description = binding.createVacancyFragmentDescriptionInput.text.toString()
        val selectedSkillsMap = getSelectedSkillsMap()

        viewModel.createVacancy(name, city, description, selectedSkillsMap, {
            Toast.makeText(
                requireContext(),
                getString(R.string.vacancy_added_successfully_msg),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
        }, {})
    }

    private fun getSelectedSkillsMap(): Map<String, Boolean> {
        val skillsList = adapter.getSkills()
        return viewModel.convertSkillsList(skillsList)
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        if (binding.createVacancyFragmentNameInput.text.isBlank()) {
            binding.createVacancyFragmentNameInput.error =
                getString(R.string.neeed_to_enter_vacancy_name_msg)
            isValid = false
        }
        if (binding.createVacancyFragmentCityInput.text.isBlank()) {
            binding.createVacancyFragmentCityInput.error =
                getString(R.string.neeed_to_enter_vacancy_city_msg)
            isValid = false
        }
        if (binding.createVacancyFragmentDescriptionInput.text.isBlank()) {
            binding.createVacancyFragmentDescriptionInput.error =
                getString(R.string.neeed_to_enter_vacancy_description_msg)
            isValid = false
        }
        return isValid
    }

    private fun validateSkills(): Boolean {
        val selectedSkills = adapter.getSkills().filter { it.isChecked }
        if (selectedSkills.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.neeed_to_select_vacancy_skill_msg),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }
}
