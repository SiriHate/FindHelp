package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.User

class RegisterFragment : Fragment() {

    // Переменные
    private lateinit var emailInput: EditText
    private lateinit var firstPasswordInput: EditText
    private lateinit var secondPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userTypeChip: Chip
    private lateinit var organizerTypeChip: Chip

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
        userTypeChip = view.findViewById(R.id.User_type)
        organizerTypeChip = view.findViewById(R.id.Organaizer_type)

        // Слушатель кнопки регистрации вызывает функцию регистрации
        registerButton.setOnClickListener {
            registration()
        }

        // Слушатель для чипа UserType
        userTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                organizerTypeChip.isChecked = false
            }
        }

        // Слушатель для чипа OrganizerType
        organizerTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userTypeChip.isChecked = false
            }
        }

        return view
    }

    // Функция регистрации
    private fun registration() {
        val email = emailInput.text.toString().trim()
        val password = firstPasswordInput.text.toString().trim()
        val confirmPassword = secondPasswordInput.text.toString().trim()

        if (validateInputs(email, password, confirmPassword)) {
            val userType = getUserType()
            registerUser(email, password, userType)
        }
    }

    // Функция проверки на заполненость полей и выбор типа пользователя
    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailInput.error = "Введите Email"
            isValid = false
        }

        if (password.isEmpty()) {
            firstPasswordInput.error = "Введите пароль"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            secondPasswordInput.error = "Введите подтверждение пароля"
            isValid = false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!userTypeChip.isChecked && !organizerTypeChip.isChecked) {
            Toast.makeText(activity, "Тип пользователя не выбран", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    // Получение типа пользователя
    private fun getUserType(): String {
        return if (userTypeChip.isChecked) "user" else "organizer"
    }

    // Функция регистрации пользователя в базу данных
    private fun registerUser(email: String, password: String, userType: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Регистрация прошла успешно",
                        Toast.LENGTH_SHORT).show()
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let {
                        FirebaseDatabase.getInstance().reference
                            .child("users").child(it.uid)
                            .setValue(User(uid = it.uid, userType = userType))
                    }
                    val welcomeFragment = WelcomeFragment()
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction().replace(R.id.AuthorizationFragment,
                        welcomeFragment).commit()
                } else {
                    Toast.makeText(
                        activity,
                        "Ошибка регистрации: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


}