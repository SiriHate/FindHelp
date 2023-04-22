package com.siri_hate.findhelp.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.viewmodel.fragments.LoginPageViewModel

class LoginPageViewModelFactory(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginPageViewModel(firebaseAuth, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}