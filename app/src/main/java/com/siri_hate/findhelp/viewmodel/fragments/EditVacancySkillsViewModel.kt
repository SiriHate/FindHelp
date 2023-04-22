package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class EditVacancySkillsViewModel : ViewModel() {

    companion object {
        const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
    }

    private val _db = FirebaseFirestore.getInstance()
    val db: FirebaseFirestore get() = _db
    private val _skillsList: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val skillsList: LiveData<List<String>> get() = _skillsList
    private var isAtLeastOneCheckboxSelected = false


    fun loadSkillsList(documentId: String) {
        db.collection(VACANCIES_LIST_COLLECTION).document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get(VACANCY_SKILLS_LIST_FIELD) as? Map<String, Boolean>
                    ?: emptyMap()
                val skillsList = skillsMap.keys.toList()
                _skillsList.value = skillsList
            }
    }

    fun onCheckboxSelected() {
        isAtLeastOneCheckboxSelected = true
    }

    fun onCreateButtonClicked(): Boolean {
        return isAtLeastOneCheckboxSelected
    }
}