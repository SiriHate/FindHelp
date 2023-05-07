package com.siri_hate.findhelp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.util.Locale
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.databinding.FragmentOrganizerPageBinding
import com.siri_hate.findhelp.ui.adapters.OrganizerVacancyListAdapter
import com.siri_hate.findhelp.ui.viewmodels.factories.OrganizerPageViewModelFactory
import com.siri_hate.findhelp.ui.viewmodels.fragments.OrganizerPageViewModel
import com.siri_hate.findhelp.data.remote.FirebaseFirestoreModel
import com.siri_hate.findhelp.data.remote.FirebaseAuthModel
class OrganizerPageFragment : Fragment() {

    private lateinit var adapter: OrganizerVacancyListAdapter
    private lateinit var binding: FragmentOrganizerPageBinding
    private val controller by lazy { findNavController() }

    private val viewModel: OrganizerPageViewModel by viewModels {
        OrganizerPageViewModelFactory(
            FirebaseAuthModel(),
            FirebaseFirestoreModel()
        )
    }

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

        val userEmail = viewModel.getUserEmail()
        adapter = OrganizerVacancyListAdapter(requireContext(), emptyList(), controller, userEmail)
        binding.organizerPageVacancyList.adapter = adapter

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
                if (query.isEmpty()) {
                    viewModel.restartVacanciesListener()
                } else {
                    viewModel.filterVacanciesByQuery(query)
                }
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
