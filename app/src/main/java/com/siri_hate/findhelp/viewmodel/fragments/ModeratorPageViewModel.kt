package com.siri_hate.findhelp.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class ModeratorPageViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val offersRef = db.collection(COLLECTION_NAME)

    private var originalOffers: List<DocumentSnapshot> = emptyList()

    private val _offersLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val offersLiveData: LiveData<List<DocumentSnapshot>> = _offersLiveData

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> = _errorMessageLiveData

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _emptyListLiveData = MutableLiveData<Boolean>()
    val emptyListLiveData: LiveData<Boolean> = _emptyListLiveData

    private var snapshotListener: ListenerRegistration? = null

    companion object {
        private const val VACANCY_NAME = "vacancy_name"
        private const val COLLECTION_NAME = "vacancies_list"
    }

    fun initSnapshotListener() {
        _loading.value = true

        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                _errorMessageLiveData.postValue("Listen failed.")
                return@addSnapshotListener
            }

            originalOffers = snapshots?.documents?.toList() ?: emptyList()
            checkIfListEmpty(originalOffers)
            _offersLiveData.postValue(originalOffers)

            _loading.value = false
        }
    }

    private fun checkIfListEmpty(list: List<DocumentSnapshot>) {
        _emptyListLiveData.postValue(list.isEmpty())
    }

    fun filterOffers(query: String) {
        val filteredOffers = originalOffers.filter {
            it.getString(VACANCY_NAME)?.lowercase(Locale.getDefault())
                ?.startsWith(query) == true
        }

        if (filteredOffers.isEmpty()) {
            _emptyListLiveData.postValue(true)
        } else {
            _emptyListLiveData.postValue(false)
        }

        _offersLiveData.postValue(filteredOffers)
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun clear() {
        onCleared()
    }
}