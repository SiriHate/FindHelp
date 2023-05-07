package com.siri_hate.findhelp.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentUserProfileBinding
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.models.Skill
import com.siri_hate.findhelp.ui.adapters.UserProfileSkillsAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.UserProfileViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.UserProfileViewModel

class UserProfileFragment : Fragment(), UserProfileSkillsAdapter.UserProfileSkillsCallback {

    private lateinit var adapter: UserProfileSkillsAdapter
    private lateinit var controller: NavController
    private lateinit var binding: FragmentUserProfileBinding

    private val viewModel: UserProfileViewModel by viewModels {
        UserProfileViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        loading(true)

        controller = findNavController()

        viewModel.userCity.observe(viewLifecycleOwner) { userCity ->
            binding.userProfileCityInput.apply {
                setText(userCity)
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val newCity = text.toString()
                        viewModel.updateUserCity(newCity)

                        val inputMethodManager =
                            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
                        clearFocus()

                        true
                    } else {
                        false
                    }
                }
                imeOptions =
                    EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loading(isLoading)
        }

        viewModel.userSkills.observe(viewLifecycleOwner) { skillsList ->
            if (skillsList.isEmpty()) {
                binding.userProfileSkillList.visibility = View.GONE
                binding.userProfileEmptyListMessage.visibility = View.VISIBLE
            } else {
                adapter = UserProfileSkillsAdapter(requireContext(), this, skillsList)
                binding.userProfileSkillList.adapter = adapter
            }
        }

        binding.userProfileMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_item_home -> {
                    controller.navigate(R.id.action_userProfileFragment_to_userPageFragment)
                    true
                }

                R.id.bottom_navigation_item_profile -> {
                    true
                }

                else -> false
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userPageFragment -> {
                    binding.userProfileMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked =
                        true
                }

                R.id.userProfileFragment -> {
                    binding.userProfileMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }

        return binding.root
    }

    override fun onSkillChecked(skill: Skill, isChecked: Boolean) {
        viewModel.updateUserSkill(skill.name, isChecked)
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.userProfileLoadingProgressBar.visibility = View.VISIBLE
            binding.userProfileMainLayout.visibility = View.GONE
        } else {
            binding.userProfileLoadingProgressBar.visibility = View.GONE
            binding.userProfileMainLayout.visibility = View.VISIBLE
        }
    }
}
