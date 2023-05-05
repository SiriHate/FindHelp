package com.siri_hate.findhelp.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.fragments.VacancyCardViewModel

class VacancyCardViewModelFactory(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VacancyCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VacancyCardViewModel(firebaseAuthModel, firestoreModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}