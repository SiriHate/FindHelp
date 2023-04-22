package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class RegisterPageFragment : Fragment() {

    companion object {
        private const val TAG = "RegisterPageFragment"
        private const val ORGANIZATION_COLLECTION = "organization_info"
        private const val ORGANIZATION_NAME = "organization_name"
        private const val ORGANIZATION_PHONE = "organization_phone"
        private const val CONTACT_PERSON = "contact_person"
        private const val USER_TYPE_USER = "user"
        private const val USER_TYPE_ORGANIZER = "organizer"
        private const val USER_RIGHTS_COLLECTION = "user_rights"
        private const val INIT_DATA_COLLECTION = "init_data"
        private const val BASE_SKILLS_INIT = "base_skills_init"
        private const val USER_CITY = "user_city"
        private const val USER_INFO_COLLECTION = "user_info"
    }


    // Переменные
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordInputRepeat: EditText
    private lateinit var registerButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userTypeChip: Chip
    private lateinit var organizerTypeChip: Chip
    private lateinit var organizationNameInput: EditText
    private lateinit var contactPersonInput: EditText
    private lateinit var organizationPhoneInput: EditText
    private lateinit var organizerLayout: LinearLayout
    private lateinit var controller: NavController

    private lateinit var db: FirebaseFirestore
    private lateinit var orgInfoCollection: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_page, container, false)

        controller = findNavController()
        firebaseAuth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        orgInfoCollection = db.collection(ORGANIZATION_COLLECTION)

        initViews(view)
        setupListeners()

        return view
    }

    private fun initViews(view: View) {
        emailInput = view.findViewById(R.id.register_fragment_email_input)
        passwordInput = view.findViewById(R.id.register_fragment_password_input)
        passwordInputRepeat = view.findViewById(R.id.register_fragment_password_input_repeat)
        registerButton = view.findViewById(R.id.register_fragment_register_button)
        userTypeChip = view.findViewById(R.id.register_fragment_user_type_chip)
        organizerTypeChip = view.findViewById(R.id.register_fragment_organaizer_type_chip)
        organizationNameInput = view.findViewById(R.id.register_fragment_organization_name_input)
        contactPersonInput = view.findViewById(R.id.register_fragment_Contact_person_input)
        organizationPhoneInput = view.findViewById(R.id.register_fragment_organization_phone_input)
        organizerLayout = view.findViewById(R.id.register_fragment_organizer_layout)
    }

    private fun setupListeners() {

        registerButton.setOnClickListener {
            registration()
        }

        userTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                organizerTypeChip.isChecked = false
                organizerLayout.visibility = View.GONE
            }
        }

        organizerTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userTypeChip.isChecked = false
                organizerLayout.visibility = View.VISIBLE
            }
        }
    }

    // Функция регистрации
    private fun registration() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = passwordInputRepeat.text.toString().trim()
        val organizationName = organizationNameInput.text.toString().trim()
        val contactPerson = contactPersonInput.text.toString().trim()
        val organizationPhone = organizationPhoneInput.text.toString().trim()
        val isOrganizer = organizerTypeChip.isChecked

        if (
            inputCheck(
                email, password, confirmPassword, isOrganizer,
                organizationName, contactPerson, organizationPhone
            )
        ) {
            if (isOrganizer) {
                val isNameExists = runBlocking { checkOrganizationNameExists(organizationName) }
                val isPhoneExists = runBlocking { checkOrganizationPhoneExists(organizationPhone) }
                if (isNameExists) {
                    organizationNameInput.error = "Название организации уже занято"
                    return
                }
                if (isPhoneExists) {
                    organizationPhoneInput.error = "Номер телефона уже занят"
                    return
                }

                registerOrganizer(email, password, organizationName, contactPerson, organizationPhone)
            } else {
                registerUser(email, password)
            }
        }
    }

    private suspend fun checkOrganizationNameExists(organizationName: String): Boolean {
        val querySnapshot =
            orgInfoCollection.whereEqualTo(ORGANIZATION_NAME, organizationName).get().await()
        return querySnapshot.documents.isNotEmpty()
    }

    private suspend fun checkOrganizationPhoneExists(organizationPhone: String): Boolean {
        val querySnapshot =
            orgInfoCollection.whereEqualTo(ORGANIZATION_PHONE, organizationPhone).get().await()
        return querySnapshot.documents.isNotEmpty()
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
        if (email.isEmpty()) {
            emailInput.error = "Введите Email"
            return false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Введите пароль"
            return false
        }

        if (confirmPassword.isEmpty()) {
            passwordInputRepeat.error = "Введите подтверждение пароля"
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return false
        }

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                organizationNameInput.error = "Введите название компании"
                return false
            }

            if (contactPerson.isEmpty()) {
                contactPersonInput.error = "Введите контактное лицо"
                return false
            }

            if (organizationPhone.isEmpty()) {
                organizationPhoneInput.error = "Введите телефон компании"
                return false
            }
        }

        return true
    }

    // Функция регистрации пользователя в базу данных
    private fun registerUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    registrationSuccess(USER_TYPE_USER, email)
                } else {
                    registrationError(task.exception?.message)
                }
            }
    }

    private fun registerOrganizer(
        email: String,
        password: String,
        organizationName: String,
        contactPerson: String,
        organizationPhone: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    registrationSuccess(USER_TYPE_ORGANIZER, email)

                    val db = Firebase.firestore

                    // Сохраняем данные в Firestore
                    val data = hashMapOf(
                        ORGANIZATION_NAME to organizationName,
                        CONTACT_PERSON to contactPerson,
                        ORGANIZATION_PHONE to organizationPhone
                    )

                    db.collection(ORGANIZATION_COLLECTION).document(email).set(data)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: $email")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
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
            initUserInfo(userType, email)
        }

        controller.navigate(R.id.action_registerFragment_to_loginFragment)
    }

    // Функция обработки ошибок регистрации
    private fun registrationError(errorMessage: String?) {
        Toast.makeText(
            activity,
            "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT
        ).show()
    }

    // Функция установки прав доступа пользователя
    private fun setUserAccessRights(userType: String, email: String, uid: String) {
        val user = User(uid = uid, userType = userType)
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection(USER_RIGHTS_COLLECTION).document(email)
        userRef.set(user)
    }

    // Функция создания нового документа для пользователей типа "user" от базового списка навыков
    private fun initUserInfo(userType: String, email: String) {
        if (userType == USER_TYPE_USER) {
            val db = FirebaseFirestore.getInstance()
            val baseSkillsRef = db.collection(INIT_DATA_COLLECTION).document(BASE_SKILLS_INIT)
            baseSkillsRef.get().addOnSuccessListener { documentSnapshot ->
                val baseSkillsData = documentSnapshot.data
                val userData = HashMap<String, Any>()
                userData.putAll(baseSkillsData ?: return@addOnSuccessListener)
                userData[USER_CITY] = "Не выбрано"
                db.collection(USER_INFO_COLLECTION).document(email)
                    .set(userData)
                    .addOnSuccessListener { Log.d(TAG, "Документ успешно создан!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Ошибка при создании документа", e) }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Ошибка при чтении документа base_skills_init", e)
            }
        }
    }
}