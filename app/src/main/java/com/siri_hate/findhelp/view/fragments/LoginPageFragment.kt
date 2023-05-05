package com.siri_hate.findhelp.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentLoginPageBinding
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.fragments.LoginPageViewModel
import com.siri_hate.findhelp.viewmodel.factory.LoginPageViewModelFactory


class LoginPageFragment : Fragment() {

    private lateinit var controller: NavController
    private lateinit var binding: FragmentLoginPageBinding

    private val viewModel: LoginPageViewModel by viewModels {
        LoginPageViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginPageBinding.inflate(inflater, container, false)

        controller = findNavController()

        setupListeners()
        observeErrorMessage()
        isUserauthorized()

        return binding.root
    }

    private fun isUserauthorized() {
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
    }

    private fun setupListeners() {
        binding.loginFragmentLoginButton.setOnClickListener {
            hideKeyboard()
            performLogin()
        }
        binding.loginFragmentRegistrationButton.setOnClickListener { navigateToRegistration() }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        view?.let { v ->
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }

    }

    private fun performLogin() {
        val email = binding.loginFragmentLoginInput.text.toString().trim()
        val password = binding.loginFragmentPasswordInput.text.toString().trim()

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
            binding.loginFragmentLoginInput.error = "Введите email"
            isEmpty = true
        }

        if (password.isEmpty()) {
            binding.loginFragmentPasswordInput.error = "Введите пароль"
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
        binding.loginFragmentRegistrationLoginProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingIndicator() {
        binding.loginFragmentRegistrationLoginProgressBar.visibility = View.INVISIBLE
    }
}
