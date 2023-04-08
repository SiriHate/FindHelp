package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R


class LoginFragment : Fragment() {

    // Переменные для UI
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var registerTextView: TextView
    private lateinit var loginFragmentRegistrationLoginProgressBar: ProgressBar
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_page, container, false)

        // Привязка переменных к UI-элементам
        emailInput = view.findViewById(R.id.login_fragment_login_input)
        passwordInput = view.findViewById(R.id.login_fragment_password_input)
        loginButton = view.findViewById(R.id.login_fragment_login_button)
        registerTextView = view.findViewById(R.id.login_fragment_registration_button)
        loginFragmentRegistrationLoginProgressBar =
            view.findViewById(R.id.login_fragment_registration_login_progress_bar)

        // Переменная для Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Переменная Firebase Firestore
        val db = FirebaseFirestore.getInstance()

        controller = findNavController()

        // Слушатель кнопки "Войти" вызывающий функцию авторизации
        loginButton.setOnClickListener {
            login()
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Получение прав доступа пользователя из базы данных Firebase Firestore
            loginFragmentRegistrationLoginProgressBar.visibility = View.VISIBLE
            val userEmail = currentUser.email
            if (!userEmail.isNullOrEmpty()) {
                db.collection("user_rights").document(userEmail).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userType = task.result?.get("userType") as? String
                            loginFragmentRegistrationLoginProgressBar.visibility = View.INVISIBLE
                            startFragment(userType)
                        } else {
                            showErrorMessage("Ошибка доступа к базе данных: " + task.exception?.message)
                        }
                    }
            }
        }

        // Слушатель кнопки "Зарегистрироваться" вызывающий функцию авторизации
        registerTextView.setOnClickListener {
            controller.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    // Основная функция авторизации
    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (fieldsAreEmpty(email, password)) {
            return
        }

        loginFragmentRegistrationLoginProgressBar.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginSuccessfull()
                } else {
                    loginFailed(task.exception)
                }
            }
    }

    // Функция проверки заполненности полей
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

    // Функция обработки данных в случае успешной авторизации
    private fun loginSuccessfull() {
        val user = firebaseAuth.currentUser
        val email = user?.email

        val db = FirebaseFirestore.getInstance()
        email?.let { userEmail ->
            db.collection("user_rights").document(userEmail).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userType = task.result?.get("userType") as? String
                        if (!userType.isNullOrEmpty()) {
                            startFragment(userType)
                        } else {
                            showErrorMessage("Не удалось определить права доступа")
                        }
                    } else {
                        showErrorMessage("Ошибка доступа к базе данных: " + task.exception?.message)
                    }
                }
        }
    }

    // Функция обработки ошибки в случае неуспешной авторизации
    private fun loginFailed(exception: Exception?) {
        loginFragmentRegistrationLoginProgressBar.visibility = View.INVISIBLE
        if (exception is FirebaseAuthInvalidCredentialsException) {
            showErrorMessage("Неверный email или пароль")
        } else {
            showErrorMessage("Ошибка авторизации: " + exception?.message)
        }
    }

    // Функция запуска главной страницы пользователя
    private fun startFragment(rights: String?) {
        val controller = findNavController()
        when (rights) {
            "user" -> controller.navigate(R.id.action_loginFragment_to_userPageFragment)
            "organizer" -> controller.navigate(R.id.action_loginFragment_to_organizerPageFragment)
            "moderator" -> controller.navigate(R.id.action_loginFragment_to_moderatorPageFragment)
            else -> showErrorMessage("Не удалось определить права доступа")
        }
        loginFragmentRegistrationLoginProgressBar.visibility = View.INVISIBLE
    }

    // Функция отображения ошибки
    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}