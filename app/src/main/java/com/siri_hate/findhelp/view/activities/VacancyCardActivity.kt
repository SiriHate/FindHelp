package com.siri_hate.findhelp.view.activities

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vacancy_card)

        vacancyNameTextView = findViewById(R.id.vacancy_card_name)
        companyNameTextView = findViewById(R.id.vacancy_card_company_name)
        contactPersonTextView = findViewById(R.id.vacancy_card_contact_person)
        organizationPhoneTextView = findViewById(R.id.vacancy_card_organization_phone)
        organizationCityTextView = findViewById(R.id.vacancy_card_organization_city)
        vacancyDescriptionTextView = findViewById(R.id.vacancy_card_description)
        vacancyCardGoBackButton = findViewById(R.id.vacancy_card_go_back_button)

        val documentId = intent.getStringExtra("document_id") ?: return

        vacancyCardGoBackButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            user?.email?.let { email ->
                db.collection("user_rights").document(email)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            when (val userType = document.getString("userType")) {
                                "user" -> {
                                    val intent = Intent(this,
                                        UserPageActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                "organizer" -> {
                                    val intent = Intent(this,
                                        OrganizerPageActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                "moderator" -> {
                                    val intent = Intent(this,
                                        ModeratorPageActivity::class.java)
                                    startActivity(intent)
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

        val skillsListView = findViewById<ListView>(R.id.vacancy_card_skills_list)
        val skillsList = mutableListOf<String>()

        val db = FirebaseFirestore.getInstance()
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
                        val adapter = VacancySkillsListAdapter(this, skillsList)
                        skillsListView.adapter = adapter
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }

        db.collection("vacancies_list").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    vacancyNameTextView.text = document.getString("vacancy_name")
                    companyNameTextView.text = document.getString("organization_name")
                    contactPersonTextView.text = document.getString("contact_person")
                    organizationPhoneTextView.text = document.getString("organization_phone")
                    organizationCityTextView.text = document.getString("vacancy_city")
                    vacancyDescriptionTextView.text = document.getString("vacancy_description")
                } else {
                    Log.d(TAG, "Документ не найден")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Ошибка получения документа", exception)
            }
    }

    companion object {
        const val TAG = "VacancyCard"
    }
}