package com.siri_hate.findhelp.viewmodel.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class EditVacancyInfoViewModel : ViewModel() {

    companion object {
        private const val COLLECTION_VACANCIES_LIST = "vacancies_list"
    }

    private val _vacancyName = MutableLiveData<String>()
    val vacancyName: LiveData<String> get() = _vacancyName

    private val _vacancyCity = MutableLiveData<String>()
    val vacancyCity: LiveData<String> get() = _vacancyCity

    private val _vacancyDescription = MutableLiveData<String>()
    val vacancyDescription: LiveData<String> get() = _vacancyDescription

    fun setVacancyValues(db: FirebaseFirestore, documentId: String) {
        db.collection(COLLECTION_VACANCIES_LIST).document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                _vacancyName.value = documentSnapshot.getString("vacancy_name")
                _vacancyCity.value = documentSnapshot.getString("vacancy_city")
                _vacancyDescription.value = documentSnapshot.getString("vacancy_description")
            }
    }

    fun updateVacancy(db: FirebaseFirestore, documentId: String, name: String, city: String, description: String) {
        val vacancyRef = db.collection(COLLECTION_VACANCIES_LIST).document(documentId)

        val updates = hashMapOf(
            "vacancy_name" to name,
            "vacancy_city" to city,
            "vacancy_description" to description
        )

        vacancyRef.update(updates as Map<String, Any>).addOnSuccessListener {
            Log.d("EditVacancyMainFragment", "Изменения сохранены")
        }
    }

}