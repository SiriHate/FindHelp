package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentCreateVacancyBinding
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter
import com.siri_hate.findhelp.viewmodel.factory.CreateVacancyViewModelFactory
import com.siri_hate.findhelp.viewmodel.fragments.CreateVacancyViewModel

class CreateVacancyFragment : Fragment() {

    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var viewModel: CreateVacancyViewModel
    private lateinit var binding: FragmentCreateVacancyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateVacancyBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            this,
            CreateVacancyViewModelFactory(
                FirebaseAuthModel(),
                FirebaseFirestoreModel()
            )
        )[CreateVacancyViewModel::class.java]

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
                adapter = CreateAndEditVacancyAdapter(requireContext(), skillsList)
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
            Toast.makeText(requireContext(), "Вакансия успешно добавлена!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
        }, {})
    }

    private fun getSelectedSkillsMap(): Map<String, Boolean> {
        val skillsMap = mutableMapOf<String, Boolean>()
        adapter.getSkills().forEach { skill ->
            skillsMap[skill.name] = skill.isChecked
        }
        return skillsMap
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        if (binding.createVacancyFragmentNameInput.text.isBlank()) {
            binding.createVacancyFragmentNameInput.error = "Введите имя вакансии!"
            isValid = false
        }
        if (binding.createVacancyFragmentCityInput.text.isBlank()) {
            binding.createVacancyFragmentCityInput.error = "Введите город вакансии!"
            isValid = false
        }
        if (binding.createVacancyFragmentDescriptionInput.text.isBlank()) {
            binding.createVacancyFragmentDescriptionInput.error = "Введите описание вакансии!"
            isValid = false
        }
        return isValid
    }

    private fun validateSkills(): Boolean {
        val selectedSkills = adapter.getSkills().filter { it.isChecked }
        if (selectedSkills.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один навык!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
