package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.siri_hate.findhelp.R

class RegisterFragment : Fragment() {

    // Переменные
    private lateinit var emailInput: EditText
    private lateinit var firstPasswordInput: EditText
    private lateinit var secondPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.register_fragment, container, false)

        // Переменные
        emailInput = view.findViewById(R.id.Email_input_register)
        firstPasswordInput = view.findViewById(R.id.First_password_input_register)
        secondPasswordInput = view.findViewById(R.id.Second_password_input_register)
        registerButton = view.findViewById(R.id.Register_button)
        firebaseAuth = FirebaseAuth.getInstance()


        // Слушатель кнопки регистрации вызывает функцию регистрации
        registerButton.setOnClickListener {
            registration()
        }

        return view
    }

    // Функция регистрации
    private fun registration() {

        // Переменные введенных данных
        val email = emailInput.text.toString().trim()
        val password = firstPasswordInput.text.toString().trim()
        val confirmPassword = secondPasswordInput.text.toString().trim()

        var isEmpty = false // Проверка заполнено ли поле

        // Проверка на заполнение поля email
        if (email.isEmpty()) {
            emailInput.error = "Введите Email"
            isEmpty = true
        }

        // Проверка на заполнение поля пароль
        if (password.isEmpty()) {
            firstPasswordInput.error = "Введите пароль"
            isEmpty = true
        }

        // Проверка на заполнение поля пароль
        if (confirmPassword.isEmpty()) {
            secondPasswordInput.error = "Введите подтверждение пароля"
            isEmpty = true
        }

        // Проверка на заполнение подтвержения пароля
        if (password != confirmPassword) {
            Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            isEmpty = true
        }

        // Если какое-то из полей пустое то выводим ошибку и выходим из функции
        if (isEmpty) {
            return
        }

        // Обработка ошибок регистрации и успешная регистрация
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        activity, "Регистрация прошла успешно",
                        Toast.LENGTH_SHORT
                    ).show()
                    val welcomeFragment = WelcomeFragment()
                    parentFragmentManager.beginTransaction() // Возврат на приветственный фрагмент
                        .replace(R.id.AuthorizationFragment, welcomeFragment)
                        .commit()
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            activity,
                            "Пользователь уже зарегистрирован в системе",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            activity, "Ошибка регистрации: " +
                                    task.exception?.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}