package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel: ViewModel() {

    companion object {
        private const val SKILLS_COLLECTION = "skills"
        private const val USER_INFO_COLLECTION = "user_info"
        private const val USER_CITY_FIELD = "user_city"
    }

    private val _userCityLiveData = MutableLiveData<String>()
    val userCityLiveData: LiveData<String>
        get() = _userCityLiveData

    private val _skillsListLiveData = MutableLiveData<List<String>>()
    val skillsListLiveData: LiveData<List<String>>
        get() = _skillsListLiveData

    fun loadUserCity(userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(USER_INFO_COLLECTION).document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                val userCity = documentSnapshot.getString(USER_CITY_FIELD)
                    _userCityLiveData.postValue(userCity!!)
            }
    }

    fun updateUserCity(newCity: String, userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(USER_INFO_COLLECTION).document(userEmail)
            .update(USER_CITY_FIELD, newCity)
    }

    fun loadSkillsList(userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(USER_INFO_COLLECTION).document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get(SKILLS_COLLECTION) as? Map<String, Any>
                val skillsList = skillsMap?.keys?.toList() ?: emptyList()
                _skillsListLiveData.postValue(skillsList)
            }
    }

}