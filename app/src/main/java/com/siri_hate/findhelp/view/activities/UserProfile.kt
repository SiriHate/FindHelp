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
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        initUI()
        uid = getUserUid()
        initListViewAndAdapter()
        fetchUserSkillsFromFirestore()
        setItemClickListenerForListView()
    }

    private fun initUI() {
        val userMainPageProfileButton: Button = findViewById(R.id.UserMainPageProfileButton)
        val userProfileProfileButton: Button = findViewById(R.id.UserProfileProfileButton)
        val userLogoutButton: Button = findViewById(R.id.UserLogoutButton)

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

    private fun getUserUid(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private fun initListViewAndAdapter() {
        userSkillsList = findViewById(R.id.UserSkillsList)
        adapter = UserSkillsAdapter(this, mutableListOf())
        userSkillsList.adapter = adapter
    }

    private fun fetchUserSkillsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val skillsRef = db.collection("user_skills").document(uid)

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
                    val skillsRef = db.collection("user_skills").document(uid)
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
