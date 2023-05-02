package com.siri_hate.findhelp.model.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// added -> Login,

class FirebaseAuthModel {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun checkUserAccess(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun performLogin(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

}