package com.siri_hate.findhelp.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.fragments.OrganizerPageViewModel

class OrganizerPageViewModelFactory(private val firebaseFirestoreModel: FirebaseFirestoreModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrganizerPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrganizerPageViewModel(firebaseFirestoreModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}