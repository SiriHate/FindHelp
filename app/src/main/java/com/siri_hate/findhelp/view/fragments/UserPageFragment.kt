package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserVacancyAdapter

class UserPageFragment : Fragment() {

    private val db by lazy { Firebase.firestore }
    private lateinit var userDoc: DocumentSnapshot
    private var allVacancies = mutableListOf<DocumentSnapshot>()
    private var filteredVacancies = mutableListOf<DocumentSnapshot>()

    private lateinit var userPageDummyButton: Button
    private lateinit var userPageGoProfileButton:Button
    private lateinit var userPageVacancyList: ListView
    private lateinit var searchView: EditText
    private lateinit var controller: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_page_fragment, container, false)

        userPageDummyButton = view.findViewById(R.id.user_page_dummy_button)
        userPageGoProfileButton = view.findViewById(R.id.user_page_go_profile_button)
        userPageVacancyList = view.findViewById(R.id.user_page_vacancy_list)
        searchView = view.findViewById(R.id.user_page_search_bar)

        controller = findNavController()

        // Слушатель кнопки "Главная"
        userPageDummyButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Профиль"
        userPageGoProfileButton.setOnClickListener {
            controller.navigate(R.id.action_userPageFragment_to_userProfileFragment)
        }

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail != null) {
            db.collection("user_skills")
                .document(currentUserEmail)
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
                                val adapter = UserVacancyAdapter(filteredVacancies, userDoc, controller)
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

        // Обработчик изменения текста в EditText
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterVacancies(s.toString())
                val adapter = UserVacancyAdapter(filteredVacancies, userDoc, controller)
                userPageVacancyList.adapter = adapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    // Функция для фильтрации вакансий
    private fun filterVacancies(query: String) {
        filteredVacancies = allVacancies.filter {
            val vacancyName = it.getString("vacancy_name") ?: ""
            vacancyName.startsWith(query, ignoreCase = true)
        }.toMutableList()
    }

    companion object {
        private const val TAG = "UserPageFragment"
    }
}