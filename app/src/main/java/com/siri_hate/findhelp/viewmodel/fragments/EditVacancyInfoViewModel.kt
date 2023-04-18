package com.siri_hate.findhelp.viewmodel.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class EditVacancyInfoViewModel : ViewModel() {

    private val collectionVacanciesList = "vacancies_list"
    private lateinit var vacancyId: String

    private val _vacancyName = MutableLiveData<String>()
    val vacancyName: LiveData<String>
        get() = _vacancyName

    private val _vacancyCity = MutableLiveData<String>()
    val vacancyCity: LiveData<String>
        get() = _vacancyCity

    private val _vacancyDescription = MutableLiveData<String>()
    val vacancyDescription: LiveData<String>
        get() = _vacancyDescription

    fun init(documentId: String) {
        vacancyId = documentId
        setVacancyValues()
    }

    fun updateVacancy(name: String, city: String, description: String) {
        val vacancyRef = FirebaseFirestore.getInstance()
            .collection(collectionVacanciesList)
            .document(vacancyId)

        val updates = hashMapOf(
            "vacancy_name" to name,
            "vacancy_city" to city,
            "vacancy_description" to description
        )

        vacancyRef.update(updates as Map<String, Any>).addOnSuccessListener {
            Log.d("EditVacancyMainFragment", "Изменения сохранены")
        }
    }

    private fun setVacancyValues() {
        FirebaseFirestore.getInstance()
            .collection(collectionVacanciesList)
            .document(vacancyId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                _vacancyName.value = documentSnapshot.getString("vacancy_name")
                _vacancyCity.value = documentSnapshot.getString("vacancy_city")
                _vacancyDescription.value = documentSnapshot.getString("vacancy_description")
            }
    }

    fun isInputValid(name: String?, city: String?, description: String?): Boolean {
        val isEmptyName = name.isNullOrBlank()
        val isEmptyCity = city.isNullOrBlank()
        val isEmptyDescription = description.isNullOrBlank()

        return !isEmptyName && !isEmptyCity && !isEmptyDescription
    }
}