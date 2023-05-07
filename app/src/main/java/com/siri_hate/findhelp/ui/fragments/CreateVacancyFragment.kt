package com.siri_hate.findhelp.ui.fragments

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
import com.siri_hate.findhelp.ui.viewmodels.fragments.CreateVacancyViewModel

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
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
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

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
        }

        viewModel.destPage.observe(viewLifecycleOwner) { destinationPage ->
            viewModel.navigateToPage(findNavController(), destinationPage)
        }

        viewModel.vacancyNameInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.createVacancyFragmentNameInput.error = getString(R.string.neeed_to_enter_vacancy_name_msg)
            }
        }

        viewModel.vacancyCityInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.createVacancyFragmentCityInput.error = getString(R.string.neeed_to_enter_vacancy_city_msg)
            }
        }

        viewModel.vacancyDescriptionInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.createVacancyFragmentDescriptionInput.error = getString(R.string.neeed_to_enter_vacancy_description_msg)
            }
        }

    }

    private fun setupListeners() {
        binding.createVacancyFragmentCreateButton.setOnClickListener {
            createVacancy()
        }
    }

    private fun createVacancy() {
        val name = binding.createVacancyFragmentNameInput.text.toString()
        val city = binding.createVacancyFragmentCityInput.text.toString()
        val description = binding.createVacancyFragmentDescriptionInput.text.toString()
        val selectedSkillsMap = adapter.getSkills().associateBy({ it.name }, { it.isChecked })

        viewModel.handleCreateVacancy(name, city, description, selectedSkillsMap)
    }

}
