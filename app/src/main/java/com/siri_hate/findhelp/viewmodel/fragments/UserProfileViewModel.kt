package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.model.models.Skill


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

            firestoreModel.getDocument(USER_INFO_COLLECTION, userEmail,
                onSuccess = { documentSnapshot ->
                    val userCity = documentSnapshot.getString(USER_CITY_FIELD) ?: ""
                    _userCity.value = userCity
                    _isLoading.value = false
                },
                onFailure = {
                    _isLoading.value = false
                }
            )

            firestoreModel.getDocument(USER_INFO_COLLECTION, userEmail,
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
            firestoreModel.getDocument(USER_INFO_COLLECTION, userEmail,
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
}