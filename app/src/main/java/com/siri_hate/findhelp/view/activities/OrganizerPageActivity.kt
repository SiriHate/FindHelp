package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.siri_hate.findhelp.R

class OrganizerPageActivity : AppCompatActivity() {

    private lateinit var organizerPageAddVacancyButton: Button
    private lateinit var organizerPageLogoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organizer_page)

        organizerPageAddVacancyButton = findViewById(R.id.organizer_page_add_vacancy_button)

        organizerPageAddVacancyButton.setOnClickListener {
            val intent = Intent(this, CreateVacancyPage::class.java)
            startActivity(intent)
        }

        organizerPageLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}