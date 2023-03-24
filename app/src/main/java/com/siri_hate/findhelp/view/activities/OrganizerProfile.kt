package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.siri_hate.findhelp.R

class OrganizerProfile : AppCompatActivity() {

    private lateinit var organizerMainProfileButton: Button
    private lateinit var organizerProfileProfileButton: Button
    private lateinit var organizerLogoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organizer_profile)

        organizerMainProfileButton = findViewById(R.id.Organizer_main_profile_button)
        organizerProfileProfileButton = findViewById(R.id.Organizer_profile_profile_button)
        organizerLogoutButton = findViewById(R.id.OrganizerLogoutButton)

        // Слушатель кнопки "Профиль"
        organizerMainProfileButton.setOnClickListener {
            val intent = Intent(this, OrganizerPageActivity::class.java)
            startActivity(intent)
        }

        // Слушатель кнопки "Главная"
        organizerProfileProfileButton.setOnClickListener {
            // Nothing
        }

        // Слушатель кнопки "Выйти из аккаунта"
        organizerLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}