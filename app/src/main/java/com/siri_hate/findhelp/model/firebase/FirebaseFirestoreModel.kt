package com.siri_hate.findhelp.model.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseFirestoreModel {

    companion object {
        const val USER_RIGHTS_COLLECTION = "user_rights"
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return db.collection(USER_RIGHTS_COLLECTION).document(userEmail).get()
    }

}