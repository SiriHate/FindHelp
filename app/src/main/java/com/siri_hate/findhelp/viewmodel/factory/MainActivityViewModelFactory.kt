package com.siri_hate.findhelp.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.activities.MainActivityViewModel

class MainActivityViewModelFactory(
    private val firestoreModel: FirebaseFirestoreModel,
    private val authModel: FirebaseAuthModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(firestoreModel, authModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}