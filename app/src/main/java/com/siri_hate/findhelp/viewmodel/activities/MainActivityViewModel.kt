package com.siri_hate.findhelp.viewmodel.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel

class MainActivityViewModel(
    private val firestoreModel: FirebaseFirestoreModel,
    private val authModel: FirebaseAuthModel
) : ViewModel() {

    private val _showLogoutButton = MutableLiveData(false)
    val showLogoutButton: LiveData<Boolean>
        get() = _showLogoutButton

    private val _showGoBackButton = MutableLiveData(false)
    val showGoBackButton: LiveData<Boolean>
        get() = _showGoBackButton

    companion object {
        private const val TAG = "MainActivityViewModel"
        private const val USER_TYPE_FIELD = "userType"
        private const val USER_TYPE_ORGANIZER_VALUE = "organizer"
        private const val USER_TYPE_USER_VALUE = "user"
        private const val USER_TYPE_MODERATOR_VALUE = "moderator"
    }
    fun onDestinationChanged(destinationId: Int) {
        when (destinationId) {
            R.id.userPageFragment,
            R.id.userProfileFragment,
            R.id.moderatorPageFragment,
            R.id.organizerPageFragment -> _showLogoutButton.value = true
            else -> _showLogoutButton.value = false
        }

        when (destinationId) {
            R.id.registerFragment,
            R.id.createVacancyFragment,
            R.id.editVacancyFragment,
            R.id.vacancyCardFragment -> _showGoBackButton.value = true
            else -> _showGoBackButton.value = false
        }
    }

    fun navigateToLoginFragment(controller: NavController) {
        when (controller.currentDestination?.id) {
            R.id.userPageFragment -> controller.navigate(R.id.action_userPageFragment_to_loginFragment)
            R.id.userProfileFragment -> controller.navigate(R.id.action_userProfileFragment_to_loginFragment)
            R.id.moderatorPageFragment -> controller.navigate(R.id.action_moderatorPageFragment_to_loginFragment)
            R.id.organizerPageFragment -> controller.navigate(R.id.action_organizerPageFragment_to_loginFragment)
        }
    }

    fun goBack(controller: NavController) {
        when (controller.currentDestination?.id) {
            R.id.registerFragment -> controller.navigate(R.id.action_registerFragment_to_loginFragment)
            R.id.createVacancyFragment -> controller.navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
            R.id.editVacancyFragment -> editVacancyCardExit(controller)
            R.id.vacancyCardFragment -> vacancyCardExit(controller)
        }
    }

    private fun editVacancyCardExit(controller: NavController) {
        val navHostFragment =
            (controller.currentDestination as NavHostFragment)
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        val bundle = currentFragment?.arguments
        controller.navigate(R.id.action_editVacancyMainFragment_to_vacancyCardFragment, bundle)
    }

    private fun vacancyCardExit(controller: NavController) {
        val user = authModel.getCurrentUser()
        user?.email?.let { email ->
            firestoreModel.getUserTypeFromFirestore(email)
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        when (val userType = document.getString(USER_TYPE_FIELD)) {
                            USER_TYPE_USER_VALUE -> controller.navigate(R.id.action_vacancyCardFragment_to_userPageFragment)
                            USER_TYPE_ORGANIZER_VALUE -> controller.navigate(R.id.action_vacancyCardFragment_to_organizerPageFragment)
                            USER_TYPE_MODERATOR_VALUE -> controller.navigate(R.id.action_vacancyCardFragment_to_moderatorPageFragment)
                            else -> Log.d(TAG, "Неккоректный userType: $userType")
                        }
                    } else {
                        Log.d(TAG, "Документ не найден")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Ошибка получения документа", exception)
                }
        }
    }

    fun performLogout() {
        authModel.performLogout()
    }
}