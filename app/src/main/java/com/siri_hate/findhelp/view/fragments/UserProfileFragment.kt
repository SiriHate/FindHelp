package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserSkillsAdapter

class UserProfileFragment : Fragment() {
    private lateinit var userProfileSkillList: ListView
    private lateinit var adapter: UserSkillsAdapter
    private lateinit var controller: NavController
    private lateinit var userProfileMenu: BottomNavigationView
    private lateinit var cityInput: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        userProfileSkillList = view.findViewById(R.id.user_profile_skill_list)
        userProfileMenu = view.findViewById(R.id.user_profile_menu)
        cityInput = view.findViewById(R.id.user_profile_city_input)

        controller = findNavController()
        userProfileMenu.setupWithNavController(controller)
        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

        db.collection("user_info").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val userCity = documentSnapshot.getString("user_city")
                cityInput.setText(userCity)
            }

        userProfileMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_item_home -> {
                    controller.navigate(R.id.action_userProfileFragment_to_userPageFragment)
                    true
                }
                R.id.bottom_navigation_item_profile -> {
                    // Nothing
                    true
                }
                else -> false
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userPageFragment -> {
                    userProfileMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked = true
                }
                R.id.userProfileFragment -> {
                    userProfileMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }

        adapter = UserSkillsAdapter(requireContext(), db, userEmail, emptyList())
        userProfileSkillList.adapter = adapter

        db.collection("user_info").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get("skills") as? Map<String, Any>
                val skillsList = skillsMap?.keys?.toList() ?: emptyList()
                adapter.updateSkillsList(skillsList)
            }

        return view
    }
}