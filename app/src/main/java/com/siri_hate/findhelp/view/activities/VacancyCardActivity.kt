package com.siri_hate.findhelp.view.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var skillsListView: ListView
    private lateinit var skillsList: MutableList<String>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vacancy_card)

        initViews()
        initSkillsListView()
        setVacancyInfo()
        setGoBackButtonListener()
    }

    private fun initViews() {
        vacancyNameTextView = findViewById(R.id.vacancy_card_name)
        companyNameTextView = findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = findViewById(R.id.vacancy_card_description)
        vacancyCardGoBackButton = findViewById(R.id.vacancy_card_go_back_button)
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
        db.collection("vacancies_list").document(documentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val skillsMap = snapshot.get("vacancy_skills_list") as Map<*, *>?
                    if (skillsMap != null) {
                        skillsList.clear()
                        for ((key, value) in skillsMap) {
                            if (value as Boolean) {
                                skillsList.add(key.toString())
                            }
                        }
                        (skillsListView.adapter as VacancySkillsListAdapter).notifyDataSetChanged()
                    }

                    vacancyNameTextView.text = snapshot.getString("vacancy_name")
                    companyNameTextView.text = snapshot.getString("organization_name")
                    contactPersonTextView.text = snapshot.getString("contact_person")
                    organizationPhoneTextView.text = snapshot.getString("organization_phone")
                    organizationCityTextView.text = snapshot.getString("vacancy_city")
                    vacancyDescriptionTextView.text = snapshot.getString("vacancy_description")
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    private fun setGoBackButtonListener() {
        vacancyCardGoBackButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.email?.let { email ->
                db.collection("user_rights").document(email)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            when (val userType = document.getString("userType")) {
                                "user" -> {
                                    startActivity(Intent(this,
                                        UserPageActivity::class.java))
                                    finish()
                                }
                                "organizer" -> {
                                    startActivity(Intent(this,
                                        OrganizerPageActivity::class.java))
                                    finish()
                                }
                                "moderator" -> {
                                    startActivity(Intent(this,
                                        ModeratorPageActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    Log.d(TAG, "Неккоректный userType: $userType")
                                }
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
    }
}





