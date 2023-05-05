package com.siri_hate.findhelp.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentRegisterPageBinding
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.factory.RegisterPageViewModelFactory
import com.siri_hate.findhelp.viewmodel.fragments.RegisterPageViewModel
import kotlinx.coroutines.runBlocking
import java.util.Locale

class RegisterPageFragment : Fragment() {

    private lateinit var binding: FragmentRegisterPageBinding
    private lateinit var viewModel: RegisterPageViewModel
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterPageBinding.inflate(inflater, container, false)

        val factory = RegisterPageViewModelFactory(FirebaseAuthModel(), FirebaseFirestoreModel())
        viewModel = ViewModelProvider(this, factory)[RegisterPageViewModel::class.java]

        setupListeners()
        observeViewModel()
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

        view?.let { v ->
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }

    }

    private fun observeViewModel() {
        viewModel.registrationSuccessLiveData.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    activity,
                    getString(R.string.user_registration_successfully_msg),
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        viewModel.registrationErrorLiveData.observe(viewLifecycleOwner) {
            Toast.makeText(
                activity,
                getString(R.string.user_registration_error_msg),
                Toast.LENGTH_SHORT
            ).show()
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

        val inputErrors = viewModel.inputCheck(
            email,
            password,
            confirmPassword,
            isOrganizer,
            organizationName,
            contactPerson,
            organizationPhone
        )

        if (inputErrors.isNotEmpty()) {
            inputErrors.forEach { entry ->
                val (fieldName, error) = entry
                if (error) {
                    when (fieldName) {
                        "email" -> binding.registerFragmentEmailInput.error =
                            getString(R.string.reg_need_to_enter_email_msg)

                        "password" -> binding.registerFragmentPasswordInput.error =
                            getString(R.string.reg_need_to_enter_password_msg)

                        "confirmPassword" -> binding.registerFragmentPasswordInputRepeat.error =
                            getString(R.string.reg_need_to_enter_password_repeat_msg)

                        "organizationName" -> binding.registerFragmentOrganizationNameInput.error =
                            getString(R.string.reg_need_to_enter_org_name_msg)

                        "contactPerson" -> binding.registerFragmentContactPersonInput.error =
                            getString(R.string.reg_need_to_enter_contact_person_msg)

                        "organizationPhone" -> binding.registerFragmentOrganizationPhoneInput.error =
                            getString(R.string.reg_need_to_enter_org_phone_msg)
                    }
                }
            }

            return
        }

        if (isOrganizer) {
            runBlocking {
                val isNameExists = viewModel.checkOrganizationNameExists(organizationName)
                val isPhoneExists = viewModel.checkOrganizationPhoneExists(organizationPhone)

                if (isNameExists) {
                    Toast.makeText(
                        activity,
                        getString(R.string.reg_need_to_enter_org_name_already_exists_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@runBlocking
                }
                if (isPhoneExists) {
                    Toast.makeText(
                        activity,
                        getString(R.string.reg_need_to_enter_org_phone_already_exists_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runBlocking
                }

                viewModel.registerOrganizer(
                    email,
                    password,
                    organizationName,
                    contactPerson,
                    organizationPhone
                )
            }
        } else {
            viewModel.registerUser(email, password)
        }
    }
}