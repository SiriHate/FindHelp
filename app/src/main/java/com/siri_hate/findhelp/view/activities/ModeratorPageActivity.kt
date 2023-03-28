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
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter

class ModeratorPageActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val offersRef = db.collection("vacancies_list")
    private lateinit var adapter: ModeratorVacancyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.moderator_page)

        // Переменные UI-элементов
        val moderatorLogoutButton: Button = findViewById(R.id.moderator_page_logout_button)
        val moderatorVacancyList: ListView = findViewById(R.id.moderator_vacancy_list)
        adapter = ModeratorVacancyListAdapter(this, emptyList())
        moderatorVacancyList.adapter = adapter

        // Слушатель кнопки "Выйти из аккаунта"
        moderatorLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Слушатель изменений коллекции Firestore
        offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val offers = ArrayList<DocumentSnapshot>()
            for (doc in snapshots!!.documents) {
                offers.add(doc)
            }

            adapter.clear()
            adapter.addAll(offers)
            adapter.notifyDataSetChanged()
        }
    }
}
