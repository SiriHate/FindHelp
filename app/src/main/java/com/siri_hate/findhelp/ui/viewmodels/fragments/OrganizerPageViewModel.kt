package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel

class OrganizerPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
    ) :
    ViewModel() {

    private val _vacanciesLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val vacanciesLiveData: LiveData<List<DocumentSnapshot>> = _vacanciesLiveData

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> = _errorMessageLiveData

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var snapshotListener: ListenerRegistration? = null

    private val _emptyListLiveData = MutableLiveData<Boolean>()
    val emptyListLiveData: LiveData<Boolean> = _emptyListLiveData

    companion object {
        private const val CREATOR_EMAIL_FIELD = "creator_email"
        private const val VACANCY_NAME_FIELD = "vacancy_name"
    }

    fun getUserEmail(): String {
        return firebaseAuthModel.getCurrentUser()?.email ?: ""
    }

    fun initVacanciesListener() {
        val userEmail = getUserEmail()

        _loading.value = true

        snapshotListener = firestoreModel.addSnapshotListener(
            FirebaseFirestoreModel.VACANCIES_COLLECTION,
            { value ->
                val offers = mutableListOf<DocumentSnapshot>()
                for (doc in value) {
                    offers.add(doc)
                }

                val filteredOffers = offers.filter {
                    it.getString(CREATOR_EMAIL_FIELD) == userEmail
                }

                _emptyListLiveData.postValue(filteredOffers.isEmpty())
                _vacanciesLiveData.postValue(filteredOffers)

                _loading.value = false
            },
            {
                _errorMessageLiveData.postValue(R.string.organizer_receiving_data_error_msg.toString())
            }
        )
    }

    fun filterVacanciesByQuery(query: String) {
        val originalList = vacanciesLiveData.value ?: emptyList()
        val filteredList = filterVacancies(query, originalList)
        _vacanciesLiveData.postValue(filteredList)
        _emptyListLiveData.postValue(filteredList.isEmpty())
    }

    private fun filterVacancies(
        query: String,
        originalList: List<DocumentSnapshot>
    ): List<DocumentSnapshot> {
        return originalList.filter {
            it.getString(VACANCY_NAME_FIELD)?.startsWith(query, ignoreCase = true) ?: false
        }
    }

    fun restartVacanciesListener() {
        _loading.value = true
        snapshotListener?.remove()
        initVacanciesListener()
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun clear() {
        onCleared()
    }

}