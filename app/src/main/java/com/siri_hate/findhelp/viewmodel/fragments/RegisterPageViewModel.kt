package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.model.models.Organization
import kotlinx.coroutines.tasks.await

class RegisterPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    companion object {
        private const val USER_TYPE_USER = "user"
        private const val USER_TYPE_ORGANIZER = "organizer"
    }

    private val _registrationSuccessLiveData = MutableLiveData<Boolean>()
    val registrationSuccessLiveData: LiveData<Boolean> = _registrationSuccessLiveData

    private val _registrationErrorLiveData = MutableLiveData<String?>()
    val registrationErrorLiveData: LiveData<String?> = _registrationErrorLiveData

    fun registerUser(email: String, password: String) {
        firebaseAuthModel.performRegistration(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                registrationSuccess(USER_TYPE_USER, email)
            } else {
                registrationError(task.exception?.message)
            }
        }
    }

    fun registerOrganizer(
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

                firestoreModel.addDocument(
                    "organization_info",
                    organization,
                    onSuccess = {},
                    onFailure = {})
            } else {
                registrationError(task.exception?.message)
            }
        }
    }

    private fun registrationSuccess(userType: String, email: String) {
        _registrationSuccessLiveData.postValue(true)
        val currentUser = firebaseAuthModel.getCurrentUser()
        currentUser?.let {
            firestoreModel.setUserAccessRights(userType, email, it.uid)
            initUserInfo(userType)
        }
    }

    private fun registrationError(errorMessage: String?) {
        _registrationErrorLiveData.postValue(errorMessage)
    }

    private fun initUserInfo(userType: String) {
        if (userType == USER_TYPE_USER) {
            val baseSkillsRef = "base_skills_init"
            firestoreModel.getDocument("init_data", baseSkillsRef,
                onSuccess = { documentSnapshot ->
                    val baseSkillsData = documentSnapshot.data
                    val userData = HashMap<String, Any>()
                    userData.putAll(baseSkillsData ?: return@getDocument)

                    firestoreModel.addDocument(
                        "user_info",
                        userData,
                        onSuccess = {},
                        onFailure = {})
                },
                onFailure = {})
        }
    }

    suspend fun checkOrganizationNameExists(organizationName: String): Boolean {
        val querySnapshot =
            firestoreModel.getQuerySnapshotWhereEqualTo(
                "organization_info",
                "organization_name",
                organizationName
            ).await()
        return querySnapshot.documents.isNotEmpty()
    }

    suspend fun checkOrganizationPhoneExists(organizationPhone: String): Boolean {
        val querySnapshot =
            firestoreModel.getQuerySnapshotWhereEqualTo(
                "organization_info",
                "organization_phone",
                organizationPhone
            ).await()
        return querySnapshot.documents.isNotEmpty()
    }

    fun inputCheck(
        email: String,
        password: String,
        confirmPassword: String,
        isOrganizer: Boolean,
        organizationName: String,
        contactPerson: String,
        organizationPhone: String
    ): Map<String, Boolean> {

        val errors = mutableMapOf<String, Boolean>()

        if (email.isEmpty()) {
            errors["email"] = true
        }

        if (password.isEmpty()) {
            errors["password"] = true
        }

        if (confirmPassword.isEmpty()) {
            errors["confirmPassword"] = true
        }

        if (password != confirmPassword) {
            errors["confirmPassword"] = true
        }

        if (isOrganizer) {
            if (organizationName.isEmpty()) {
                errors["organizationName"] = true
            }

            if (contactPerson.isEmpty()) {
                errors["contactPerson"] = true
            }

            if (organizationPhone.isEmpty()) {
                errors["organizationPhone"] = true
            }
        }

        return errors.toMap()
    }
}