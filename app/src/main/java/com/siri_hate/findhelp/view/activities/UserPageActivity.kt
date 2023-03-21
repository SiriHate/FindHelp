package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.siri_hate.findhelp.R

class UserPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_page)

        // Переменные UI-элементов
        val userMainPageButton: Button = findViewById(R.id.UserMainPageButton)
        val userProfilePageButton: Button = findViewById(R.id.UserProfilePageButton)

        // Слушатель кнопки "Главная"
        userMainPageButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Профиль"
        userProfilePageButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }


    }
}