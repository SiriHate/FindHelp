package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel

class LoginPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    companion object {
        const val USER_PAGE = "user"
        const val ORGANIZER_PAGE = "organizer"
        const val MODERATOR_PAGE = "moderator"
    }

    val errorMessageLiveData: MutableLiveData<String> = MutableLiveData()

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading:LiveData<Boolean> = _loading

    private val _destinationPage: MutableLiveData<String?> = MutableLiveData()
    val destinationPage: MutableLiveData<String?> = _destinationPage

    private val _emailInputError: MutableLiveData<Boolean> = MutableLiveData()
    val emailInputError: LiveData<Boolean> = _emailInputError

    private val _passwordInputError: MutableLiveData<Boolean> = MutableLiveData()
    val passwordInputError: LiveData<Boolean> = _passwordInputError

    private fun checkUserAccess(): FirebaseUser? {
        return firebaseAuthModel.getCurrentUser()
    }

    private fun performLogin(email: String, password: String): Task<AuthResult> {
        return firebaseAuthModel.performLogin(email, password)
    }

    private fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return firestoreModel.getUserTypeFromFirestore(userEmail)
    }

    fun startUserPageFragment(controller: NavController, dest_page: String?) {
        when (dest_page) {
            USER_PAGE -> controller.navigate(R.id.action_loginFragment_to_userPageFragment)
            ORGANIZER_PAGE -> controller.navigate(R.id.action_loginFragment_to_organizerPageFragment)
            MODERATOR_PAGE -> controller.navigate(R.id.action_loginFragment_to_moderatorPageFragment)
            else -> showErrorMessage(R.string.login_cant_determine_user_rights_msg.toString())
        }
    }

    fun login(email: String, password: String) {

        if (inputFieldsAreEmpty(email, password)) {
            return
        }

        _loading.value = true
        performLogin(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuthModel().getCurrentUser()
                val userEmail = user?.email

                userEmail?.let { email ->
                    getUserTypeFromFirestore(email).addOnCompleteListener { userTypeTask ->
                        if (userTypeTask.isSuccessful) {
                            val userType = userTypeTask.result?.getString("userType")
                            _destinationPage.value = userType
                        } else {
                            showErrorMessage("Ошибка доступа к базе данных: " + userTypeTask.exception?.message)
                        }
                        _loading.value = false
                    }
                }
            } else {
                showErrorMessage("Ошибка авторизации: " + task.exception?.message)
                _loading.value = false
            }
        }
    }

    private fun inputFieldsAreEmpty(email: String, password: String): Boolean {
        var isEmpty = false

        if (email.isEmpty()) {
            _emailInputError.value = true
            isEmpty = true
        }

        if (password.isEmpty()) {
            _passwordInputError.value = true
            isEmpty = true
        }

        return isEmpty
    }

    fun navigateToRegistration(controller: NavController) {
        controller.navigate(R.id.action_loginFragment_to_registerFragment)
    }

    fun isUserauthorized() {
        checkUserAccess()?.let { currentUser ->
            _loading.value = true
            currentUser.email?.let { userEmail ->
                getUserTypeFromFirestore(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userType = task.result?.getString("userType")
                        _destinationPage.value = userType
                    } else {
                        showErrorMessage("Ошибка доступа к базе данных: " + task.exception?.message)
                    }
                    _loading.value = false
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        errorMessageLiveData.postValue(message)
    }
}