package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
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


    private lateinit var userPageVacancyList: ListView
    private lateinit var userPageSearchBar: SearchView
    private lateinit var controller: NavController
    private lateinit var userPageMenu: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_page, container, false)


        userPageVacancyList = view.findViewById(R.id.user_page_vacancy_list)
        userPageSearchBar = view.findViewById(R.id.user_page_search_bar)
        userPageMenu = view.findViewById(R.id.user_page_menu)

        controller = findNavController()
        userPageMenu.setupWithNavController(controller)

        userPageMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_item_home -> {
                    // Nothing
                    true
                }
                R.id.bottom_navigation_item_profile -> {
                    controller.navigate(R.id.action_userPageFragment_to_userProfileFragment)
                    true
                }
                else -> false
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userPageFragment -> {
                    userPageMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked = true
                }
                R.id.userProfileFragment -> {
                    userPageMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked = true
                }
            }
        }

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        (currentUserEmail)?.let { it ->
            db.collection("user_skills")
                .document(it)
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
        userPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterVacancies(newText ?: "")
                val adapter = UserVacancyAdapter(filteredVacancies, userDoc, controller)
                userPageVacancyList.adapter = adapter
                return true
            }
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