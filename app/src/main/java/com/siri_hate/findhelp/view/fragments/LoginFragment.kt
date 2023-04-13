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


    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var registerTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var controller: NavController
    private lateinit var db: FirebaseFirestore

    companion object {
        const val USER_TYPE_USER = "user"
        const val USER_TYPE_ORGANIZER = "organizer"
        const val USER_TYPE_MODERATOR = "moderator"
        const val USER_RIGHTS_COLLECTION = "user_rights"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_page, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        controller = findNavController()

        initViews(view)
        setupListeners()
        checkUserAccess()

        return view
    }

    private fun initViews(view: View) {
        emailInput = view.findViewById(R.id.login_fragment_login_input)
        passwordInput = view.findViewById(R.id.login_fragment_password_input)
        loginButton = view.findViewById(R.id.login_fragment_login_button)
        registerTextView = view.findViewById(R.id.login_fragment_registration_button)
        loadingProgressBar = view.findViewById(R.id.login_fragment_registration_login_progress_bar)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener { performLogin() }
        registerTextView.setOnClickListener { navigateToRegistration() }
    }

    private fun checkUserAccess() {
        firebaseAuth.currentUser?.let { currentUser ->
            showLoadingIndicator()
            currentUser.email?.let { userEmail ->
                getUserTypeFromFirestore(userEmail)
            }
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (inputFieldsAreEmpty(email, password)) {
            return
        }

        showLoadingIndicator()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginSuccessful()
                } else {
                    loginFailure(task.exception)
                }
            }
    }

    private fun inputFieldsAreEmpty(email: String, password: String): Boolean {
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

    private fun navigateToRegistration() {
        controller.navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun getUserTypeFromFirestore(userEmail: String) {
        db.collection(USER_RIGHTS_COLLECTION).document(userEmail).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userType = task.result?.getString("userType")
                    checkUserType(userType)
                } else {
                    showErrorMessage("Ошибка доступа к базе данных: " + task.exception?.message)
                }
            }
    }

    private fun checkUserType(userType: String?) {
        hideLoadingIndicator()
        if (!userType.isNullOrEmpty()) {
            startUserPageFragment(userType)
        } else {
            showErrorMessage("Не удалось определить права доступа")
        }
    }

    private fun loginSuccessful() {
        val user = firebaseAuth.currentUser
        val email = user?.email

        email?.let { userEmail ->
            getUserTypeFromFirestore(userEmail)
        }
    }

    private fun loginFailure(exception: Exception?) {
        hideLoadingIndicator()
        if (exception is FirebaseAuthInvalidCredentialsException) {
            showErrorMessage("Неверный email или пароль")
        } else {
            showErrorMessage("Ошибка авторизации: " + exception?.message)
        }
    }

    private fun startUserPageFragment(userType: String?) {
        when (userType) {
            USER_TYPE_USER -> controller.navigate(R.id.action_loginFragment_to_userPageFragment)
            USER_TYPE_ORGANIZER -> controller.navigate(R.id.action_loginFragment_to_organizerPageFragment)
            USER_TYPE_MODERATOR -> controller.navigate(R.id.action_loginFragment_to_moderatorPageFragment)
            else -> showErrorMessage("Не удалось определить права доступа")
        }
    }

    private fun showLoadingIndicator() {
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingIndicator() {
        loadingProgressBar.visibility = View.INVISIBLE
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
