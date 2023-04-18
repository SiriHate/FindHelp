package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.VacancySkillsListAdapter

class VacancyCardFragment : Fragment() {

    companion object {
        private const val TAG = "VacancyCardActivity"
        private const val DOCUMENT_ID_KEY = "document_id"
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        private const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
        private const val USER_RIGHTS_COLLECTION = "user_rights"
        private const val USER_TYPE_FIELD = "userType"
        private const val USER_TYPE_ORGANIZER_VALUE = "organizer"
        private const val CREATOR_EMAIL_FIELD = "creator_email"
    }

    private lateinit var vacancyNameTextView: TextView
    private lateinit var companyNameTextView: TextView
    private lateinit var contactPersonTextView: TextView
    private lateinit var organizationPhoneTextView: TextView
    private lateinit var organizationCityTextView: TextView
    private lateinit var vacancyDescriptionTextView: TextView
    private lateinit var vacancyCardEditVacancyButton: Button
    private lateinit var skillsListView: RecyclerView
    private lateinit var skillsList: MutableList<String>
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
        initSkillsListView()
        setVacancyInfo()

        vacancyCardEditVacancyButton.setOnClickListener { editVacancy() }
    }

    private fun editVacancy() {
        val documentId = arguments?.getString(DOCUMENT_ID_KEY) ?: ""
        val bundle = Bundle()
        bundle.putString(DOCUMENT_ID_KEY, documentId)
        controller.navigate(R.id.action_vacancyCardFragment_to_editVacancyMainFragment, bundle)
    }

    private fun initViews(view: View) {
        vacancyNameTextView = view.findViewById(R.id.vacancy_card_name)
        companyNameTextView = view.findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = view.findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = view.findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = view.findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = view.findViewById(R.id.vacancy_card_description)
        vacancyCardEditVacancyButton = view.findViewById(R.id.vacancy_card_edit_vacancy_button)
        skillsListView = view.findViewById(R.id.vacancy_card_skills_list)
        db = FirebaseFirestore.getInstance()
    }


    private fun initSkillsListView() {
        skillsList = mutableListOf()
        skillsListView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = VacancySkillsListAdapter(skillsList)
        skillsListView.adapter = adapter
    }

    private fun setVacancyInfo() {
        val documentId = arguments?.getString(DOCUMENT_ID_KEY) ?: return
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
        db.collection(VACANCIES_LIST_COLLECTION).document(documentId)
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
            val newSkillsList = mutableListOf<String>()
            for ((key, value) in skillsMap) {
                if (value as Boolean) {
                    newSkillsList.add(key.toString())
                }
            }
            val diffResult = DiffUtil.calculateDiff(
                VacancySkillsListAdapter.SkillsDiffCallback(
                    adapter.skillsList,
                    newSkillsList
                )
            )
            adapter.skillsList.clear()
            adapter.skillsList.addAll(newSkillsList)
            diffResult.dispatchUpdatesTo(adapter)
        }
    }

    private fun updateVacancyInfo(snapshot: DocumentSnapshot) {
        val skillsMap = snapshot.get(VACANCY_SKILLS_LIST_FIELD) as Map<*, *>?
        updateSkillsList(skillsMap, skillsListView.adapter as VacancySkillsListAdapter)

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
            db.collection(USER_RIGHTS_COLLECTION).document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userType = document.getString(USER_TYPE_FIELD)
                        if ((userType == USER_TYPE_ORGANIZER_VALUE) && (user.email == snapshot.getString(
                                CREATOR_EMAIL_FIELD
                            ))
                        ) {
                            vacancyCardEditVacancyButton.visibility = View.VISIBLE
                        } else {
                            vacancyCardEditVacancyButton.visibility = View.GONE
                        }
                    } else {
                        Log.d(TAG, "User document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting user document", exception)
                }
        }
    }
}


