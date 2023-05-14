package com.siri_hate.findhelp.ui.viewmodels.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel


class UserPageViewModel(
    private val firebaseAuthModel: FirebaseAuthModel,
    private val firestoreModel: FirebaseFirestoreModel
) : ViewModel() {

    private lateinit var userDoc: DocumentSnapshot
    private val allVacancies = mutableListOf<DocumentSnapshot>()
    private val _filteredVacancies = MutableLiveData<List<DocumentSnapshot>>()
    val filteredVacancies:LiveData<List<DocumentSnapshot>> = _filteredVacancies
    private val _userSkills = MutableLiveData<HashMap<String, Boolean>>()
    val userSkills: LiveData<HashMap<String, Boolean>> = _userSkills
    private val _toastMessage = MutableLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    companion object {
        private const val TAG = "UserPageFragment"
        private const val VACANCY_CITY_FIELD = "vacancy_city"
        private const val USER_CITY_FIELD = "user_city"
        private const val VACANCY_NAME_FIELD = "vacancy_name"
        private const val USER_INFO_COLLECTION = "user_info"
        private const val VACANCIES_LIST_COLLECTION = "vacancies_list"
        private const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
        private const val SKILLS_FIELD = "skills"
    }

    fun fetchCurrentUserDocument() {
        val currentUserEmail = firebaseAuthModel.getCurrentUser()?.email
        (currentUserEmail)?.let { email ->
            firestoreModel.getDocument(USER_INFO_COLLECTION, email,
                onSuccess = { document ->
                    userDoc = document
                    updateUserSkills()
                    updateUserVacancies()
                },
                onFailure = {
                    Log.d(TAG, "No such document")
                }
            )
        }
    }

    private fun updateUserSkills() {
        @Suppress("UNCHECKED_CAST")
        val skills = userDoc[SKILLS_FIELD] as HashMap<String, Boolean>
        _userSkills.value = skills
    }

    private fun updateUserVacancies() {
        firestoreModel.addSnapshotListener(VACANCIES_LIST_COLLECTION,
            onEvent = { value ->
                updateAllAndFilteredVacancies(value)
            },
            onError = { exception ->
                Log.d(TAG, "Ошибка: ", exception)
                _toastMessage.value = R.string.user_page_list_loading_err_msg
            }
        )
    }

    private fun updateAllAndFilteredVacancies(value: QuerySnapshot?) {
        allVacancies.clear()

        value?.documents?.let { allVacancies.addAll(it) }

        filterAndSortVacancies("")
    }

    fun filterAndSortVacancies(query: String) {
        val matchingCityVacancies = filterVacancies(allVacancies, "")

        val filtered = filterVacancies(matchingCityVacancies, query)
        sortFilteredVacanciesByMatchingSkills(filtered)

        _filteredVacancies.value = filtered
    }

    private fun filterVacancies(
        vacancies: List<DocumentSnapshot>,
        query: String
    ): MutableList<DocumentSnapshot> {
        return vacancies.filter {
            it.getString(VACANCY_CITY_FIELD) == userDoc.getString(USER_CITY_FIELD)
                    && (query.isEmpty() || it.getString(VACANCY_NAME_FIELD)
                ?.startsWith(query, ignoreCase = true) ?: false)
        }.toMutableList()
    }

    private fun sortFilteredVacanciesByMatchingSkills(filtered: MutableList<DocumentSnapshot>) {
        filtered.sortByDescending { vacancy ->
            @Suppress("UNCHECKED_CAST")
            val vacancySkillsList = vacancy[VACANCY_SKILLS_LIST_FIELD] as? HashMap<String, Boolean>

            var matchCount = 0
            var vacancyCount = 0

            vacancySkillsList?.forEach { (skill, value) ->
                if (value && userSkills.value?.get(skill) == true) {
                    matchCount++
                }
                if (value) {
                    vacancyCount++
                }
            }

            if (vacancyCount == 0) 0 else (matchCount * 100 / vacancyCount)
        }
    }

}
