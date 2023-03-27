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

    private lateinit var userMainPageProfileButton: Button
    private lateinit var userProfileProfileButton: Button
    private lateinit var userLogoutButton: Button
    private lateinit var listView: ListView
    private lateinit var adapter: UserSkillsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        userMainPageProfileButton = findViewById(R.id.UserMainPageProfileButton)
        userProfileProfileButton = findViewById(R.id.UserProfileProfileButton)
        userLogoutButton = findViewById(R.id.UserLogoutButton)
        listView = findViewById(R.id.user_skills_list)

        userMainPageProfileButton.setOnClickListener {
            val intent = Intent(this, UserPageActivity::class.java)
            startActivity(intent)
        }

        userProfileProfileButton.setOnClickListener {}

        userLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        adapter = UserSkillsAdapter(this, db, userEmail, emptyList())
        listView.adapter = adapter

        db.collection("user_skills").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val skillsMap = documentSnapshot.get("skills") as? Map<String, Boolean> ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                adapter.updateSkillsList(skillsList)
            }
    }
}