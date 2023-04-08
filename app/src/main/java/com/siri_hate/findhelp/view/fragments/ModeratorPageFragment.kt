package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter
import java.util.*

class ModeratorPageFragment : Fragment(R.layout.fragment_moderator_page) {

    private lateinit var searchBar: SearchView
    private lateinit var moderatorVacancyList: ListView

    private val db = FirebaseFirestore.getInstance()
    private val offersRef = db.collection("vacancies_list")
    private lateinit var adapter: ModeratorVacancyListAdapter
    private var snapshotListener: ListenerRegistration? = null
    private var offers: List<DocumentSnapshot> = emptyList()
    private var originalOffers: List<DocumentSnapshot> = emptyList()
    private lateinit var controller: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Переменные UI-элементов
        moderatorVacancyList = view.findViewById(R.id.moderator_vacancy_list)
        searchBar = view.findViewById(R.id.moderator_page_search_bar)

        controller = findNavController()

        // Адаптер
        adapter = ModeratorVacancyListAdapter(requireContext(), emptyList(), controller)
        moderatorVacancyList.adapter = adapter

        // Слушатель изменения текста в searchBar
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Получение текста из SearchView
                val query = newText?.lowercase(Locale.getDefault()) ?: ""

                // Фильтрация списка вакансий по запросу
                val filteredOffers = offers.filter {
                    it.getString("vacancy_name")?.lowercase(Locale.getDefault())
                        ?.startsWith(query) == true
                }

                // Обновление адаптера с отфильтрованным списком вакансий
                adapter.clear()
                adapter.addAll(filteredOffers)
                adapter.notifyDataSetChanged()

                return true
            }
        })

        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            originalOffers = snapshots?.documents?.toList() ?: emptyList()
            offers = originalOffers
            adapter.clear()
            adapter.addAll(offers)
            adapter.notifyDataSetChanged()
        }

        // Слушатель изменений коллекции Firestore
        snapshotListener = offersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val offers = snapshots?.documents?.toList() ?: emptyList<DocumentSnapshot>()
            adapter.clear()
            adapter.addAll(offers)
            adapter.notifyDataSetChanged()
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
