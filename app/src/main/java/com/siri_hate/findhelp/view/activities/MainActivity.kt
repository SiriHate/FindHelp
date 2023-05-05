package com.siri_hate.findhelp.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.ActivityMainBinding
import com.siri_hate.findhelp.viewmodel.activities.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment_container) as NavHostFragment

        controller = navHostFragment.navController

        db = FirebaseFirestore.getInstance()

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        viewModel.initialize(db)

        controller.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.onDestinationChanged(destination.id)
        }

        viewModel.showLogoutButton.observe(this) {
            binding.mainLogoutButton.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        viewModel.showGoBackButton.observe(this) {
            binding.mainGoBackButton.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        binding.mainLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            viewModel.navigateToLoginFragment(controller)
        }

        binding.mainGoBackButton.setOnClickListener {
            viewModel.goBack(controller)
        }
    }
}