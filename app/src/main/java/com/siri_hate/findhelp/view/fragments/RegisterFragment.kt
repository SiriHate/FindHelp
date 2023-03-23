package com.siri_hate.findhelp.view.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var goBackButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.register_fragment, container, false)

        // Переменные UI-элементов
        emailInput = view.findViewById(R.id.Email_input_register)
        firstPasswordInput = view.findViewById(R.id.First_password_input_register)
        secondPasswordInput = view.findViewById(R.id.Second_password_input_register)
        registerButton = view.findViewById(R.id.Register_button)
        firebaseAuth = FirebaseAuth.getInstance()
        userTypeChip = view.findViewById(R.id.User_type)
        organizerTypeChip = view.findViewById(R.id.Organaizer_type)
        goBackButton = view.findViewById(R.id.Go_back_button)

        goBackButton.setOnClickListener {
            goBack()
        }

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

    // Функция возврата на экран авторизации
    private fun goBack() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.AuthorizationFragment, LoginFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Функция регистрации
    private fun registration() {
        val email = emailInput.text.toString().trim()
        val password = firstPasswordInput.text.toString().trim()
        val confirmPassword = secondPasswordInput.text.toString().trim()

        if (inputCheck(email, password, confirmPassword)) {
            val userType = getUserType()
            registerUser(email, password, userType)
        }
    }

    // Функция проверки на заполненость полей и выбор типа пользователя
    private fun inputCheck(email: String, password: String, confirmPassword: String): Boolean {
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
                    registrationSuccess(userType, email)
                } else {
                    registrationError(task.exception?.message)
                }
            }
    }

    // Функция обработки успешной регистрации
    private fun registrationSuccess(userType: String, email: String) {
        Toast.makeText(activity, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            setUserAccessRights(userType, email, it.uid)
            initUserSkillsList(userType, email)
        }

        val loginFragment = LoginFragment()
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.AuthorizationFragment,
            loginFragment).commit()
    }

    // Функция обработки ошибок регистрации
    private fun registrationError(errorMessage: String?) { Toast.makeText(activity,
            "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    // Функция установки прав доступа пользователя
    private fun setUserAccessRights(userType: String, email: String, uid: String) {
        val user = User(uid = uid, userType = userType)
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("user_rights").document(email)
        userRef.set(user)
    }

    // Функция создания нового документа для пользователей типа "user" от базового списка навыков
    private fun initUserSkillsList(userType: String, email: String) {
        if (userType == "user") {
            val db = FirebaseFirestore.getInstance()
            val skillListInitRef = db.
            collection("user_skills").document("skill_list_init")
            skillListInitRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val skillListData = documentSnapshot.data
                        val userSkillRef = db.collection("user_skills").document(email)
                        userSkillRef.set(skillListData!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "Документ успешно создан!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Ошибка при создании документа", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Ошибка при чтении документа skill_list_init", e)
                }
        }
    }


}