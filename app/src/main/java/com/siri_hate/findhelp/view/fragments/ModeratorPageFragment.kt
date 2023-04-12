package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter
import java.util.*

class ModeratorPageFragment : Fragment() {

    private lateinit var searchBar: SearchView
    private lateinit var moderatorVacancyList: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val offersRef = db.collection("vacancies_list")
    private lateinit var adapter: ModeratorVacancyListAdapter
    private var snapshotListener: ListenerRegistration? = null
    private var originalOffers: List<DocumentSnapshot> = emptyList()
    private lateinit var controller: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_moderator_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moderatorVacancyList = view.findViewById(R.id.moderator_vacancy_list)
        searchBar = view.findViewById(R.id.moderator_page_search_bar)

        controller = findNavController()

        moderatorVacancyList.layoutManager = LinearLayoutManager(requireContext())

        adapter = ModeratorVacancyListAdapter(controller)
        moderatorVacancyList.adapter = adapter

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase(Locale.getDefault()) ?: ""

                val filteredOffers = originalOffers.filter {
                    it.getString("vacancy_name")?.lowercase(Locale.getDefault())
                        ?.startsWith(query) == true
                }

                adapter.submitList(filteredOffers)

                return true
            }
        })

        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            originalOffers = snapshots?.documents?.toList() ?: emptyList()
            adapter.submitList(originalOffers)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    companion object {
        private const val TAG = "ModeratorPageFragment"
    }
}


