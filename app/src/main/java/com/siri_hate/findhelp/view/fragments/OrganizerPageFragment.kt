package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentOrganizerPageBinding
import com.siri_hate.findhelp.model.firebase.FirebaseFirestoreModel
import com.siri_hate.findhelp.view.adapters.OrganizerVacancyListAdapter
import com.siri_hate.findhelp.viewmodel.factory.OrganizerPageViewModelFactory
import com.siri_hate.findhelp.viewmodel.fragments.OrganizerPageViewModel
import java.util.Locale

class OrganizerPageFragment : Fragment() {

    private lateinit var adapter: OrganizerVacancyListAdapter
    private lateinit var viewModel: OrganizerPageViewModel
    private lateinit var binding: FragmentOrganizerPageBinding

    private val controller by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrganizerPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrganizerVacancyListAdapter(requireContext(), emptyList(), controller)
        binding.organizerPageVacancyList.adapter = adapter

        val firebaseFirestoreModel = FirebaseFirestoreModel()
        val viewModelFactory = OrganizerPageViewModelFactory(firebaseFirestoreModel)
        viewModel = ViewModelProvider(this, viewModelFactory)[OrganizerPageViewModel::class.java]

        viewModel.initVacanciesListener()

        viewModel.vacanciesLiveData.observe(viewLifecycleOwner) { vacancies ->
            adapter.updateVacancies(vacancies)
        }

        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.loading.observe(viewLifecycleOwner) { loadingStatus ->
            if (loadingStatus) {
                binding.organizerPageVacancyList.visibility = View.GONE
                binding.organizerPageLoadingProgressBar.visibility = View.VISIBLE
           }
            else {
                binding.organizerPageVacancyList.visibility = View.VISIBLE
                binding.organizerPageLoadingProgressBar.visibility = View.GONE
            }
        }

        viewModel.emptyListLiveData.observe(viewLifecycleOwner) { isEmpty ->
           showEmptyListMessage(isEmpty)
        }

        binding.organizerPageAddVacancyButton.setOnClickListener {
            controller.navigate(R.id.action_organizerPageFragment_to_createVacancyMainFragment)
        }

        binding.organizerPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val query = newText.lowercase(Locale.getDefault())
                val originalList = viewModel.vacanciesLiveData.value ?: emptyList()
                val filteredList = viewModel.filterVacancies(query, originalList)
                adapter.updateVacancies(filteredList)

                showEmptyListMessage(filteredList.isEmpty())

                return true
            }
        })
    }

    private fun showEmptyListMessage(isEmpty: Boolean) {
        if (isEmpty) {
            binding.organizerPageEmptyListMessage.visibility = View.VISIBLE
            binding.organizerPageVacancyList.visibility = View.GONE
        } else {
            binding.organizerPageEmptyListMessage.visibility = View.GONE
            binding.organizerPageVacancyList.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

}
