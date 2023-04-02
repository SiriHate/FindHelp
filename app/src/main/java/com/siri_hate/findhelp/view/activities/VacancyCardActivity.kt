package com.siri_hate.findhelp.view.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.VacancySkillsListAdapter

class VacancyCardActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vacancy_card)

        initViews()
        initSkillsListView()
        setVacancyInfo()

        vacancyCardGoBackButton.setOnClickListener { navigateToUserPage() }

        vacancyCardEditVacancyButton.setOnClickListener { editVacancy() }

    }

    private fun editVacancy() {
        val documentId = intent.getStringExtra("document_id")
        val intent = Intent(this, EditVacancyPage::class.java)
        intent.putExtra("document_id", documentId)
        startActivity(intent)
    }

    private fun initViews() {
        vacancyNameTextView = findViewById(R.id.vacancy_card_name)
        companyNameTextView = findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = findViewById(R.id.vacancy_card_description)
        vacancyCardGoBackButton = findViewById(R.id.vacancy_card_go_back_button)
        vacancyCardEditVacancyButton = findViewById(R.id.vacancy_card_edit_vacancy_button)
        skillsListView = findViewById(R.id.vacancy_card_skills_list)
        db = FirebaseFirestore.getInstance()
    }


    private fun initSkillsListView() {
        skillsList = mutableListOf()
        val adapter = VacancySkillsListAdapter(this, skillsList)
        skillsListView.adapter = adapter
    }

    private fun setVacancyInfo() {
        val documentId = intent.getStringExtra("document_id") ?: return
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
                            "user" -> navigateToActivity(UserPageActivity::class.java)
                            "organizer" -> navigateToActivity(OrganizerPageActivity::class.java)
                            "moderator" -> navigateToActivity(ModeratorPageActivity::class.java)
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

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
}





