package com.siri_hate.findhelp.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val USER_RIGHTS_COLLECTION = "user_rights"
        private const val USER_TYPE_FIELD = "userType"
        private const val USER_TYPE_ORGANIZER_VALUE = "organizer"
        private const val USER_TYPE_USER_VALUE = "user"
        private const val USER_TYPE_MODERATOR_VALUE = "moderator"
    }

    private lateinit var mainLogoutButton: ImageButton
    private lateinit var mainGoBackButton: ImageButton
    private lateinit var controller: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLogoutButton = findViewById(R.id.main_logout_button)
        mainGoBackButton = findViewById(R.id.main_go_back_button)



        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment_container) as NavHostFragment

        controller = navHostFragment.navController

        db = FirebaseFirestore.getInstance()

        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (
                destination.id == R.id.userPageFragment ||
                destination.id == R.id.userProfileFragment ||
                destination.id == R.id.moderatorPageFragment ||
                destination.id == R.id.organizerPageFragment
            ) {
                mainLogoutButton.visibility = View.VISIBLE
            } else {
                mainLogoutButton.visibility = View.INVISIBLE
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (
                destination.id == R.id.registerFragment ||
                destination.id == R.id.createVacancyFragment ||
                destination.id == R.id.editVacancyFragment ||
                destination.id == R.id.vacancyCardFragment
            ) {
                mainGoBackButton.visibility = View.VISIBLE
            } else {
                mainGoBackButton.visibility = View.INVISIBLE
            }
        }

        mainLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            navigateToLoginFragment()
        }

        mainGoBackButton.setOnClickListener {
            goBack()
        }


    }

    private fun goBack() {
        when (controller.currentDestination?.id) {
            R.id.registerFragment -> controller.navigate(R.id.action_registerFragment_to_loginFragment)
            R.id.createVacancyFragment -> controller.navigate(R.id.action_createVacancyFragment_to_organizerPageFragment)
            R.id.editVacancyFragment -> editVacancyCardExit()
            R.id.vacancyCardFragment -> vacancyCardExit()
        }
    }

    private fun navigateToLoginFragment() {

        when (controller.currentDestination?.id) {
            R.id.userPageFragment -> controller.navigate(R.id.action_userPageFragment_to_loginFragment)
            R.id.userProfileFragment -> controller.navigate(R.id.action_userProfileFragment_to_loginFragment)
            R.id.moderatorPageFragment -> controller.navigate(R.id.action_moderatorPageFragment_to_loginFragment)
            R.id.organizerPageFragment -> controller.navigate(R.id.action_organizerPageFragment_to_loginFragment)
        }
    }

    private fun editVacancyCardExit() {
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        val bundle = currentFragment?.arguments
        controller.navigate(R.id.action_editVacancyMainFragment_to_vacancyCardFragment, bundle)
    }

    private fun vacancyCardExit() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let { email ->
            db.collection(USER_RIGHTS_COLLECTION).document(email)
                .get()
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
}