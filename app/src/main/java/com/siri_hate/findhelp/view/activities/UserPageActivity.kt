package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.siri_hate.findhelp.R

class UserPageActivity : AppCompatActivity() {

    private lateinit var userMainPageButton: Button
    private lateinit var userProfilePageButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_page)

        // Переменные UI-элементов
        userMainPageButton = findViewById(R.id.UserMainPageButton)
        userProfilePageButton = findViewById(R.id.UserProfilePageButton)

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