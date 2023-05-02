package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrganizerPageViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        private const val CREATOR_EMAIL_FIELD = "creator_email"
        private const val VACANCY_NAME_FIELD = "vacancy_name"
    }

    fun initVacanciesListener() {
        val userEmail = auth.currentUser?.email ?: ""

        _loading.value = true

        db.collection(VACANCIES_LIST_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _errorMessageLiveData.postValue("Listen failed.")
                    return@addSnapshotListener
                }

                val offers = mutableListOf<DocumentSnapshot>()
                for (doc in value!!) {
                    offers.add(doc)
                }

                val filteredOffers = offers.filter {
                    it.getString(CREATOR_EMAIL_FIELD) == userEmail
                }

                _emptyListLiveData.postValue(filteredOffers.isEmpty())
                _vacanciesLiveData.postValue(filteredOffers)

                _loading.value = false
            }
    }

    fun filterVacancies(query: String, originalList: List<DocumentSnapshot>): List<DocumentSnapshot> {
        return originalList.filter {
            it.getString(VACANCY_NAME_FIELD)?.startsWith(query, ignoreCase = true) ?: false
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun clear() {
        onCleared()
    }
}