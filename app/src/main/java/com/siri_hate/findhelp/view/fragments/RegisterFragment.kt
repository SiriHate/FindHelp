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
    private lateinit var registerFragmentEmailInput: EditText
    private lateinit var registerFragmentPasswordInput: EditText
    private lateinit var registerFragmentPasswordInputRepeat: EditText
    private lateinit var registerFragmentRegisterButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var registerFragmentUserTypeChip: Chip
    private lateinit var registerFragmentOrganaizerTypeChip: Chip
    private lateinit var registerFragmentGoBackButton: ImageButton
    private lateinit var registerFragmentOrganizationNameInput: EditText
    private lateinit var registerFragmentContactPersonInput: EditText
    private lateinit var registerFragmentOrganizationPhoneInput: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.register_fragment, container, false)

        // Переменные UI-элементов
        registerFragmentEmailInput = view.findViewById(R.id.register_fragment_email_input)
        registerFragmentPasswordInput = view.findViewById(R.id.register_fragment_password_input)
        registerFragmentPasswordInputRepeat =
            view.findViewById(R.id.register_fragment_password_input_repeat)
        registerFragmentRegisterButton = view.findViewById(R.id.register_fragment_register_button)
        firebaseAuth = FirebaseAuth.getInstance()
        registerFragmentUserTypeChip = view.findViewById(R.id.register_fragment_user_type_chip)
        registerFragmentOrganaizerTypeChip =
            view.findViewById(R.id.register_fragment_organaizer_type_chip)
        registerFragmentGoBackButton = view.findViewById(R.id.register_fragment_go_back_button)
        registerFragmentOrganizationNameInput =
            view.findViewById(R.id.register_fragment_organization_name_input)
        registerFragmentContactPersonInput =
            view.findViewById(R.id.register_fragment_Contact_person_input)
        registerFragmentOrganizationPhoneInput =
            view.findViewById(R.id.register_fragment_organization_phone_input)

        registerFragmentGoBackButton.setOnClickListener {
            goBack()
        }

        // Слушатель кнопки регистрации вызывает функцию регистрации
        registerFragmentRegisterButton.setOnClickListener {
            registration()
        }

        // Слушатель для чипа UserType
        registerFragmentUserTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                registerFragmentOrganaizerTypeChip.isChecked = false
                view?.findViewById<View>(R.id.register_fragment_organizer_layout)?.visibility =
                    View.GONE
            }
        }

        // Слушатель для чипа OrganizerType
        registerFragmentOrganaizerTypeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                registerFragmentUserTypeChip.isChecked = false
                view?.findViewById<View>(R.id.register_fragment_organizer_layout)?.visibility =
                    View.VISIBLE
            }
        }

        return view
    }

    // Функция возврата на экран авторизации
    private fun goBack() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.authorization_fragment_layout, LoginFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Функция регистрации
    private fun registration() {
        val email = registerFragmentEmailInput.text.toString().trim()
        val password = registerFragmentPasswordInput.text.toString().trim()
        val confirmPassword = registerFragmentPasswordInputRepeat.text.toString().trim()
        val organizationName = registerFragmentOrganizationNameInput.text.toString().trim()
        val contactPerson = registerFragmentContactPersonInput.text.toString().trim()
        val organizationPhone = registerFragmentOrganizationPhoneInput.text.toString().trim()
        val isOrganizer = registerFragmentOrganaizerTypeChip.isChecked

        if (inputCheck(
                email, password, confirmPassword, isOrganizer,
                organizationName, contactPerson, organizationPhone
            )
        ) {
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
            registerFragmentEmailInput.error = "Введите Email"
            isValid = false
        }

        if (password.isEmpty()) {
            registerFragmentPasswordInput.error = "Введите пароль"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            registerFragmentPasswordInputRepeat.error = "Введите подтверждение пароля"
            isValid = false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                registerFragmentOrganizationNameInput.error = "Введите название компании"
                isValid = false
            }

            if (contactPerson.isEmpty()) {
                registerFragmentContactPersonInput.error = "Введите контактное лицо"
                isValid = false
            }

            if (organizationPhone.isEmpty()) {
                registerFragmentOrganizationPhoneInput.error = "Введите телефон компании"
                isValid = false
            }
        }

        return isValid
    }

    // Получение типа пользователя
    private fun getUserType(): String {
        return if (registerFragmentUserTypeChip.isChecked) "user" else "organizer"
    }

    // Функция регистрации пользователя в базу данных
    private fun registerUser(email: String, password: String, userType: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    registrationSuccess(userType, email)

                    if (userType == "organizer") {
                        val db = Firebase.firestore
                        val organizationName =
                            view?.findViewById<EditText>(R.id.register_fragment_organization_name_input)?.text.toString()
                        val contactPerson =
                            view?.findViewById<EditText>(R.id.register_fragment_Contact_person_input)?.text.toString()
                        val organizationPhone =
                            view?.findViewById<EditText>(R.id.register_fragment_organization_phone_input)?.text.toString()

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
        fragmentManager.beginTransaction().replace(
            R.id.authorization_fragment_layout,
            loginFragment
        ).commit()
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
        val userRef = db.collection("user_rights").document(email)
        userRef.set(user)
    }

    // Функция создания нового документа для пользователей типа "user" от базового списка навыков
    private fun initUserSkillsList(userType: String, email: String) {
        if (userType == "user") {
            val db = FirebaseFirestore.getInstance()
            val baseSkillsRef = db.collection("init_data").document("base_skills_init")
            baseSkillsRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val baseSkillsData = documentSnapshot.data
                        val userSkillRef = db.collection("user_skills").document(email)
                        userSkillRef.set(baseSkillsData!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "Документ успешно создан!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Ошибка при создании документа", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Ошибка при чтении документа base_skills_init", e)
                }
        }
    }
}