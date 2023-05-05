package com.siri_hate.findhelp.model.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.siri_hate.findhelp.model.models.User
import com.siri_hate.findhelp.model.models.Vacancy

class FirebaseFirestoreModel {

    companion object {
        const val USER_RIGHTS_COLLECTION = "user_rights"
        const val VACANCIES_COLLECTION = "vacancies_list"

    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserTypeFromFirestore(userEmail: String): Task<DocumentSnapshot> {
        return db.collection(USER_RIGHTS_COLLECTION).document(userEmail).get()
    }

    fun addSnapshotListener(
        collectionName: String,
        onEvent: (QuerySnapshot) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val collectionRef = db.collection(collectionName)
        return collectionRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                onError(e)
            } else {
                onEvent(snapshots ?: return@addSnapshotListener)
            }
        }
    }

    fun getDocument(
        collectionName: String,
        documentId: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: () -> Unit = {}
    ) {
        db.collection(collectionName).document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onSuccess(document)
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun updateDocument(
        collectionName: String,
        documentId: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        db.collection(collectionName).document(documentId).update(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun addDocument(
        collectionName: String,
        data: Any,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        db.collection(collectionName).add(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun getQuerySnapshotWhereEqualTo(collectionName: String, field: String, value: Any): Task<QuerySnapshot> {
        return db.collection(collectionName).whereEqualTo(field, value).get()
    }

    fun setUserAccessRights(userType: String, email: String, uid: String) {
        val user = User(uid = uid, userType = userType)
        val userRef = db.collection(USER_RIGHTS_COLLECTION).document(email)
        userRef.set(user)
    }

    fun getVacancy(documentId: String): Task<DocumentSnapshot> {
        return db.collection(VACANCIES_COLLECTION).document(documentId).get()
    }

    fun updateVacancy(documentId: String, vacancy: Vacancy): Task<Void> {
        return db.collection(VACANCIES_COLLECTION).document(documentId).update(vacancy.toMap())
    }
}