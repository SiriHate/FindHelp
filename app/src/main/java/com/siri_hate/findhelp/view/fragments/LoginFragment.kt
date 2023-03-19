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
import com.siri_hate.findhelp.R
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

        var isEmpty = false // Проверка на заполнение поля

        // Проверка на заполнение поля "email"
        if (email.isEmpty()) {
            emailInput.error = "Введите email"
            isEmpty = true
        }

        // Проверка на заполнение поля "пароль"
        if (password.isEmpty()) {
            passwordInput.error = "Введите пароль"
            isEmpty = true
        }

        // Если какое-то из полей пустое то выводим ошибку и выходим из функции
        if (isEmpty) {
            return
        }

        // Обработка ошибок авторизации и успешной авторизации
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), UserPageActivity::class.java)
                    startActivity(intent) // Запуск экрана пользователя
                    requireActivity().finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            requireContext(),
                            "Неверный email или пароль",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка авторизации: " + task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}