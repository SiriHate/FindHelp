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
import com.siri_hate.findhelp.databinding.FragmentEditVacancyBinding
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.view.adapters.CreateAndEditVacancyAdapter
import com.siri_hate.findhelp.viewmodel.factory.EditVacancyViewModelFactory
import com.siri_hate.findhelp.viewmodel.fragments.EditVacancyViewModel

class EditVacancyFragment : Fragment() {

    private lateinit var viewModel: EditVacancyViewModel
    private lateinit var adapter: CreateAndEditVacancyAdapter
    private lateinit var documentId: String
    private lateinit var binding: FragmentEditVacancyBinding

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(DOCUMENT_ID_KEY, "") ?: ""
        }

        val firestoreModel = FirebaseFirestoreModel()
        val viewModelFactory = EditVacancyViewModelFactory(firestoreModel)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[EditVacancyViewModel::class.java]
        viewModel.loadData(documentId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditVacancyBinding.inflate(inflater, container, false)

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
                adapter = CreateAndEditVacancyAdapter(requireContext(), skillsListData)
                binding.editVacancyFragmentSkillsList.adapter = adapter
                binding.editVacancyEmptyListMessage.visibility = View.GONE
            }
        }

        binding.editVacancyInfoFragmentEditButton.setOnClickListener {
            val name = binding.editVacancyFragmentNameInput.text.toString()
            val city = binding.editVacancyFragmentCityInput.text.toString()
            val description = binding.editVacancyFragmentDescriptionInput.text.toString()
            val selectedSkills = adapter.getSkills().associateBy({ it.name }, { it.isChecked })

            if (viewModel.validateInputs(name, city, description) && viewModel.validateSelectedSkills(adapter.getSkills())) {
                viewModel.updateVacancy(documentId, name, city, description, selectedSkills, {
                    Toast.makeText(requireContext(), "Вакансия успешно изменена!", Toast.LENGTH_SHORT).show()

                    val bundle = Bundle()
                    bundle.putString(DOCUMENT_ID_KEY, documentId)
                    findNavController().navigate(
                        R.id.action_editVacancyMainFragment_to_vacancyCardFragment,
                        bundle
                    )
                }, { errorMessage ->
                    Toast.makeText(requireContext(), "Ошибка изменения вакансии: $errorMessage", Toast.LENGTH_SHORT).show()
                })
            }
        }

        return binding.root
    }
}


