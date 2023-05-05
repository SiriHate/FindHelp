package com.siri_hate.findhelp.viewmodel.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel

class VacancyCardViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    private val _skillsList = MutableLiveData<List<String>>()
    val skillsList: LiveData<List<String>>
        get() = _skillsList

    private val _vacancyName = MutableLiveData<String>()
    val vacancyName: LiveData<String>
        get() = _vacancyName

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _companyName = MutableLiveData<String>()
    val companyName: LiveData<String>
        get() = _companyName

    private val _contactPerson = MutableLiveData<String>()
    val contactPerson: LiveData<String>
        get() = _contactPerson

    private val _organizationPhone = MutableLiveData<String>()
    val organizationPhone: LiveData<String>
        get() = _organizationPhone

    private val _organizationCity = MutableLiveData<String>()
    val organizationCity: LiveData<String>
        get() = _organizationCity

    private val _vacancyDescription = MutableLiveData<String>()
    val vacancyDescription: LiveData<String>
        get() = _vacancyDescription

    private val _isEditButtonVisible = MutableLiveData<Boolean>()
    val isEditButtonVisible: LiveData<Boolean>
        get() = _isEditButtonVisible

    private val _isSkillsListEmpty = MutableLiveData<Boolean>()
    val isSkillsListEmpty: LiveData<Boolean> = _isSkillsListEmpty

    companion object {
        private const val USER_TYPE_FIELD = "userType"
        private const val USER_TYPE_ORGANIZER_VALUE = "organizer"
        private const val USER_TYPE_MODERATOR_VALUE = "moderator"
        private const val CREATOR_EMAIL_FIELD = "creator_email"
    }

    fun loadVacancyInfo(documentId: String) {
        _isLoading.postValue(true)
        firestoreModel.getVacancy(documentId)
            .addOnSuccessListener { vacancySnapshot ->
                updateVacancyInfo(vacancySnapshot)
                firebaseAuthModel.getCurrentUser()?.let { user ->
                    checkUserRightsAndSetEditButtonVisibility(user, vacancySnapshot)
                }
                _isLoading.postValue(false)
            }
            .addOnFailureListener { exception ->
                Log.d("VacancyCardViewModel", "Error getting vacancy document", exception)
                _isLoading.postValue(false)
            }
    }

    private fun updateSkillsList(skillsMap: Map<*, *>?) {
        val newSkillsList = mutableListOf<String>()
        skillsMap?.forEach { (key, value) ->
            if (value as Boolean) {
                newSkillsList.add(key.toString())
            }
        }
        _skillsList.postValue(newSkillsList)
        _isSkillsListEmpty.postValue(newSkillsList.isEmpty())
    }

    private fun updateVacancyInfo(snapshot: DocumentSnapshot) {
        val skillsMap = snapshot.get("vacancy_skills_list") as Map<*, *>?
        updateSkillsList(skillsMap)

        _vacancyName.postValue(snapshot.getString("vacancy_name"))
        _companyName.postValue(snapshot.getString("organization_name"))
        _contactPerson.postValue(snapshot.getString("contact_person"))
        _organizationPhone.postValue(snapshot.getString("organization_phone"))
        _organizationCity.postValue(snapshot.getString("vacancy_city"))
        _vacancyDescription.postValue(snapshot.getString("vacancy_description"))
    }

    private fun checkUserRightsAndSetEditButtonVisibility(
        user: FirebaseUser,
        snapshot: DocumentSnapshot
    ) {
        user.email?.let { email ->
            firestoreModel.getUserTypeFromFirestore(email)
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userType = document.getString(USER_TYPE_FIELD)
                        if (userType == USER_TYPE_MODERATOR_VALUE || (userType == USER_TYPE_ORGANIZER_VALUE && user.email == snapshot.getString(
                                CREATOR_EMAIL_FIELD
                            ))
                        ) {
                            _isEditButtonVisible.postValue(true)
                        } else {
                            _isEditButtonVisible.postValue(false)
                        }
                    } else {
                        Log.d("VacancyCardViewModel", "User document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("VacancyCardViewModel", "Error getting user document", exception)
                }
        }
    }
}