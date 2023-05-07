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
import com.siri_hate.findhelp.databinding.FragmentEditVacancyBinding
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.adapters.EditVacancyAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.EditVacancyViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.EditVacancyViewModel

class EditVacancyFragment : Fragment() {

    private lateinit var adapter: EditVacancyAdapter
    private lateinit var documentId: String
    private lateinit var binding: FragmentEditVacancyBinding

    private val viewModel: EditVacancyViewModel by viewModels {
        EditVacancyViewModelFactory(
            FirebaseFirestoreModel()
        )
    }

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            documentId = it.getString(DOCUMENT_ID_KEY, "") ?: ""
        }

        viewModel.loadData(documentId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditVacancyBinding.inflate(inflater, container, false)

        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupObservers() {

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.editVacancyMainLayout.visibility = View.GONE
                binding.editVacancyPageLoadingProgressBar.visibility = View.VISIBLE
            } else {
                binding.editVacancyPageLoadingProgressBar.visibility = View.GONE
                binding.editVacancyMainLayout.visibility = View.VISIBLE
            }
        }

        viewModel.vacancyName.observe(viewLifecycleOwner) { name ->
            binding.editVacancyFragmentNameInput.setText(name)
        }

        viewModel.vacancyCity.observe(viewLifecycleOwner) { city ->
            binding.editVacancyFragmentCityInput.setText(city)
        }

        viewModel.vacancyDescription.observe(viewLifecycleOwner) { description ->
            binding.editVacancyFragmentDescriptionInput.setText(description)
        }

        viewModel.skillsList.observe(viewLifecycleOwner) { skillsListData ->
            if (skillsListData.isEmpty()) {
                binding.editVacancyFragmentSkillsList.visibility = View.GONE
                binding.editVacancyEmptyListMessage.visibility = View.VISIBLE
            } else {
                adapter = EditVacancyAdapter(requireContext(), skillsListData)
                binding.editVacancyFragmentSkillsList.adapter = adapter
                binding.editVacancyEmptyListMessage.visibility = View.GONE
            }
        }

        viewModel.vacancyNameInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.editVacancyFragmentNameInput.error = getString(R.string.neeed_to_enter_vacancy_name_msg)
            }
        }

        viewModel.vacancyCityInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.editVacancyFragmentCityInput.error = getString(R.string.neeed_to_enter_vacancy_city_msg)
            }
        }

        viewModel.vacancyDescriptionInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.editVacancyFragmentDescriptionInput.error = getString(R.string.neeed_to_enter_vacancy_description_msg)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
        }

        viewModel.goBack.observe(viewLifecycleOwner) { status ->
            if (status) {
                val bundle = Bundle()
                bundle.putString(DOCUMENT_ID_KEY, documentId)
                findNavController().navigate(
                    R.id.action_editVacancyMainFragment_to_vacancyCardFragment,
                    bundle
                )
            }
        }

    }

    private fun setupListeners() {
        binding.editVacancyInfoFragmentEditButton.setOnClickListener {
            editVacancy()
        }
    }

    private fun editVacancy() {
        val name = binding.editVacancyFragmentNameInput.text.toString()
        val city = binding.editVacancyFragmentCityInput.text.toString()
        val description = binding.editVacancyFragmentDescriptionInput.text.toString()
        val selectedSkills = adapter.getSkills().associateBy({ it.name }, { it.isChecked })

        viewModel.handleUpdateVacancy(documentId, name, city, description, selectedSkills)
    }

}


