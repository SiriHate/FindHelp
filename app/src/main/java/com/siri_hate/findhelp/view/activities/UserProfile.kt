package com.siri_hate.findhelp.view.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserSkillsAdapter

class UserProfile : AppCompatActivity() {

    private lateinit var userSkillsList: ListView
    private lateinit var adapter: UserSkillsAdapter
    private lateinit var email: String
    private lateinit var userMainPageProfileButton: Button
    private lateinit var userProfileProfileButton: Button
    private lateinit var userLogoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        initUI()
        email = getUserEmail()
        initListViewAndAdapter()
        fetchUserSkillsFromFirestore()
        setItemClickListenerForListView()
    }

    private fun initUI() {
        userMainPageProfileButton = findViewById(R.id.UserMainPageProfileButton)
        userProfileProfileButton = findViewById(R.id.UserProfileProfileButton)
        userLogoutButton = findViewById(R.id.UserLogoutButton)

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
    }

    private fun getUserEmail(): String {
        return FirebaseAuth.getInstance().currentUser!!.email ?: ""
    }

    private fun initListViewAndAdapter() {
        userSkillsList = findViewById(R.id.UserSkillsList)
        adapter = UserSkillsAdapter(this, mutableListOf())
        userSkillsList.adapter = adapter
    }

    private fun fetchUserSkillsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val skillsRef = db.collection("user_skills").document(email)

        skillsRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.w(TAG, "Неудачная попытка прослушивания.", e)
                return@addSnapshotListener
            }

            documentSnapshot?.let { snapshot ->
                if (snapshot.exists()) {
                    val skillsMap = snapshot.data?.mapValues { (_, value) -> value as Boolean }
                    skillsMap?.let { adapter.updateSkills(it) }
                } else {
                    Log.d(TAG, "Текущие данные: null")
                }
            }
        }
    }

    private fun setItemClickListenerForListView() {
        userSkillsList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val skillItem = adapter.getItem(position)

                skillItem?.let {
                    val db = FirebaseFirestore.getInstance()
                    val skillsRef = db.collection("user_skills").document(email)
                    skillsRef.update(it.name, it.isSelected)
                        .addOnSuccessListener {
                            Log.d(TAG, "Документ успешно обновлен!")
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.w(TAG, "Ошибка обновления документа", e)
                        }
                }
            }
    }
}
