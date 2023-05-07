package com.siri_hate.findhelp.ui.viewmodels.fragments

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Skill
import com.siri_hate.findhelp.data.models.Vacancy

class CreateVacancyViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    private val _skillsList = MutableLiveData<List<Skill>>()
    val skillsList: LiveData<List<Skill>>
        get() = _skillsList

    private val _toastMessage = MutableLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _destPage = MutableLiveData<String>()
    val destPage: LiveData<String> = _destPage

    private val _vacancyNameInputError = MutableLiveData<Boolean>()
    val vacancyNameInputError: LiveData<Boolean> = _vacancyNameInputError

    private val _vacancyCityInputError = MutableLiveData<Boolean>()
    val vacancyCityInputError: LiveData<Boolean> = _vacancyCityInputError

    private val _vacancyDescriptionInputError = MutableLiveData<Boolean>()
    val vacancyDescriptionInputError: LiveData<Boolean> = _vacancyDescriptionInputError

    init {
        loadSkills()
    }

    fun handleCreateVacancy(
        name: String,
        city: String,
        description: String,
        selectedSkills: Map<String, Boolean>
    ) {
        if (validateInputs(name, city, description) && validateSkills(selectedSkills)) {
            createVacancy(name, city, description, selectedSkills)
        }
    }

    private fun createVacancy(
        name: String,
        city: String,
        description: String,
        selectedSkillsMap: Map<String, Boolean>
    ) {
        val currentUserEmail = firebaseAuthModel.getCurrentUser()?.email.toString()
        firestoreModel.getDocument("organization_info", currentUserEmail,
            onSuccess = { documentSnapshot ->
                val contactPerson = documentSnapshot.getString("contact_person") ?: ""
                val orgName = documentSnapshot.getString("organization_name") ?: ""
                val orgPhone = documentSnapshot.getString("organization_phone") ?: ""

                val vacancy = Vacancy(
                    creator_email = currentUserEmail,
                    contact_person = contactPerson,
                    organization_name = orgName,
                    organization_phone = orgPhone,
                    vacancy_name = name,
                    vacancy_city = city,
                    vacancy_description = description,
                    vacancy_skills_list = selectedSkillsMap
                )

                firestoreModel.addDocument("vacancies_list", vacancy,
                    onSuccess = { createVacancySuccess() },
                    onFailure = { createVacancyFailed() }
                )
            },
            onFailure = { createVacancyFailed() }
        )
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

    private fun createVacancySuccess() {
        _destPage.value = "organizer"
        _toastMessage.value = R.string.vacancy_added_successfully_msg
    }

    private fun createVacancyFailed() {
        _toastMessage.value = R.string.vacancy_added_failed_msg
    }

    fun navigateToPage(controller: NavController, destPage: String) {
        when (destPage) {
            "organizer" -> controller.navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
            else -> Log.d(TAG, "Navigate error")
        }
    }

    private fun loadSkills() {
        firestoreModel.getDocument("init_data", "base_skills_init",
            onSuccess = { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get("skills") as? Map<String, Boolean>
                val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()

                _skillsList.postValue(skillsList)
            }
        )
    }

}