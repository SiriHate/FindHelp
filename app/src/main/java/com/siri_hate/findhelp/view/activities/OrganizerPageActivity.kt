package com.siri_hate.findhelp.view.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.OrganizerVacancyListAdapter

class OrganizerPageActivity : AppCompatActivity() {

    private lateinit var organizerPageAddVacancyButton: Button
    private lateinit var organizerPageLogoutButton: Button
    private lateinit var listView: ListView
    private lateinit var adapter: OrganizerVacancyListAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organizer_page)

        organizerPageAddVacancyButton = findViewById(R.id.organizer_page_add_vacancy_button)
        organizerPageLogoutButton = findViewById(R.id.organizer_page_logout_button)
        // Инициализация переменных
        listView = findViewById(R.id.organizer_page_vacancy_list)
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email ?: ""

        organizerPageAddVacancyButton.setOnClickListener {
            val intent = Intent(this, CreateVacancyPage::class.java)
            startActivity(intent)
        }

        organizerPageLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Получение списка вакансий из Firebase Firestore
        database.collection("vacancies_list")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val offers = mutableListOf<DocumentSnapshot>()
                for (doc in value!!) {
                    offers.add(doc)
                }

                // Фильтрация списка по полю "creator_email"
                val filteredOffers = offers.filter {
                    it.getString("creator_email") == userEmail
                }

                // Создание адаптера и привязка его к ListView
                adapter = OrganizerVacancyListAdapter(this, filteredOffers)
                listView.adapter = adapter
            }
    }
}