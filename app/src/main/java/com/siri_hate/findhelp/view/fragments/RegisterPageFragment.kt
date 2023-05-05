package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private fun observeViewModel() {
        viewModel.registrationSuccessLiveData.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(activity, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        viewModel.registrationErrorLiveData.observe(viewLifecycleOwner) {
            Toast.makeText(activity, "Ошибка регистрации: $it", Toast.LENGTH_SHORT).show()
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
                        "email" -> binding.registerFragmentEmailInput.error = "Введите email"
                        "password" -> binding.registerFragmentPasswordInput.error = "Введите пароль"
                        "confirmPassword" -> binding.registerFragmentPasswordInputRepeat.error =
                            "Подтвердите пароль"

                        "organizationName" -> binding.registerFragmentOrganizationNameInput.error =
                            "Введите название организации"

                        "contactPerson" -> binding.registerFragmentContactPersonInput.error =
                            "Введите контактное лицо"

                        "organizationPhone" -> binding.registerFragmentOrganizationPhoneInput.error =
                            "Введите номер телефона"
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
                    Toast.makeText(activity, "Название организации уже занято", Toast.LENGTH_SHORT)
                        .show()
                    return@runBlocking
                }
                if (isPhoneExists) {
                    Toast.makeText(activity, "Номер телефона уже занят", Toast.LENGTH_SHORT).show()
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