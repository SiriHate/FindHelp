package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.VacancySkillsListAdapter
import com.siri_hate.findhelp.viewmodel.fragments.VacancyCardViewModel

class VacancyCardPageFragment : Fragment() {

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    private lateinit var vacancyNameTextView: TextView
    private lateinit var companyNameTextView: TextView
    private lateinit var contactPersonTextView: TextView
    private lateinit var organizationPhoneTextView: TextView
    private lateinit var organizationCityTextView: TextView
    private lateinit var vacancyDescriptionTextView: TextView
    private lateinit var vacancyCardEditVacancyButton: Button
    private lateinit var skillsListView: RecyclerView
    private lateinit var viewModel: VacancyCardViewModel
    private lateinit var controller: NavController
    private lateinit var vacancyCardLoadingBar: ProgressBar
    private lateinit var vacancyCard: CardView
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        controller = findNavController()
        return inflater.inflate(R.layout.fragment_vacancy_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        arguments?.getString(DOCUMENT_ID_KEY)?.let { documentId ->
            viewModel.loadVacancyInfo(documentId, user)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                vacancyCardLoadingBar.visibility = View.VISIBLE
                vacancyCard.visibility = View.GONE
            } else {
                vacancyCardLoadingBar.visibility = View.GONE
                vacancyCard.visibility = View.VISIBLE
            }
        }

        viewModel.skillsList.observe(viewLifecycleOwner) { skillsList ->
            (skillsListView.adapter as VacancySkillsListAdapter).submitList(skillsList)
        }

        viewModel.vacancyName.observe(viewLifecycleOwner) { vacancyName ->
            vacancyNameTextView.text = vacancyName
        }

        viewModel.companyName.observe(viewLifecycleOwner) { companyName ->
            companyNameTextView.text = companyName
        }

        viewModel.contactPerson.observe(viewLifecycleOwner) { contactPerson ->
            contactPersonTextView.text = contactPerson
        }

        viewModel.organizationPhone.observe(viewLifecycleOwner) { organizationPhone ->
            organizationPhoneTextView.text = organizationPhone
        }

        viewModel.organizationCity.observe(viewLifecycleOwner) { organizationCity ->
            organizationCityTextView.text = organizationCity
        }

        viewModel.vacancyDescription.observe(viewLifecycleOwner) { vacancyDescription ->
            vacancyDescriptionTextView.text = vacancyDescription
        }

        viewModel.isEditButtonVisible.observe(viewLifecycleOwner) { isEditButtonVisible ->
            vacancyCardEditVacancyButton.visibility = if (isEditButtonVisible) View.VISIBLE else View.GONE
        }

        vacancyCardEditVacancyButton.setOnClickListener {
            arguments?.getString(DOCUMENT_ID_KEY)?.let { documentId ->
                val bundle = Bundle()
                bundle.putString(DOCUMENT_ID_KEY, documentId)
                controller.navigate(R.id.action_vacancyCardFragment_to_editVacancyMainFragment, bundle)
            }
        }
    }

    private fun initViews(view: View) {
        vacancyCardLoadingBar = view.findViewById(R.id.vacancy_card_loading_bar)
        vacancyCard = view.findViewById(R.id.vacancy_card)
        vacancyNameTextView = view.findViewById(R.id.vacancy_card_name)
        companyNameTextView = view.findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = view.findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = view.findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = view.findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = view.findViewById(R.id.vacancy_card_description)
        vacancyCardEditVacancyButton = view.findViewById(R.id.vacancy_card_edit_vacancy_button)
        skillsListView = view.findViewById(R.id.vacancy_card_skills_list)

        viewModel = ViewModelProvider(this,
            ViewModelProvider.NewInstanceFactory()
        )[VacancyCardViewModel::class.java]

        activity?.let {
            user = FirebaseAuth.getInstance().currentUser
        }

        skillsListView.adapter = VacancySkillsListAdapter()
    }
}


