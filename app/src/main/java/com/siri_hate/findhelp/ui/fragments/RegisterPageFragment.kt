package com.siri_hate.findhelp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentRegisterPageBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.factories.RegisterPageViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.RegisterPageViewModel
import java.util.Locale

class RegisterPageFragment : Fragment() {

    private lateinit var binding: FragmentRegisterPageBinding
    private val navController: NavController by lazy { findNavController() }

    private val viewModel: RegisterPageViewModel by viewModels {
        RegisterPageViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterPageBinding.inflate(inflater, container, false)

        setupListeners()
        setupObservers()
        return binding.root
    }

    private fun setupListeners() {

        binding.registerFragmentRegisterButton.setOnClickListener {
            hideKeyboard()
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

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view?.let { v -> imm?.hideSoftInputFromWindow(v.windowToken, 0) }
    }

    private fun setupObservers() {

        viewModel.registrationSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    activity,
                    getString(R.string.user_registration_successfully_msg),
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(
                activity,
                getString(R.string.user_registration_error_msg),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.emailInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentEmailInput.error =
                    getString(R.string.reg_need_to_enter_email_msg)
            }
        }

        viewModel.passwordInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentPasswordInput.error =
                    getString(R.string.reg_need_to_enter_password_msg)
            }
        }

        viewModel.passwordConfirmInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentPasswordInputRepeat.error =
                    getString(R.string.reg_need_to_enter_password_repeat_msg)
            }
        }

        viewModel.organizationNameInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentOrganizationNameInput.error =
                    getString(R.string.reg_need_to_enter_org_name_msg)
            }
        }

        viewModel.contactPersonInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentContactPersonInput.error =
                    getString(R.string.reg_need_to_enter_contact_person_msg)
            }
        }

        viewModel.organizationPhoneInputError.observe(viewLifecycleOwner) { errorStatus ->
            if (errorStatus) {
                binding.registerFragmentOrganizationPhoneInput.error =
                    getString(R.string.reg_need_to_enter_org_phone_msg)
            }
        }

    }

    private fun registration() {
        val email = binding.registerFragmentEmailInput.text.toString().trim().lowercase(Locale.ROOT)
        val password = binding.registerFragmentPasswordInput.text.toString().trim()
        val confirmPassword = binding.registerFragmentPasswordInputRepeat.text.toString().trim()
        val organizationName = binding.registerFragmentOrganizationNameInput.text.toString().trim()
        val contactPerson = binding.registerFragmentContactPersonInput.text.toString().trim()
        val organizationPhone =
            binding.registerFragmentOrganizationPhoneInput.text.toString().trim()
        val isOrganizer = binding.registerFragmentOrganaizerTypeChip.isChecked

        viewModel.handleRegistration(
            email,
            password,
            confirmPassword,
            isOrganizer,
            organizationName,
            contactPerson,
            organizationPhone
        )
    }
}