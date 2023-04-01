package com.siri_hate.findhelp.view.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
    private lateinit var searchView: EditText
    private val db = Firebase.firestore
    private lateinit var userDoc: DocumentSnapshot
    private var allVacancies = mutableListOf<DocumentSnapshot>()
    private var filteredVacancies = mutableListOf<DocumentSnapshot>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_page)

        // Переменные UI-элементов
        userPageDummyButton = findViewById(R.id.user_page_dummy_button)
        userPageGoProfileButton = findViewById(R.id.user_page_go_profile_button)
        userPageVacancyList = findViewById(R.id.user_page_vacancy_list)
        searchView = findViewById(R.id.user_page_search_bar)

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

                            allVacancies.clear()
                            filteredVacancies.clear()

                            value?.documents?.let { allVacancies.addAll(it) }
                            filterVacancies("")
                            val adapter = UserVacancyAdapter(filteredVacancies, userDoc)
                            userPageVacancyList.adapter = adapter
                        }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        // Обработчик изменения текста в EditText
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterVacancies(s.toString())
                val adapter = UserVacancyAdapter(filteredVacancies, userDoc)
                userPageVacancyList.adapter = adapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Функция для фильтрации вакансий
    private fun filterVacancies(query: String) {
        filteredVacancies = allVacancies.filter {
            val vacancyName = it.getString("vacancy_name") ?: ""
            vacancyName.startsWith(query, ignoreCase = true)
        }.toMutableList()
    }
}