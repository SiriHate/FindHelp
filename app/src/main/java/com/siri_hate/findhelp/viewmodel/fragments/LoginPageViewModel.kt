package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel

class LoginPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    companion object {
        const val USER_TYPE_USER = "user"
        const val USER_TYPE_ORGANIZER = "organizer"
        const val USER_TYPE_MODERATOR = "moderator"
    }

    val errorMessageLiveData: MutableLiveData<String> = MutableLiveData()

    fun checkUserAccess(): FirebaseUser? {
        return firebaseAuthModel.getCurrentUser()
    }

    fun performLogin(email: String, password: String): Task<AuthResult> {
        return firebaseAuthModel.performLogin(email, password)
    }

    fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return firestoreModel.getUserTypeFromFirestore(userEmail)
    }

    fun startUserPageFragment(controller: NavController, userType: String?) {
        when (userType) {
            USER_TYPE_USER -> controller.navigate(R.id.action_loginFragment_to_userPageFragment)
            USER_TYPE_ORGANIZER -> controller.navigate(R.id.action_loginFragment_to_organizerPageFragment)
            USER_TYPE_MODERATOR -> controller.navigate(R.id.action_loginFragment_to_moderatorPageFragment)
            else -> showErrorMessage("Не удалось определить права доступа")
        }
    }

    fun showErrorMessage(message: String) {
        errorMessageLiveData.postValue(message)
    }
}