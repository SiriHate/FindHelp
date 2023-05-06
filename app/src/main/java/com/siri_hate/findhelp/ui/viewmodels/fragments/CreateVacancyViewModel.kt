package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Skill
import com.siri_hate.findhelp.data.models.Vacancy

class CreateVacancyViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
): ViewModel() {

    private val _skillsList = MutableLiveData<List<Skill>>()
    val skillsList: LiveData<List<Skill>>
        get() = _skillsList

    init {
        loadSkills()
    }

    fun createVacancy(
        name: String,
        city: String,
        description: String,
        selectedSkillsMap: Map<String, Boolean>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
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
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = {
                        onFailure()
                    }
                )
            },
            onFailure = {
                onFailure()
            }
        )
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

    fun convertSkillsList(skillsList: List<Skill>): MutableMap<String, Boolean> {
        val skillsMap = mutableMapOf<String, Boolean>()
        skillsList.forEach { skill ->
            skillsMap[skill.name] = skill.isChecked
        }
        return skillsMap
    }


}