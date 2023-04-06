package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserSkillsAdapter
class UserProfileFragment : Fragment() {
    private lateinit var userProfileGoUserPage: Button
    private lateinit var userProfileDummyButton: Button
    private lateinit var userProfileLogoutButton: Button
    private lateinit var userProfileSkillList: ListView
    private lateinit var adapter: UserSkillsAdapter
    private lateinit var controller: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.user_profile_fragment, container, false)

        userProfileGoUserPage = view.findViewById(R.id.user_profile_go_user_page)
        userProfileDummyButton = view.findViewById(R.id.user_profile_dummy_button)
        userProfileLogoutButton = view.findViewById(R.id.user_profile_logout_button)
        userProfileSkillList = view.findViewById(R.id.user_profile_skill_list)

        controller = findNavController()

        // Слушатель кнопки "Главная"
        userProfileGoUserPage.setOnClickListener {
            controller.navigate(R.id.action_userProfileFragment_to_userPageFragment)
        }

        // Слушатель кнопки "Профиль"
        userProfileDummyButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Выйти из аккаунта"
        userProfileLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            controller.navigate(R.id.action_userProfileFragment_to_loginFragment)
        }

        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
        adapter = UserSkillsAdapter(requireContext(), db, userEmail, emptyList())
        userProfileSkillList.adapter = adapter

        db.collection("user_skills").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val skillsMap = documentSnapshot.get("skills") as? Map<String, Any>
                val skillsList = skillsMap?.keys?.toList() ?: emptyList()
                adapter.updateSkillsList(skillsList)
            }

        return view
    }
}