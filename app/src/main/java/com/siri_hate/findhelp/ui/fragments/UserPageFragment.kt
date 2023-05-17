package com.siri_hate.findhelp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.databinding.FragmentUserPageBinding
import com.siri_hate.findhelp.ui.adapters.UserVacancyListAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.UserPageViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.UserPageViewModel

class UserPageFragment : Fragment() {

    private val viewModel: UserPageViewModel by viewModels {
        UserPageViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }
    private lateinit var controller: NavController
    private lateinit var adapter: UserVacancyListAdapter
    private lateinit var binding: FragmentUserPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserPageBinding.inflate(inflater, container, false)

        controller = findNavController()
        showVacancyListState(isLoading = true, isVacancyListEmpty = true)
        viewModel.fetchCurrentUserDocument()

        setupNavigation()
        setupSearchBar()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, getString(message), Toast.LENGTH_SHORT).show()
        }

        viewModel.userSkills.observe(viewLifecycleOwner) { userSkills ->
            adapter = UserVacancyListAdapter(
                viewModel.filteredVacancies.value ?: mutableListOf(),
                userSkills,
                controller
            )
            binding.userPageVacancyList.adapter = adapter
        }

        viewModel.filteredVacancies.observe(viewLifecycleOwner) { filteredVacancies ->
            val isVacancyListEmpty = filteredVacancies.isEmpty()
            emptyVacancyListMessage(isVacancyListEmpty)
            adapter.updateList(filteredVacancies)
            showVacancyListState(false, isVacancyListEmpty)
        }

    }

    private fun emptyVacancyListMessage(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.userPageVacancyList.visibility = View.GONE
            binding.userPageEmptyListMessage.visibility = View.VISIBLE
        } else {
            binding.userPageVacancyList.visibility = View.VISIBLE
            binding.userPageEmptyListMessage.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        binding.userPageMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.bottom_navigation_item_home -> {
                    true
                }

                R.id.bottom_navigation_item_profile -> {
                    controller.navigate(R.id.action_userPageFragment_to_userProfileFragment)
                    true
                }

                else -> false
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userPageFragment -> {
                    binding.userPageMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked =
                        true
                }

                R.id.userProfileFragment -> {
                    binding.userPageMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }
    }

    private fun setupSearchBar() {
        binding.userPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterAndSortVacancies(newText ?: "")
                return true
            }
        })
    }

    private fun showVacancyListState(isLoading: Boolean, isVacancyListEmpty: Boolean) {
        if (isLoading) {
            binding.userPageLoadingProgressBar.visibility = View.VISIBLE
            binding.userPageVacancyList.visibility = View.GONE
            binding.userPageEmptyListMessage.visibility = View.GONE
        } else {
            if (isVacancyListEmpty) {
                binding.userPageEmptyListMessage.visibility = View.VISIBLE
                binding.userPageVacancyList.visibility = View.GONE
            } else {
                binding.userPageEmptyListMessage.visibility = View.GONE
                binding.userPageVacancyList.visibility = View.VISIBLE
            }
            binding.userPageLoadingProgressBar.visibility = View.GONE
        }
    }

}