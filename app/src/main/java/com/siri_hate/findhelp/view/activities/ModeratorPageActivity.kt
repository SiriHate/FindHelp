package com.siri_hate.findhelp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.siri_hate.findhelp.R

class ModeratorPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.moderator_page)

        // Переменные UI-элементов
        val moderatorLogoutButton: Button = findViewById(R.id.ModeratorLogoutButton)
        val auth = FirebaseAuth.getInstance()


        // Слушатель кнопки "Выйти из аккаунта"
        moderatorLogoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this,  AuthorizationPageActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}
