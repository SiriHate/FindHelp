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
import com.siri_hate.findhelp.databinding.FragmentModeratorPageBinding
import com.siri_hate.findhelp.view.adapters.ModeratorVacancyListAdapter
import com.siri_hate.findhelp.viewmodel.fragments.ModeratorPageViewModel
import java.util.*

class ModeratorPageFragment : Fragment() {

    private lateinit var viewModel: ModeratorPageViewModel
    private lateinit var adapter: ModeratorVacancyListAdapter
    private lateinit var controller: NavController
    private lateinit var binding: FragmentModeratorPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeratorPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = findNavController()

        adapter = ModeratorVacancyListAdapter(controller)
        binding.moderatorVacancyList.adapter = adapter

        viewModel = ViewModelProvider(this)[ModeratorPageViewModel::class.java]
        viewModel.initSnapshotListener()

        viewModel.offersLiveData.observe(viewLifecycleOwner) { offers ->
            adapter.submitList(offers)
        }

        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.emptyListLiveData.observe(viewLifecycleOwner) { isEmpty ->
            if (isEmpty) {
                binding.moderatorPageEmptyListMessage.visibility = View.VISIBLE
                binding.moderatorVacancyList.visibility = View.GONE
            } else {
                binding.moderatorPageEmptyListMessage.visibility = View.GONE
                binding.moderatorVacancyList.visibility = View.VISIBLE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { LoadingStatus ->
            if (LoadingStatus) {
                binding.moderatorPageLoadingProgressBar.visibility = View.VISIBLE
                binding.moderatorVacancyList.visibility = View.GONE
            } else {
                binding.moderatorPageLoadingProgressBar.visibility = View.GONE
                binding.moderatorVacancyList.visibility = View.VISIBLE
            }
        }

        binding.moderatorPageSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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


