package com.siri_hate.findhelp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.ActivityMainBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.ui.viewmodels.factories.MainActivityViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(
            FirebaseFirestoreModel(),
            FirebaseAuthModel()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container) as NavHostFragment
        controller = navHostFragment.navController

        setupListeners()
        setupObservers()

    }

    private fun setupObservers() {

        viewModel.showLogoutButton.observe(this) {
            binding.mainLogoutButton.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.showGoBackButton.observe(this) {
            binding.mainGoBackButton.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.editVacancyExit.observe(this) { status ->
            if (status) {
                editVacancyCardExit()
            }
        }

    }

    private fun setupListeners() {

        controller.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.onDestinationChanged(destination.id)
        }

        binding.mainLogoutButton.setOnClickListener {
            viewModel.performLogout()
            viewModel.navigateToLoginFragment(controller)
        }

        binding.mainGoBackButton.setOnClickListener {
            viewModel.goBack(controller)
        }

    }

    private fun editVacancyCardExit() {
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        val bundle = currentFragment?.arguments
        controller.navigate(R.id.action_editVacancyMainFragment_to_vacancyCardFragment, bundle)
    }

}