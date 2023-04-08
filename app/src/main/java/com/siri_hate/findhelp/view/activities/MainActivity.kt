package com.siri_hate.findhelp.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

import com.siri_hate.findhelp.R

class MainActivity : AppCompatActivity() {

    private lateinit var mainLogoutButton: ImageButton
    private lateinit var controller: NavController
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLogoutButton = findViewById(R.id.main_logout_button)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment_container) as NavHostFragment

        controller = navHostFragment.navController

        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (
                destination.id == R.id.userPageFragment ||
                destination.id == R.id.userProfileFragment ||
                destination.id == R.id.moderatorPageFragment ||
                destination.id == R.id.organizerPageFragment
            ) {
                mainLogoutButton.visibility = View.VISIBLE
            } else {
                mainLogoutButton.visibility = View.GONE
            }
        }

        mainLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            navigateToLoginFragment()
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
}