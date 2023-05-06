package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Skill

class UserProfileViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {
    private val _userCity = MutableLiveData<String>()
    val userCity: LiveData<String>
        get() = _userCity
    private val _userSkills = MutableLiveData<List<Skill>>()
    val userSkills: LiveData<List<Skill>>
        get() = _userSkills

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    companion object {
        private const val SKILLS_COLLECTION = "skills"
        private const val USER_INFO_COLLECTION = "user_info"
        private const val USER_CITY_FIELD = "user_city"
    }

    init {
        _isLoading.value = true
        val currentUser = firebaseAuthModel.getCurrentUser()

        if (currentUser != null) {
            val userEmail = currentUser.email.orEmpty()

            firestoreModel.getDocument(
                USER_INFO_COLLECTION, userEmail,
                onSuccess = { documentSnapshot ->
                    val userCity = documentSnapshot.getString(USER_CITY_FIELD) ?: ""
                    _userCity.value = userCity
                    _isLoading.value = false
                },
                onFailure = {
                    _isLoading.value = false
                }
            )

            firestoreModel.getDocument(
                USER_INFO_COLLECTION, userEmail,
                onSuccess = { documentSnapshot ->
                    @Suppress("UNCHECKED_CAST")
                    val skillsMap = documentSnapshot.get(SKILLS_COLLECTION) as? Map<String, Boolean>
                    val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()

                    _userSkills.value = skillsList
                    _isLoading.value = false
                },
                onFailure = {
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateUserCity(newCity: String) {
        val currentUser = firebaseAuthModel.getCurrentUser()

        if (currentUser != null) {
            val userEmail = currentUser.email.orEmpty()
            firestoreModel.getDocument(
                USER_INFO_COLLECTION, userEmail,
                onSuccess = {
                    firestoreModel.updateDocument(
                        USER_INFO_COLLECTION,
                        userEmail,
                        mapOf(USER_CITY_FIELD to newCity),
                        onSuccess = {},
                        onFailure = {}
                    )
                },
                onFailure = {}
            )
        }
    }

    fun updateUserSkill(skillName: String, isChecked: Boolean) {
        val currentUser = firebaseAuthModel.getCurrentUser()

        if (currentUser != null) {
            val userEmail = currentUser.email.orEmpty()
            val data = mapOf("$SKILLS_COLLECTION.$skillName" to isChecked)
            firestoreModel.updateDocument(
                USER_INFO_COLLECTION, userEmail, data,
                onSuccess = {
                    val skillsList = _userSkills.value?.toMutableList() ?: mutableListOf()
                    val updatedSkillIndex = skillsList.indexOfFirst { it.name == skillName }

                    if (updatedSkillIndex != -1) {
                        skillsList[updatedSkillIndex] = Skill(skillName, isChecked)
                    } else {
                        skillsList.add(Skill(skillName, isChecked))
                    }

                    _userSkills.value = skillsList
                },
                onFailure = {})
        }
    }
}