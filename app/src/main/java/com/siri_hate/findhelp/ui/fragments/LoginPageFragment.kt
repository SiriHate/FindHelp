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
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentLoginPageBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.fragments.LoginPageViewModel
import com.siri_hate.findhelp.ui.viewmodels.factories.LoginPageViewModelFactory


class LoginPageFragment : Fragment() {

    private lateinit var controller: NavController
    private lateinit var binding: FragmentLoginPageBinding

    companion object {
        const val USER_PAGE = "user"
        const val ORGANIZER_PAGE = "organizer"
        const val MODERATOR_PAGE = "moderator"

    }

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
        viewModel.isUserAuthorized()

        return binding.root
    }

    private fun setupListeners() {
        binding.loginFragmentLoginButton.setOnClickListener {
            hideKeyboard()
            performLogin()
        }
        binding.loginFragmentRegistrationButton.setOnClickListener {
            controller.navigate(R.id.action_loginFragment_to_registerFragment)
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

        viewModel.toastMessage.observe(viewLifecycleOwner) { errorMessageType ->
            when (errorMessageType) {
                "db_error" -> Toast.makeText(requireContext(), R.string.login_db_error_message, Toast.LENGTH_SHORT).show()
                "login_error" -> Toast.makeText(requireContext(), R.string.login_error_msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loadingStatus ->
            if (loadingStatus) {
                showLoadingIndicator()
            } else {
                hideLoadingIndicator()
            }
        }

        viewModel.destinationPage.observe(viewLifecycleOwner) { dest_page ->
            when (dest_page) {
                USER_PAGE -> controller.navigate(R.id.action_loginFragment_to_userPageFragment)
                ORGANIZER_PAGE -> controller.navigate(R.id.action_loginFragment_to_organizerPageFragment)
                MODERATOR_PAGE -> controller.navigate(R.id.action_loginFragment_to_moderatorPageFragment)
                else -> Toast.makeText(
                    requireContext(),
                    R.string.login_cant_determine_user_rights_msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.emailInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.loginFragmentLoginInput.error = getString(R.string.login_need_to_enter_email_msg)
            }
        }

        viewModel.passwordInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.loginFragmentPasswordInput.error = getString(R.string.login_need_to_enter_password_msg)
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
