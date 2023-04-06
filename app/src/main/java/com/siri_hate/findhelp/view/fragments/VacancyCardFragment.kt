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
    private lateinit var skillsListView: ListView
    private lateinit var skillsList: MutableList<String>
    private lateinit var db: FirebaseFirestore
    private lateinit var controller: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        controller = findNavController()
        return inflater.inflate(R.layout.vacancy_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initSkillsListView()
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
        skillsListView = view.findViewById(R.id.vacancy_card_skills_list)
        db = FirebaseFirestore.getInstance()
    }


    private fun initSkillsListView() {
        skillsList = mutableListOf()
        val adapter = VacancySkillsListAdapter(requireContext(), skillsList)
        skillsListView.adapter = adapter
    }

    private fun setVacancyInfo() {
        val documentId = arguments?.getString("document_id") ?: return
        getVacancyDocument(documentId,
            { snapshot ->
                updateVacancyInfo(snapshot)
                checkUserRightsAndSetEditButtonVisibility(
                    FirebaseAuth.getInstance().currentUser,
                    snapshot
                )
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
            skillsList.clear()
            for ((key, value) in skillsMap) {
                if (value as Boolean) {
                    skillsList.add(key.toString())
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateVacancyInfo(snapshot: DocumentSnapshot) {
        val skillsMap = snapshot.get("vacancy_skills_list") as Map<*, *>?
        updateSkillsList(skillsMap, skillsListView.adapter as VacancySkillsListAdapter)

        vacancyNameTextView.text = snapshot.getString("vacancy_name")
        companyNameTextView.text = snapshot.getString("organization_name")
        contactPersonTextView.text = snapshot.getString("contact_person")
        organizationPhoneTextView.text = snapshot.getString("organization_phone")
        organizationCityTextView.text = snapshot.getString("vacancy_city")
        vacancyDescriptionTextView.text = snapshot.getString("vacancy_description")
    }

    private fun checkUserRightsAndSetEditButtonVisibility(
        user: FirebaseUser?,
        snapshot: DocumentSnapshot
    ) {
        user?.email?.let { email ->
            db.collection("user_rights").document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userType = document.getString("userType")
                        if (userType == "moderator" ||
                            (userType == "organizer" && user.email ==
                                    snapshot.getString("creator_email"))) {
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

    private fun navigateToUserPage() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let { email ->
            db.collection("user_rights").document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        when (val userType = document.getString("userType")) {
                            "user" -> controller.navigate(R.id.action_vacancyCardFragment_to_userPageFragment)
                            "organizer" -> controller.navigate(R.id.action_vacancyCardFragment_to_organizerPageFragment)
                            "moderator" -> controller.navigate(R.id.action_vacancyCardFragment_to_moderatorPageFragment)
                            else -> Log.d(TAG, "Неккоректный userType: $userType")
                        }
                    } else {
                        Log.d(TAG, "Документ не найден")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Ошибка получения документа", exception)
                }
        }
    }

    companion object {
        private const val TAG = "VacancyCardActivity"
    }
}





