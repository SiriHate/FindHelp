package com.siri_hate.findhelp.view.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentUserProfileBinding
import com.siri_hate.findhelp.model.models.Skill
import com.siri_hate.findhelp.view.adapters.UserProfileSkillsAdapter

class UserProfileFragment : Fragment() {
    private lateinit var adapter: UserProfileSkillsAdapter
    private lateinit var controller: NavController
    private lateinit var binding: FragmentUserProfileBinding

    companion object {
        private const val SKILLS_COLLECTION = "skills"
        private const val USER_INFO_COLLECTION = "user_info"
        private const val USER_CITY_FIELD = "user_city"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        loading(true)

        controller = findNavController()

        val db = FirebaseFirestore.getInstance()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

        db.collection(USER_INFO_COLLECTION).document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                loading(false)
                val userCity = documentSnapshot.getString(USER_CITY_FIELD)
                binding.userProfileCityInput.apply {
                    setText(userCity)
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            val newCity = text.toString()
                            db.collection(USER_INFO_COLLECTION).document(userEmail)
                                .update(USER_CITY_FIELD, newCity)

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

        binding.userProfileCityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newCity = binding.userProfileCityInput.text.toString()
                db.collection(USER_INFO_COLLECTION).document(userEmail)
                    .update(USER_CITY_FIELD, newCity)
                true
            } else {
                false
            }
        }

        binding.userProfileMenu.setOnItemSelectedListener { item ->
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
                    binding.userProfileMenu.menu.findItem(R.id.bottom_navigation_item_home).isChecked = true
                }
                R.id.userProfileFragment -> {
                    binding.userProfileMenu.menu.findItem(R.id.bottom_navigation_item_profile).isChecked =
                        true
                }
            }
        }

        db.collection(USER_INFO_COLLECTION).document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot?.get(SKILLS_COLLECTION) as? Map<String, Boolean>
                val skillsList = skillsMap?.map { Skill(it.key, it.value) } ?: emptyList()

                if (skillsList.isEmpty()) {
                    binding.userProfileSkillList.visibility = View.GONE
                    binding.userProfileEmptyListMessage.visibility = View.VISIBLE
                } else {
                    adapter = UserProfileSkillsAdapter(requireContext(), db, userEmail, skillsList)
                    binding.userProfileSkillList.adapter = adapter
                }
            }

        return binding.root
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