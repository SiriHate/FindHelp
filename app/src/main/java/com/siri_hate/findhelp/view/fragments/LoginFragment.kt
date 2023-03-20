package com.siri_hate.findhelp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.ModeratorPageActivity
import com.siri_hate.findhelp.view.activities.OrganizerPageActivity
import com.siri_hate.findhelp.view.activities.UserPageActivity


class LoginFragment : Fragment() {

    // Переменные для UI
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)

        // Привязка переменных к UI-элементам
        emailInput = view.findViewById(R.id.Email_input_login)
        passwordInput = view.findViewById(R.id.Password_input_login)
        loginButton = view.findViewById(R.id.Login_button)

        // Переменная для Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Слушатель кнопки "Войти" вызывающий функцию авторизации
        loginButton.setOnClickListener {
            login()
        }

        return view
    }

    // Функция авторизации
    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (fieldsAreEmpty(email, password)) {
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginSuccessfull()
                } else {
                    loginFailed(task.exception)
                }
            }
    }

    private fun fieldsAreEmpty(email: String, password: String): Boolean {
        var isEmpty = false

        if (email.isEmpty()) {
            emailInput.error = "Введите email"
            isEmpty = true
        }

        if (password.isEmpty()) {
            passwordInput.error = "Введите пароль"
            isEmpty = true
        }

        return isEmpty
    }

    private fun loginSuccessfull() {
        val user = firebaseAuth.currentUser
        val uid = user?.uid

        val database = FirebaseDatabase.getInstance().getReference("users")
        uid?.let { userId ->
            database.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userType = snapshot.child("userType").value.toString()
                        startActivity(userType)
                    } else {
                        showErrorMessage("Не удалось определить права доступа")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorMessage("Ошибка доступа к базе данных: " + error.message)
                }
            })
        }
    }

    private fun loginFailed(exception: Exception?) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            showErrorMessage("Неверный email или пароль")
        } else {
            showErrorMessage("Ошибка авторизации: " + exception?.message)
        }
    }

    private fun startActivity(rights: String) {
        val intent = when (rights) {
            "user" -> Intent(requireContext(), UserPageActivity::class.java)
            "organizer" -> Intent(requireContext(), OrganizerPageActivity::class.java)
            "moderator" -> Intent(requireContext(), ModeratorPageActivity::class.java)
            else -> null
        }

        intent?.let {
            it.putExtra("layout", "${rights}_page")
            startActivity(it)
            requireActivity().finish()
        } ?: showErrorMessage("Не удалось определить права доступа")
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}