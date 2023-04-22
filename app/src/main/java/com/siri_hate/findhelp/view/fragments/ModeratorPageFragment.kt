package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter
import com.siri_hate.findhelp.viewmodel.fragments.ModeratorPageViewModel
import java.util.*

class ModeratorPageFragment : Fragment() {

    private lateinit var searchBar: SearchView
    private lateinit var moderatorVacancyList: RecyclerView

    private lateinit var viewModel: ModeratorPageViewModel
    private lateinit var adapter: ModeratorVacancyListAdapter

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

        viewModel = ViewModelProvider(this)[ModeratorPageViewModel::class.java]
        viewModel.initSnapshotListener()

        viewModel.offersLiveData.observe(viewLifecycleOwner) { offers ->
            adapter.submitList(offers)
        }

        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase(Locale.getDefault()) ?: ""
                viewModel.filterOffers(query)
                return true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }
}


