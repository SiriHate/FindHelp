package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.siri_hate.findhelp.R

class UserProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        // Переменные UI-элементов
        val userMainPageProfileButton: Button = findViewById(R.id.UserMainPageProfileButton)
        val userProfileProfileButton: Button = findViewById(R.id.UserProfileProfileButton)
        val userLogoutButton: Button = findViewById(R.id.UserLogoutButton)
        val auth = FirebaseAuth.getInstance()

        // Слушатель кнопки перехода на главную страницу
        userMainPageProfileButton.setOnClickListener {
            val intent = Intent(this, UserPageActivity::class.java)
            startActivity(intent)
        }

        // Слушатель кнопки перехода в профиль
        userProfileProfileButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки выхода из аккаунта
        userLogoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this,  AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}