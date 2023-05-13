package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Organization
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class RegisterPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    companion object {
        private const val USER_TYPE_USER = "user"
        private const val USER_TYPE_ORGANIZER = "organizer"
    }

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    private val _toastMessage = MutableLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _emailInputError = MutableLiveData<Boolean>()
    val emailInputError: LiveData<Boolean> = _emailInputError

    private val _passwordInputError = MutableLiveData<Boolean>()
    val passwordInputError: LiveData<Boolean> = _passwordInputError

    private val _passwordConfirmInputError = MutableLiveData<Boolean>()
    val passwordConfirmInputError: LiveData<Boolean> = _passwordConfirmInputError

    private val _organizationNameInputError = MutableLiveData<Boolean>()
    val organizationNameInputError: LiveData<Boolean> = _organizationNameInputError

    private val _contactPersonInputError = MutableLiveData<Boolean>()
    val contactPersonInputError: LiveData<Boolean> = _contactPersonInputError

    private val _organizationPhoneInputError = MutableLiveData<Boolean>()
    val organizationPhoneInputError: LiveData<Boolean> = _organizationPhoneInputError

    private fun registerUser(email: String, password: String) {
        firebaseAuthModel.performRegistration(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                registrationSuccess(USER_TYPE_USER, email)
            } else {
                registrationError()
            }
        }
    }

    fun handleRegistration(
        email: String,
        password: String,
        confirmPassword: String,
        isOrganizer: Boolean,
        organizationName: String,
        contactPerson: String,
        organizationPhone: String
    ) {
        if (!validateInputs(
                email,
                password,
                confirmPassword,
                isOrganizer,
                organizationName,
                contactPerson,
                organizationPhone
            )
        ) {
            return
        } else {
            if (isOrganizer) {
                runBlocking {

                    val isNameExists = checkOrganizationNameExists(organizationName)
                    val isPhoneExists = checkOrganizationPhoneExists(organizationPhone)

                    if (isNameExists) {
                        _toastMessage.postValue(R.string.reg_need_to_enter_org_name_already_exists_msg)
                        return@runBlocking
                    }
                    if (isPhoneExists) {
                        _toastMessage.postValue(R.string.reg_need_to_enter_org_phone_already_exists_msg)
                        return@runBlocking
                    }

                    registerOrganizer(
                        email,
                        password,
                        organizationName,
                        contactPerson,
                        organizationPhone
                    )
                }
            } else {
                registerUser(email, password)
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
        firebaseAuthModel.performRegistration(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                registrationSuccess(USER_TYPE_ORGANIZER, email)

                val organization = Organization(
                    organization_name = organizationName,
                    contact_person = contactPerson,
                    organization_phone = organizationPhone
                )

                firestoreModel.setDocument(
                    "organization_info",
                    email,
                    organization,
                    onSuccess = {},
                    onFailure = {})
            } else {
                registrationError()
            }
        }
    }

    private fun registrationSuccess(userType: String, email: String) {
        _registrationSuccess.postValue(true)
        val currentUser = firebaseAuthModel.getCurrentUser()
        currentUser?.let {
            firestoreModel.setUserAccessRights(userType, email, it.uid)
            initUserInfo(userType, email)
        }
    }

    private fun registrationError() {
        _toastMessage.postValue(R.string.user_registration_error_msg)
    }

    private fun initUserInfo(userType: String, email: String) {
        if (userType == USER_TYPE_USER) {
            val baseSkillsRef = "base_skills_init"
            firestoreModel.getDocument("init_data", baseSkillsRef,
                onSuccess = { documentSnapshot ->
                    val baseSkillsData = documentSnapshot.data
                    val userData = HashMap<String, Any>()
                    userData.putAll(baseSkillsData ?: return@getDocument)

                    firestoreModel.setDocument(
                        "user_info",
                        email,
                        userData,
                        onSuccess = {},
                        onFailure = {})
                },
                onFailure = {})
        }
    }

    private suspend fun checkOrganizationNameExists(organizationName: String): Boolean {
        val querySnapshot =
            firestoreModel.getQuerySnapshotWhereEqualTo(
                "organization_info",
                "organization_name",
                organizationName
            ).await()
        return querySnapshot.documents.isNotEmpty()
    }

    private suspend fun checkOrganizationPhoneExists(organizationPhone: String): Boolean {
        val querySnapshot =
            firestoreModel.getQuerySnapshotWhereEqualTo(
                "organization_info",
                "organization_phone",
                organizationPhone
            ).await()
        return querySnapshot.documents.isNotEmpty()
    }

    private fun validateInputs(
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
            _emailInputError.postValue(true)
            isValid = false
        }

        if (password.isEmpty()) {
            _passwordInputError.postValue(true)
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            _passwordConfirmInputError.postValue(true)
            isValid = false
        }

        if (password != confirmPassword) {
            _toastMessage.postValue(R.string.reg_passwords_dont_match_msg)
            isValid = false
        }

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                _organizationNameInputError.postValue(true)
                isValid = false
            }

            if (contactPerson.isEmpty()) {
                _contactPersonInputError.postValue(true)
                isValid = false
            }

            if (organizationPhone.isEmpty()) {
                _organizationPhoneInputError.postValue(true)
                isValid = false
            }
        }

        return isValid
    }
}