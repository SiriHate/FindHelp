package com.siri_hate.findhelp.ui.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.fragments.OrganizerPageViewModel

class OrganizerPageViewModelFactory(private val firebaseFirestoreModel: FirebaseFirestoreModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrganizerPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrganizerPageViewModel(firebaseFirestoreModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}