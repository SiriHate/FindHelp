package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.VacancySkillsListAdapter

class VacancyCardFragment : Fragment() {

    private lateinit var vacancyNameTextView: TextView
    private lateinit var companyNameTextView: TextView
    private lateinit var contactPersonTextView: TextView
    private lateinit var organizationPhoneTextView: TextView
    private lateinit var organizationCityTextView: TextView
    private lateinit var vacancyDescriptionTextView: TextView
    private lateinit var vacancyCardGoBackButton: ImageButton
    private lateinit var vacancyCardEditVacancyButton: ImageButton
    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var controller: NavController

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
        initSkillsRecyclerView()
        setVacancyInfo()

        vacancyCardGoBackButton.setOnClickListener { navigateToUserPage() }
        vacancyCardEditVacancyButton.setOnClickListener { editVacancy() }
    }

    private fun editVacancy() {
        val documentId = arguments?.getString("document_id") ?: ""
        val bundle = Bundle()
        bundle.putString("document_id", documentId)
        controller.navigate(R.id.action_vacancyCardFragment_to_editVacancyMainFragment, bundle)
    }

    private fun initViews(view: View) {
        vacancyNameTextView = view.findViewById(R.id.vacancy_card_name)
        companyNameTextView = view.findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = view.findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = view.findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = view.findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = view.findViewById(R.id.vacancy_card_description)
        vacancyCardGoBackButton = view.findViewById(R.id.vacancy_card_go_back_button)
        vacancyCardEditVacancyButton = view.findViewById(R.id.vacancy_card_edit_vacancy_button)
        skillsRecyclerView = view.findViewById(R.id.vacancy_card_skills_list)
        db = FirebaseFirestore.getInstance()
    }

    private fun initSkillsRecyclerView() {
        skillsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = VacancySkillsListAdapter(requireContext())
        skillsRecyclerView.adapter = adapter
    }

    private fun setVacancyInfo() {
        val documentId = arguments?.getString("document_id") ?: return
        getVacancyDocument(documentId,
            { snapshot ->
                updateVacancyInfo(snapshot)
                FirebaseAuth.getInstance().currentUser?.let {
                    checkUserRightsAndSetEditButtonVisibility(it, snapshot)
                }
            },
            { exception ->
                Log.d(TAG, "Error getting vacancy document", exception)
            }
        )
    }

    private fun getVacancyDocument(
        documentId: String,
        onSuccess: (snapshot: DocumentSnapshot) -> Unit,
        onFailure: (exception: Exception) -> Unit
    ) {
        db.collection("vacancies_list").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onSuccess(document)
                } else {
                    Log.d(TAG, "Vacancy document not found")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun updateSkillsList(skillsMap: Map<*, *>?, adapter: VacancySkillsListAdapter) {
        if (skillsMap != null) {
            val skillsList = mutableListOf<String>()
            for ((key, value) in skillsMap) {
                if (value as Boolean) {
                    skillsList.add(key.toString())
                }
            }
            adapter.setSkillsList(skillsList)
        }
    }

    private fun updateVacancyInfo(snapshot: DocumentSnapshot) {
        val skillsMap = snapshot.get("vacancy_skills_list") as Map<*, *>?
        updateSkillsList(skillsMap, skillsRecyclerView.adapter as VacancySkillsListAdapter)

        vacancyNameTextView.text = snapshot.getString("vacancy_name")
        companyNameTextView.text = snapshot.getString("organization_name")
        contactPersonTextView.text = snapshot.getString("contact_person")
        organizationPhoneTextView.text = snapshot.getString("organization_phone")
        organizationCityTextView.text = snapshot.getString("vacancy_city")
        vacancyDescriptionTextView.text = snapshot.getString("vacancy_description")
    }

    private fun checkUserRightsAndSetEditButtonVisibility(
        user: FirebaseUser,
        snapshot: DocumentSnapshot
    ) {
        user.email?.let { email ->
            db.collection("user_rights").document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userType = document.getString("rights_type")
                            if (userType == "admin" || userType == "company") {
                                vacancyCardEditVacancyButton.visibility = View.VISIBLE
                            }
                    } else {
                        Log.d(TAG, "User rights not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting user rights", exception)
                }
        }
    }

    private fun navigateToUserPage() {
        controller.popBackStack()
    }

    companion object {
        const val TAG = "VacancyCardFragment"
    }
}




