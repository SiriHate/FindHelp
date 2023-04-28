package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
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

    private lateinit var userPageVacancyList: RecyclerView
    private lateinit var userPageSearchBar: SearchView
    private lateinit var controller: NavController
    private lateinit var userPageMenu: BottomNavigationView

    companion object {
        private const val TAG = "UserPageFragment"
        private const val VACANCY_CITY_FIELD = "vacancy_city"
        private const val USER_CITY_FIELD = "user_city"
        private const val VACANCY_NAME_FIELD = "vacancy_name"
        private const val USER_INFO_COLLECTION = "user_info"
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        private const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
        private const val SKILLS_FIELD = "skills"
    }

    private lateinit var adapter: UserVacancyAdapter

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
            db.collection(USER_INFO_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userDoc = document

                        adapter = UserVacancyAdapter(filteredVacancies, userDoc, controller)
                        userPageVacancyList.adapter = adapter

                        db.collection(VACANCIES_LIST_COLLECTION)
                            .addSnapshotListener { value, error ->
                                if (error != null) {
                                    Log.w(TAG, "Listen failed.", error)
                                    return@addSnapshotListener
                                }

                                allVacancies.clear()
                                filteredVacancies.clear()

                                value?.documents?.let { allVacancies.addAll(it) }

                                filterAndSortVacancies("")
                            }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

        userPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndSortVacancies(newText ?: "")
                return true
            }
        })

        return view
    }

    private fun filterAndSortVacancies(query: String) {
        filteredVacancies = allVacancies.filter {
            val vacancyCity = it.getString(VACANCY_CITY_FIELD)
            vacancyCity == userDoc.getString(USER_CITY_FIELD)
        }.toMutableList()

        if (query.isNotEmpty()) {
            filteredVacancies = filteredVacancies.filter {
                val vacancyName = it.getString(VACANCY_NAME_FIELD) ?: ""
                vacancyName.startsWith(query, ignoreCase = true)
            }.toMutableList()
        }

        filteredVacancies.sortByDescending { vacancy ->
            @Suppress("UNCHECKED_CAST")
            val vacancySkillsList = vacancy[VACANCY_SKILLS_LIST_FIELD] as? HashMap<String, Boolean>
            @Suppress("UNCHECKED_CAST")
            val userSkills = userDoc[SKILLS_FIELD] as? HashMap<String, Boolean>
            var matchCount = 0
            var vacancyCount = 0

            vacancySkillsList?.forEach { (skill, value) ->
                if (value && userSkills?.get(skill) == true) {
                    matchCount++
                }
                if (value) {
                    vacancyCount++
                }
            }

            if (vacancyCount == 0) 0 else (matchCount * 100 / vacancyCount)
        }

        adapter.updateList(filteredVacancies)
    }
}