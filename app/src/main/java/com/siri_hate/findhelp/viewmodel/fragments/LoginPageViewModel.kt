package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class LoginPageViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    companion object {
        const val USER_TYPE_USER = "user"
        const val USER_TYPE_ORGANIZER = "organizer"
        const val USER_TYPE_MODERATOR = "moderator"
        const val USER_RIGHTS_COLLECTION = "user_rights"
    }

    val errorMessageLiveData: MutableLiveData<String> = MutableLiveData()

    fun checkUserAccess(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun performLogin(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return db.collection(USER_RIGHTS_COLLECTION).document(userEmail).get()
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