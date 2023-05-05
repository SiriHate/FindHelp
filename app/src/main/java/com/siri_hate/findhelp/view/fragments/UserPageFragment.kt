package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentUserPageBinding
import com.siri_hate.findhelp.view.adapters.UserVacancyAdapter

class UserPageFragment : Fragment() {

    private val db by lazy { Firebase.firestore }
    private lateinit var userDoc: DocumentSnapshot
    private var allVacancies = mutableListOf<DocumentSnapshot>()
    private var filteredVacancies = mutableListOf<DocumentSnapshot>()
    private lateinit var controller: NavController
    private lateinit var adapter: UserVacancyAdapter
    private lateinit var binding: FragmentUserPageBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserPageBinding.inflate(inflater, container, false)

        loading(true)
        fetchCurrentUserDocument()

        setupNavigation()
        setupSearchBar()

        return binding.root
    }

    private fun setupNavigation() {
        controller = findNavController()
        binding.userPageMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_item_home -> {
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
                    binding.userPageMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked =
                        true
                }

                R.id.userProfileFragment -> {
                    binding.userPageMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }
    }

    private fun setupSearchBar() {
        binding.userPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndSortVacancies(newText ?: "")
                return true
            }
        })
    }

    private fun fetchCurrentUserDocument() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        (currentUserEmail)?.let { it ->
            db.collection(USER_INFO_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userDoc = document

                        adapter = UserVacancyAdapter(filteredVacancies, userDoc, controller)
                        binding.userPageVacancyList.adapter = adapter

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

                                loading(false)
                            }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error: ", exception)
                    Toast.makeText(
                        context,
                        getString(R.string.user_page_list_loading_err_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.userPageLoadingProgressBar.visibility = View.VISIBLE
            binding.userPageVacancyList.visibility = View.GONE
            binding.userPageEmptyListMessage.visibility = View.GONE
        } else {
            if (filteredVacancies.isEmpty()) {
                binding.userPageEmptyListMessage.visibility = View.VISIBLE
                binding.userPageVacancyList.visibility = View.GONE
            } else {
                binding.userPageEmptyListMessage.visibility = View.GONE
                binding.userPageVacancyList.visibility = View.VISIBLE
            }
            binding.userPageLoadingProgressBar.visibility = View.GONE
        }
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

        if (filteredVacancies.isEmpty()) {
            binding.userPageEmptyListMessage.visibility = View.VISIBLE
            binding.userPageVacancyList.visibility = View.GONE
        } else {
            binding.userPageEmptyListMessage.visibility = View.GONE
            binding.userPageVacancyList.visibility = View.VISIBLE
        }

        adapter.updateList(filteredVacancies.toList())
    }
}