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

    private var snapshotListener: ListenerRegistration? = null

    companion object {
        private const val VACANCY_NAME = "vacancy_name"
        private const val COLLECTION_NAME = "vacancies_list"
    }

    fun initSnapshotListener() {
        offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                _errorMessageLiveData.postValue("Listen failed.")
                return@addSnapshotListener
            }

            originalOffers = snapshots?.documents?.toList() ?: emptyList()
            _offersLiveData.postValue(originalOffers)
        }
    }

    fun filterOffers(query: String) {
        val filteredOffers = originalOffers.filter {
            it.getString(VACANCY_NAME)?.lowercase(Locale.getDefault())
                ?.startsWith(query) == true
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