package com.siri_hate.findhelp.ui.fragments

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
import com.siri_hate.findhelp.databinding.FragmentLoginPageBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.fragments.LoginPageViewModel
import com.siri_hate.findhelp.ui.viewmodels.factories.LoginPageViewModelFactory


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
        setupObservers()
        viewModel.isUserauthorized()

        return binding.root
    }


    private fun setupListeners() {
        binding.loginFragmentLoginButton.setOnClickListener {
            hideKeyboard()
            performLogin()
        }
        binding.loginFragmentRegistrationButton.setOnClickListener {
            viewModel.navigateToRegistration(controller)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view?.let { v -> imm?.hideSoftInputFromWindow(v.windowToken, 0) }
    }

    private fun performLogin() {
        val email = binding.loginFragmentLoginInput.text.toString().trim()
        val password = binding.loginFragmentPasswordInput.text.toString().trim()

        viewModel.login(email, password)
    }

    private fun setupObservers() {

        viewModel.toastMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(viewLifecycleOwner) { loadingStatus ->
            if (loadingStatus) {
                showLoadingIndicator()
            } else {
                hideLoadingIndicator()
            }
        }

        viewModel.destinationPage.observe(viewLifecycleOwner) { page ->
            viewModel.startUserPageFragment(controller, page)
        }

        viewModel.emailInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.loginFragmentLoginInput.error = "Введите email"
            }
        }

        viewModel.passwordInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.loginFragmentPasswordInput.error = "Введите пароль"
            }
        }

    }

    private fun showLoadingIndicator() {
        binding.loginFragmentRegistrationLoginProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingIndicator() {
        binding.loginFragmentRegistrationLoginProgressBar.visibility = View.INVISIBLE
    }

}
