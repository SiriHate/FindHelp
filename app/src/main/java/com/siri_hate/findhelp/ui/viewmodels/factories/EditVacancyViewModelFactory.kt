package com.siri_hate.findhelp.ui.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.fragments.EditVacancyViewModel

class EditVacancyViewModelFactory(private val firestoreModel: FirebaseFirestoreModel) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditVacancyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditVacancyViewModel(firestoreModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}