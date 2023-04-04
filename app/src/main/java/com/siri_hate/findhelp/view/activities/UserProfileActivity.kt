package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserSkillsAdapter
class UserProfileActivity : AppCompatActivity() {

    private lateinit var userProfileGoUserPage: Button
    private lateinit var userProfileDummyButton: Button
    private lateinit var userProfileLogoutButton: Button
    private lateinit var userProfileSkillList: ListView
    private lateinit var adapter: UserSkillsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        userProfileGoUserPage = findViewById(R.id.user_profile_go_user_page)
        userProfileDummyButton = findViewById(R.id.user_profile_dummy_button)
        userProfileLogoutButton = findViewById(R.id.user_profile_logout_button)
        userProfileSkillList = findViewById(R.id.user_profile_skill_list)

        // Слушатель кнопки "Главная"
        userProfileGoUserPage.setOnClickListener {
            val intent = Intent(this, UserPageActivity::class.java)
            startActivity(intent)
        }

        // Слушатель кнопки "Профиль"
        userProfileDummyButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Выйти из аккаунта"
        userProfileLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
        adapter = UserSkillsAdapter(this, db, userEmail, emptyList())
        userProfileSkillList.adapter = adapter

        db.collection("user_skills").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val skillsMap = documentSnapshot.get("skills") as? Map<String, Any>
                val skillsList = skillsMap?.keys?.toList() ?: emptyList()
                adapter.updateSkillsList(skillsList)
            }
    }
}