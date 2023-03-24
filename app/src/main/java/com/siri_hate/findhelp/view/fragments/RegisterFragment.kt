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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private lateinit var organizationNameInput: EditText
    private lateinit var contactPersonInput: EditText
    private lateinit var organizationPhoneInput: EditText
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
        organizationNameInput = view.findViewById(R.id.Organization_name_input)
        contactPersonInput = view.findViewById(R.id.Contact_person_input)
        organizationPhoneInput = view.findViewById(R.id.Organization_phone_input)

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
                view?.findViewById<View>(R.id.OrganizerLayout)?.visibility = View.GONE
            }
        }

        // Слушатель для чипа OrganizerType
        organizerTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userTypeChip.isChecked = false
                view?.findViewById<View>(R.id.OrganizerLayout)?.visibility = View.VISIBLE
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
        val organizationName = organizationNameInput.text.toString().trim()
        val contactPerson = contactPersonInput.text.toString().trim()
        val organizationPhone = organizationPhoneInput.text.toString().trim()
        val isOrganizer = organizerTypeChip.isChecked

        if (inputCheck(email, password, confirmPassword, isOrganizer,
                organizationName, contactPerson, organizationPhone)) {
            val userType = getUserType()
            registerUser(email, password, userType)
        }
    }

    // Функция проверки на заполненость полей и выбор типа пользователя
    private fun inputCheck(
        email: String,
        password: String,
        confirmPassword: String,
        isOrganizer: Boolean,
        organizationName: String,
        contactPerson: String,
        organizationPhone: String
    ): Boolean {
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

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                organizationNameInput.error = "Введите название компании"
                isValid = false
            }

            if (contactPerson.isEmpty()) {
                contactPersonInput.error = "Введите контактное лицо"
                isValid = false
            }

            if (organizationPhone.isEmpty()) {
                organizationPhoneInput.error = "Введите телефон компании"
                isValid = false
            }
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

                    if (userType == "organizer") {
                        val db = Firebase.firestore
                        val organizationName = view?.findViewById<EditText>(R.id.Organization_name_input)?.text.toString()
                        val contactPerson = view?.findViewById<EditText>(R.id.Contact_person_input)?.text.toString()
                        val organizationPhone = view?.findViewById<EditText>(R.id.Organization_phone_input)?.text.toString()

                        // Сохраняем данные в Firestore
                        val data = hashMapOf(
                            "organization_name" to organizationName,
                            "contact_person" to contactPerson,
                            "organization_phone" to organizationPhone
                        )

                        db.collection("organization_info").document(email).set(data)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot added with ID: $email")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    }
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