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
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.viewmodel.fragments.LoginPageViewModel
import com.siri_hate.findhelp.viewmodel.factory.LoginPageViewModelFactory


class LoginPageFragment : Fragment() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var controller: NavController

    private val viewModel: LoginPageViewModel by viewModels {
        LoginPageViewModelFactory(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_page, container, false)

        controller = findNavController()

        initViews(view)
        setupListeners()
        observeErrorMessage()

        viewModel.checkUserAccess()?.let { currentUser ->
            showLoadingIndicator()
            currentUser.email?.let { userEmail ->
                viewModel.getUserTypeFromFirestore(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userType = task.result?.getString("userType")
                        viewModel.startUserPageFragment(controller, userType)
                    } else {
                        viewModel.showErrorMessage("Ошибка доступа к базе данных: " + task.exception?.message)
                    }
                    hideLoadingIndicator()
                }
            }
        }

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

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (inputFieldsAreEmpty(email, password)) {
            return
        }

        showLoadingIndicator()
        viewModel.performLogin(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                val userEmail = user?.email

                userEmail?.let { email ->
                    viewModel.getUserTypeFromFirestore(email).addOnCompleteListener { userTypeTask ->
                        if (userTypeTask.isSuccessful) {
                            val userType = userTypeTask.result?.getString("userType")
                            viewModel.startUserPageFragment(controller, userType)
                        } else {
                            viewModel.showErrorMessage("Ошибка доступа к базе данных: " + userTypeTask.exception?.message)
                        }
                        hideLoadingIndicator()
                    }
                }
            } else {
                viewModel.showErrorMessage("Ошибка авторизации: " + task.exception?.message)
                hideLoadingIndicator()
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

    private fun observeErrorMessage() {
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoadingIndicator() {
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingIndicator() {
        loadingProgressBar.visibility = View.INVISIBLE
    }
}
