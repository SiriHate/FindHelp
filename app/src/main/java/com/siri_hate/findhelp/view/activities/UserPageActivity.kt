package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.siri_hate.findhelp.R

class UserPageActivity : AppCompatActivity() {

    private lateinit var userPageDummyButton: Button
    private lateinit var userPageGoProfileButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_page)

        // Переменные UI-элементов
        userPageDummyButton = findViewById(R.id.user_page_dummy_button)
        userPageGoProfileButton = findViewById(R.id.user_page_go_profile_button)

        // Слушатель кнопки "Главная"
        userPageDummyButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Профиль"
        userPageGoProfileButton.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }


    }
}