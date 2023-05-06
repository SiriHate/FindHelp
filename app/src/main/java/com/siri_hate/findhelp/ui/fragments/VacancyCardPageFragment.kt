package com.siri_hate.findhelp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentVacancyCardBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.adapters.VacancySkillsListAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.VacancyCardViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.VacancyCardViewModel

class VacancyCardPageFragment : Fragment() {

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    private lateinit var controller: NavController
    private lateinit var binding: FragmentVacancyCardBinding

    private val viewModel: VacancyCardViewModel by viewModels {
        VacancyCardViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVacancyCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = findNavController()

        binding.vacancyCardSkillsList.adapter = VacancySkillsListAdapter()

        arguments?.getString(DOCUMENT_ID_KEY)?.let { documentId ->
            viewModel.loadVacancyInfo(documentId)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.vacancyCardLoadingBar.visibility = View.VISIBLE
                binding.vacancyCard.visibility = View.GONE
            } else {
                binding.vacancyCardLoadingBar.visibility = View.GONE
                binding.vacancyCard.visibility = View.VISIBLE
            }
        }

        viewModel.isSkillsListEmpty.observe(viewLifecycleOwner) { isSkillsListEmpty ->
            if (isSkillsListEmpty) {
                binding.vacancyCardSkillsList.visibility = View.GONE
                binding.vacancyCardEmptyListMessage.visibility = View.VISIBLE
            } else {
                binding.vacancyCardSkillsList.visibility = View.VISIBLE
                binding.vacancyCardEmptyListMessage.visibility = View.GONE
            }
        }

        viewModel.skillsList.observe(viewLifecycleOwner) { skillsList ->
            (binding.vacancyCardSkillsList.adapter as VacancySkillsListAdapter).submitList(skillsList)
        }

        viewModel.vacancyName.observe(viewLifecycleOwner) { vacancyName ->
            binding.vacancyCardName.text = vacancyName
        }

        viewModel.companyName.observe(viewLifecycleOwner) { companyName ->
            binding.vacancyCardCompanyName.text = companyName
        }

        viewModel.contactPerson.observe(viewLifecycleOwner) { contactPerson ->
            binding.vacancyCardContactPerson.text = contactPerson
        }

        viewModel.organizationPhone.observe(viewLifecycleOwner) { organizationPhone ->
            binding.vacancyCardOrganizationPhone.text = organizationPhone
        }

        viewModel.organizationCity.observe(viewLifecycleOwner) { organizationCity ->
            binding.vacancyCardVacancyCity.text = organizationCity
        }

        viewModel.vacancyDescription.observe(viewLifecycleOwner) { vacancyDescription ->
            binding.vacancyCardDescription.text = vacancyDescription
        }

        viewModel.isEditButtonVisible.observe(viewLifecycleOwner) { isEditButtonVisible ->
            binding.vacancyCardEditVacancyButton.visibility = if (isEditButtonVisible) View.VISIBLE else View.GONE
        }

        binding.vacancyCardEditVacancyButton.setOnClickListener {
            arguments?.getString(DOCUMENT_ID_KEY)?.let { documentId ->
                val bundle = Bundle()
                bundle.putString(DOCUMENT_ID_KEY, documentId)
                controller.navigate(R.id.action_vacancyCardFragment_to_editVacancyMainFragment, bundle)
            }
        }
    }
}


