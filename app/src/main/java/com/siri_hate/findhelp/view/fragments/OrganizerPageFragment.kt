package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.adapters.OrganizerVacancyListAdapter
import com.siri_hate.findhelp.viewmodel.fragments.OrganizerPageViewModel
import java.util.Locale

class OrganizerPageFragment : Fragment() {

    private lateinit var organizerPageAddVacancyButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrganizerVacancyListAdapter
    private lateinit var organizerPageSearchBar: SearchView

    private lateinit var viewModel: OrganizerPageViewModel

    private val controller by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_organizer_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        organizerPageAddVacancyButton = view.findViewById(R.id.organizer_page_add_vacancy_button)
        recyclerView = view.findViewById(R.id.organizer_page_vacancy_list)
        organizerPageSearchBar = view.findViewById(R.id.organizer_page_search_bar)

        adapter = OrganizerVacancyListAdapter(requireContext(), emptyList(), controller)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(this)[OrganizerPageViewModel::class.java]
        viewModel.initVacanciesListener()

        viewModel.vacanciesLiveData.observe(viewLifecycleOwner) { vacancies ->
            adapter.updateVacancies(vacancies)
        }

        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        organizerPageAddVacancyButton.setOnClickListener {
            controller.navigate(R.id.action_organizerPageFragment_to_createVacancyMainFragment)
        }

        organizerPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val query = newText.lowercase(Locale.getDefault())
                val originalList = viewModel.vacanciesLiveData.value ?: emptyList()
                val filteredList = viewModel.filterVacancies(query, originalList)
                adapter.updateVacancies(filteredList)
                return true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

}
