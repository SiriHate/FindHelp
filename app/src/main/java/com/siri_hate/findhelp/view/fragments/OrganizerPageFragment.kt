package com.siri_hate.findhelp.view.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.OrganizerVacancyListAdapter

class OrganizerPageFragment : Fragment() {

    private lateinit var organizerPageAddVacancyButton: ImageButton
    private lateinit var organizerPageLogoutButton: Button
    private lateinit var listView: ListView
    private lateinit var adapter: OrganizerVacancyListAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var controller: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        controller = findNavController()
        return inflater.inflate(R.layout.organizer_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        organizerPageAddVacancyButton = view.findViewById(R.id.organizer_page_add_vacancy_button)
        organizerPageLogoutButton = view.findViewById(R.id.organizer_page_logout_button)
        listView = view.findViewById(R.id.organizer_page_vacancy_list)
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email ?: ""

        organizerPageAddVacancyButton.setOnClickListener {
            controller.navigate(R.id.action_organizerPageFragment_to_createVacancyMainFragment)
        }

        organizerPageLogoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            controller.navigate(R.id.action_organizerPageFragment_to_loginFragment)
        }

        database.collection("vacancies_list")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val offers = mutableListOf<DocumentSnapshot>()
                for (doc in value!!) {
                    offers.add(doc)
                }

                val filteredOffers = offers.filter {
                    it.getString("creator_email") == userEmail
                }

                adapter = OrganizerVacancyListAdapter(requireContext(), filteredOffers, controller)
                listView.adapter = adapter

                val searchbar = view.findViewById<EditText>(R.id.organizer_page_search_bar)
                searchbar.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // Ничего не делаем
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val filteredList = filteredOffers.filter {
                            it.getString("vacancy_name")
                                ?.startsWith(s.toString(), ignoreCase = true) ?: false
                        }

                        adapter.clear()
                        adapter.addAll(filteredList)
                        adapter.notifyDataSetChanged()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // Ничего не делаем
                    }
                })
            }
    }
}