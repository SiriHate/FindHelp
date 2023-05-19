package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel

class LoginPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    private val _toastMessage: MutableLiveData<String> = MutableLiveData()
    val toastMessage: LiveData<String> = _toastMessage

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading:LiveData<Boolean> = _loading

    private val _destinationPage: MutableLiveData<String?> = MutableLiveData()
    val destinationPage: MutableLiveData<String?> = _destinationPage

    private val _emailInputError: MutableLiveData<Boolean> = MutableLiveData()
    val emailInputError: LiveData<Boolean> = _emailInputError

    private val _passwordInputError: MutableLiveData<Boolean> = MutableLiveData()
    val passwordInputError: LiveData<Boolean> = _passwordInputError

    companion object {
        const val USER_TYPE_FIELD = "userType"
    }

    private fun checkUserAccess(): FirebaseUser? {
        return firebaseAuthModel.getCurrentUser()
    }

    private fun performLogin(email: String, password: String): Task<AuthResult> {
        return firebaseAuthModel.performLogin(email, password)
    }

    private fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return firestoreModel.getUserTypeFromFirestore(userEmail)
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
                            val userType = userTypeTask.result?.getString(USER_TYPE_FIELD)
                            _destinationPage.value = userType
                        } else {
                            showErrorMessage("db_error")
                        }
                        _loading.value = false
                    }
                }
            } else {
                showErrorMessage("login_error")
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

    fun isUserAuthorized() {
        checkUserAccess()?.let { currentUser ->
            _loading.value = true
            currentUser.email?.let { userEmail ->
                getUserTypeFromFirestore(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userType = task.result?.getString(USER_TYPE_FIELD)
                        _destinationPage.value = userType
                    } else {
                        showErrorMessage("db_error")
                    }
                    _loading.value = false
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        _toastMessage.postValue(message)
    }
}