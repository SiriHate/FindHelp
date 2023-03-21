package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.siri_hate.findhelp.R

class OrganizerPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organizer_page)

        // Переменные UI-элементов
        val organizerMainPageButton: Button = findViewById(R.id.OrganizerMainPageButton)
        val organizerProfilePageButton: Button = findViewById(R.id.OrganizerProfilePageButton)

        // Слушатель кнопки "Главная"
        organizerMainPageButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Профиль"
        organizerProfilePageButton.setOnClickListener {
            val intent = Intent(this, OrganizerProfile::class.java)
            startActivity(intent)
        }

    }
}