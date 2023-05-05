package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.model.models.Skill
import com.siri_hate.findhelp.model.models.Vacancy

class EditVacancyViewModel(private val firestoreModel: FirebaseFirestoreModel) : ViewModel() {

    val vacancyName = MutableLiveData<String>()
    val vacancyCity = MutableLiveData<String>()
    val vacancyDescription = MutableLiveData<String>()
    val skillsList = MutableLiveData<List<Skill>>()
    val isLoading = MutableLiveData<Boolean>()

    fun loadData(documentId: String) {
        isLoading.postValue(true)

        firestoreModel.getVacancy(documentId)
            .addOnSuccessListener { documentSnapshot ->
                vacancyName.postValue(documentSnapshot.getString("vacancy_name"))
                vacancyCity.postValue(documentSnapshot.getString("vacancy_city"))
                vacancyDescription.postValue(documentSnapshot.getString("vacancy_description"))

                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get("vacancy_skills_list") as? Map<String, Boolean>
                val skillsListData = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()
                skillsList.postValue(skillsListData)

                isLoading.postValue(false)
            }
    }

    fun updateVacancy(
        documentId: String,
        name: String,
        city: String,
        description: String,
        selectedSkills: Map<String, Boolean>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val vacancy = Vacancy(
            vacancy_name = name,
            vacancy_city = city,
            vacancy_description = description,
            vacancy_skills_list = selectedSkills
        )

        firestoreModel.updateVacancy(documentId, vacancy)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "")
            }
    }

    fun validateInputs(name: String, city: String, description: String): Boolean {
        if (name.isEmpty()) return false
        if (city.isEmpty()) return false
        if (description.isEmpty()) return false

        return true
    }

    fun validateSelectedSkills(selectedSkills: List<Skill>): Boolean {
        val filteredSkills = selectedSkills.filter { it.isChecked }
        return filteredSkills.isNotEmpty()
    }
}