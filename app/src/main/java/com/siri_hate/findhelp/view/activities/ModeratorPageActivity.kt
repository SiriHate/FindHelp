package com.siri_hate.findhelp.view.activities

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter
import java.util.*

class ModeratorPageActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var moderatorLogoutButton: Button
    private lateinit var moderatorVacancyList: ListView

    private val db = FirebaseFirestore.getInstance()
    private val offersRef = db.collection("vacancies_list")
    private lateinit var adapter: ModeratorVacancyListAdapter
    private var snapshotListener: ListenerRegistration? = null
    private var offers: List<DocumentSnapshot> = emptyList()
    private var originalOffers: List<DocumentSnapshot> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.moderator_page)

        // Переменные UI-элементов
        moderatorLogoutButton = findViewById(R.id.moderator_page_logout_button)
        moderatorVacancyList = findViewById(R.id.moderator_vacancy_list)
        searchBar = findViewById(R.id.moderator_page_search_bar)

        // Адаптер
        adapter = ModeratorVacancyListAdapter(this, emptyList())
        moderatorVacancyList.adapter = adapter

        // Слушатель изменения текста в searchBar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Получение текста из searchBar
                val query = s.toString().lowercase(Locale.getDefault())

                // Фильтрация списка вакансий по запросу
                val filteredOffers = offers.filter {
                    it.getString("vacancy_name")?.lowercase(Locale.getDefault())
                        ?.startsWith(query) == true
                }

                // Обновление адаптера с отфильтрованным списком вакансий
                adapter.clear()
                adapter.addAll(filteredOffers)
                adapter.notifyDataSetChanged()
            }
        })

        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            originalOffers = snapshots?.documents?.toList() ?: emptyList()
            offers = originalOffers
            adapter.clear()
            adapter.addAll(offers)
            adapter.notifyDataSetChanged()
        }

        // Слушатель кнопки "Выйти из аккаунта"
        moderatorLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Слушатель изменений коллекции Firestore
        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val offers = snapshots?.documents?.toList() ?: emptyList<DocumentSnapshot>()
            adapter.clear()
            adapter.addAll(offers)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    companion object {
        private const val TAG = "ModeratorPageActivity"
    }
}
