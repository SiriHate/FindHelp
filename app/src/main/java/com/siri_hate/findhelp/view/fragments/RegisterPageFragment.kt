package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentRegisterPageBinding
import com.siri_hate.findhelp.model.models.Organization
import com.siri_hate.findhelp.model.models.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class RegisterPageFragment : Fragment() {

    companion object {
        private const val TAG = "RegisterPageFragment"
        private const val ORGANIZATION_COLLECTION = "organization_info"
        private const val ORGANIZATION_NAME = "organization_name"
        private const val ORGANIZATION_PHONE = "organization_phone"
        private const val USER_TYPE_USER = "user"
        private const val USER_TYPE_ORGANIZER = "organizer"
        private const val USER_RIGHTS_COLLECTION = "user_rights"
        private const val INIT_DATA_COLLECTION = "init_data"
        private const val BASE_SKILLS_INIT = "base_skills_init"
        private const val USER_CITY = "user_city"
        private const val USER_INFO_COLLECTION = "user_info"
    }

    private lateinit var controller: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var orgInfoCollection: CollectionReference
    private lateinit var binding: FragmentRegisterPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterPageBinding.inflate(inflater, container, false)

        controller = findNavController()
        firebaseAuth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        orgInfoCollection = db.collection(ORGANIZATION_COLLECTION)

        setupListeners()

        return binding.root
    }

    private fun setupListeners() {

        binding.registerFragmentRegisterButton.setOnClickListener {
            registration()
        }

        binding.registerFragmentUserTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.registerFragmentOrganaizerTypeChip.isChecked = false
                binding.registerFragmentOrganizerLayout.visibility = View.GONE
            }
        }

        binding.registerFragmentOrganaizerTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.registerFragmentUserTypeChip.isChecked = false
                binding.registerFragmentOrganizerLayout.visibility = View.VISIBLE
            }
        }
    }

    // Функция регистрации
    private fun registration() {
        val email = binding.registerFragmentEmailInput.text.toString().trim()
        val password = binding.registerFragmentPasswordInput.text.toString().trim()
        val confirmPassword = binding.registerFragmentPasswordInputRepeat.text.toString().trim()
        val organizationName = binding.registerFragmentOrganizationNameInput.text.toString().trim()
        val contactPerson = binding.registerFragmentContactPersonInput.text.toString().trim()
        val organizationPhone = binding.registerFragmentOrganizationPhoneInput.text.toString().trim()
        val isOrganizer = binding.registerFragmentOrganaizerTypeChip.isChecked

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
                    Toast.makeText(activity, "Название организации уже занято", Toast.LENGTH_SHORT).show()
                    return
                }
                if (isPhoneExists) {
                    Toast.makeText(activity, "Номер телефона уже занят", Toast.LENGTH_SHORT).show()
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

        var isValid = true

        if (email.isEmpty()) {
            binding.registerFragmentEmailInput.error = "Введите Email"
            isValid = false
        } else {
            binding.registerFragmentEmailInput.error = null
        }

        if (password.isEmpty()) {
            binding.registerFragmentPasswordInput.error = "Введите пароль"
            isValid = false
        } else {
            binding.registerFragmentPasswordInput.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.registerFragmentPasswordInputRepeat.error = "Введите подтверждение пароля"
            isValid = false
        } else {
            binding.registerFragmentPasswordInputRepeat.error = null
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                binding.registerFragmentOrganizationNameInput.error = "Введите название компании"
                isValid = false
            } else {
                binding.registerFragmentOrganizationNameInput.error = null
            }

            if (contactPerson.isEmpty()) {
                binding.registerFragmentContactPersonInput.error = "Введите контактное лицо"
                isValid = false
            } else {
                binding.registerFragmentContactPersonInput.error = null
            }

            if (organizationPhone.isEmpty()) {
                binding.registerFragmentOrganizationPhoneInput.error = "Введите телефон компании"
                isValid = false
            } else {
                binding.registerFragmentOrganizationPhoneInput.error = null
            }
        }

        return isValid
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
                    val organization = Organization(
                        organization_name = organizationName,
                        contact_person = contactPerson,
                        organization_phone = organizationPhone
                    )

                    db.collection(ORGANIZATION_COLLECTION).document(email).set(organization)
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