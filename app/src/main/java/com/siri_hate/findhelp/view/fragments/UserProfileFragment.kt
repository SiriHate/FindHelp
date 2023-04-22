package com.siri_hate.findhelp.view.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.UserProfileSkillsAdapter
import com.siri_hate.findhelp.viewmodel.fragments.UserProfileViewModel

class UserProfileFragment : Fragment() {
    private lateinit var userProfileSkillList: RecyclerView
    private lateinit var adapter: UserProfileSkillsAdapter
    private lateinit var controller: NavController
    private lateinit var userProfileMenu: BottomNavigationView
    private lateinit var cityInput: EditText

    private lateinit var viewModel: UserProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        userProfileSkillList = view.findViewById(R.id.user_profile_skill_list)
        userProfileMenu = view.findViewById(R.id.user_profile_menu)
        cityInput = view.findViewById(R.id.user_profile_city_input)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        userProfileSkillList.layoutManager = layoutManager

        controller = findNavController()
        userProfileMenu.setupWithNavController(controller)

        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]

        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

        viewModel.loadUserCity(userEmail)
        viewModel.userCityLiveData.observe(viewLifecycleOwner) { userCity ->
            cityInput.apply {
                setText(userCity)
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val newCity = text.toString()
                        viewModel.updateUserCity(newCity, userEmail)

                        // Скрываем клавиатуру
                        val inputMethodManager =
                            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)

                        // Убираем фокус с EditText
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

        cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newCity = cityInput.text.toString()
                viewModel.updateUserCity(newCity, userEmail)
                true
            } else {
                false
            }
        }

        viewModel.loadSkillsList(userEmail)
        viewModel.skillsListLiveData.observe(viewLifecycleOwner) { skillsList ->
            adapter.updateSkillsList(skillsList)
        }

        userProfileMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_item_home -> {
                    controller.navigate(R.id.action_userProfileFragment_to_userPageFragment)
                    true
                }
                R.id.bottom_navigation_item_profile -> {
                    // Nothing
                    true
                }
                else -> false
            }
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userPageFragment -> {
                    userProfileMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked = true
                }
                R.id.userProfileFragment -> {
                    userProfileMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }

        adapter = UserProfileSkillsAdapter(requireContext(), db, userEmail, emptyList())
        userProfileSkillList.adapter = adapter

        return view
    }
}