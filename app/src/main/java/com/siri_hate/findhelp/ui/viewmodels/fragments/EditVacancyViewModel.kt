package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Skill
import com.siri_hate.findhelp.data.models.Vacancy

class EditVacancyViewModel(private val firestoreModel: FirebaseFirestoreModel) : ViewModel() {

    val vacancyName = MutableLiveData<String>()
    val vacancyCity = MutableLiveData<String>()
    val vacancyDescription = MutableLiveData<String>()
    val skillsList = MutableLiveData<List<Skill>>()
    val isLoading = MutableLiveData<Boolean>()

    private val _toastMessage = MutableLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _vacancyNameInputError = MutableLiveData<Boolean>()
    val vacancyNameInputError: LiveData<Boolean> = _vacancyNameInputError

    private val _vacancyCityInputError = MutableLiveData<Boolean>()
    val vacancyCityInputError: LiveData<Boolean> = _vacancyCityInputError

    private val _vacancyDescriptionInputError = MutableLiveData<Boolean>()
    val vacancyDescriptionInputError: LiveData<Boolean> = _vacancyDescriptionInputError

    private val _goBack = MutableLiveData<Boolean>()
    val goBack: LiveData<Boolean> = _goBack

    fun loadData(documentId: String) {
        isLoading.postValue(true)

        firestoreModel.getVacancy(documentId)
            .addOnSuccessListener { documentSnapshot ->
                vacancyName.postValue(documentSnapshot.getString("vacancy_name"))
                vacancyCity.postValue(documentSnapshot.getString("vacancy_city"))
                vacancyDescription.postValue(documentSnapshot.getString("vacancy_description"))

                @Suppress("UNCHECKED_CAST")
                val skillsMap =
                    documentSnapshot?.get("vacancy_skills_list") as? Map<String, Boolean>
                val skillsListData = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()
                skillsList.postValue(skillsListData)

                isLoading.postValue(false)
            }
    }

    fun handleUpdateVacancy(
        documentId: String,
        name: String,
        city: String,
        description: String,
        selectedSkills: Map<String, Boolean>,
    ) {
        if (validateInputs(name, city, description) && validateSkills(selectedSkills)) {
            updateVacancy(documentId, name, city, description, selectedSkills)
        }
    }

    private fun updateVacancy(
        documentId: String,
        name: String,
        city: String,
        description: String,
        selectedSkills: Map<String, Boolean>,
    ) {
        val vacancy = Vacancy(
            vacancy_name = name,
            vacancy_city = city,
            vacancy_description = description,
            vacancy_skills_list = selectedSkills
        )

        firestoreModel.updateVacancy(documentId, vacancy)
            .addOnSuccessListener {
                createVacancySuccess()
            }
            .addOnFailureListener {
                createVacancyFailed()
            }
    }

    private fun createVacancySuccess() {
        _goBack.value = true
        _toastMessage.value = R.string.vacancy_edited_successfully_msg
    }

    private fun createVacancyFailed() {
        _toastMessage.value = R.string.vacancy_edit_error_message
    }

    private fun validateInputs(email: String, city: String, description: String): Boolean {
        var isValid = true
        if (email.isBlank()) {
            _vacancyNameInputError.value = true
            isValid = false
        }
        if (city.isBlank()) {
            _vacancyCityInputError.value = true
            isValid = false
        }
        if (description.isBlank()) {
            _vacancyDescriptionInputError.value = true
            isValid = false
        }
        return isValid
    }

    private fun validateSkills(selectedSkills: Map<String, Boolean>): Boolean {
        if (!selectedSkills.containsValue(true)) {
            _toastMessage.value = R.string.neeed_to_select_vacancy_skill_msg
            return false
        }
        return true
    }
}