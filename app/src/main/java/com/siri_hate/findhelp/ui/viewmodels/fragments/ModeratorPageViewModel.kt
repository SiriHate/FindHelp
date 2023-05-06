package com.siri_hate.findhelp.ui.viewmodels.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import java.util.Locale

class ModeratorPageViewModel(private val firestoreModel: FirebaseFirestoreModel) : ViewModel() {

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

        snapshotListener = firestoreModel.addSnapshotListener(
            COLLECTION_NAME,
            { snapshots ->
                originalOffers = snapshots.documents.toList()
                checkIfListEmpty(originalOffers)
                _offersLiveData.postValue(originalOffers)

                _loading.value = false
            },
            {
                _errorMessageLiveData.postValue(R.string.moderator_receiving_data_error_msg.toString())
            }
        )
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