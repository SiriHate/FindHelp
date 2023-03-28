package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.siri_hate.findhelp.R

class OrganizerPageActivity : AppCompatActivity() {

    private lateinit var addVacancyButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organizer_page)

        addVacancyButton = findViewById(R.id.organizer_page_add_vacancy_button)

        addVacancyButton.setOnClickListener {
            val intent = Intent(this, CreateVacancyPage::class.java)
            startActivity(intent)
        }

    }
}