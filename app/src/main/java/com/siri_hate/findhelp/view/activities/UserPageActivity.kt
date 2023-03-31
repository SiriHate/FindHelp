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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserVacancyAdapter

class UserPageActivity : AppCompatActivity() {

    private lateinit var userPageDummyButton: Button
    private lateinit var userPageGoProfileButton: Button
    private lateinit var userPageVacancyList: ListView
    private val db = Firebase.firestore
    private lateinit var userDoc: DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_page)

        // Переменные UI-элементов
        userPageDummyButton = findViewById(R.id.user_page_dummy_button)
        userPageGoProfileButton = findViewById(R.id.user_page_go_profile_button)
        userPageVacancyList = findViewById(R.id.user_page_vacancy_list)

        // Слушатель кнопки "Главная"
        userPageDummyButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Профиль"
        userPageGoProfileButton.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        db.collection("user_skills")
            .document(FirebaseAuth.getInstance().currentUser?.email ?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userDoc = document
                    db.collection("vacancies_list")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                Log.w(TAG, "Listen failed.", error)
                                return@addSnapshotListener
                            }

                            val vacancyList = mutableListOf<DocumentSnapshot>()
                            value?.documents?.let { vacancyList.addAll(it) }
                            val adapter = UserVacancyAdapter(vacancyList, userDoc)
                            userPageVacancyList.adapter = adapter
                        }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
}