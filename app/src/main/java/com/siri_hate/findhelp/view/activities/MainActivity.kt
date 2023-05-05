package com.siri_hate.findhelp.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.ActivityMainBinding
import com.siri_hate.findhelp.model.firebase.FirebaseAuthModel
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.viewmodel.activities.MainActivityViewModel
import com.siri_hate.findhelp.viewmodel.factory.MainActivityViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container) as NavHostFragment
        controller = navHostFragment.navController

        val firestoreModel = FirebaseFirestoreModel()
        val authModel = FirebaseAuthModel()
        val viewModelFactory = MainActivityViewModelFactory(firestoreModel, authModel)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        viewModel = MainActivityViewModel(firestoreModel, authModel)

        controller.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.onDestinationChanged(destination.id)
        }

        viewModel.showLogoutButton.observe(this) {
            binding.mainLogoutButton.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.showGoBackButton.observe(this) {
            binding.mainGoBackButton.visibility = if (it) View.VISIBLE else View.GONE
        }

        binding.mainLogoutButton.setOnClickListener {
            viewModel.performLogout()
            viewModel.navigateToLoginFragment(controller)
        }

        binding.mainGoBackButton.setOnClickListener {
            viewModel.goBack(controller)
        }
    }
}