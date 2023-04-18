package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.viewmodel.fragments.CreateVacancyInfoViewModel

class CreateVacancyInfoFragment : Fragment() {

    // View elements
    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var createVacancyButton: Button
    private lateinit var controller: NavController

    // ViewModel
    private lateinit var viewModel: CreateVacancyInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_vacancy_info, container, false)
        initViews(view)
        controller = findNavController()
        return view
    }

    private fun initViews(view: View) {
        nameInput = view.findViewById(R.id.create_vacancy_info_fragment_name_input)
        cityInput = view.findViewById(R.id.create_vacancy_info_fragment_city_input)
        descriptionInput = view.findViewById(R.id.create_vacancy_info_fragment_description_input)
        createVacancyButton = view.findViewById(R.id.create_vacancy_info_fragment_continue_button)

        setClickListeners()

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[CreateVacancyInfoViewModel::class.java]
    }

    private fun setClickListeners() {
        createVacancyButton.setOnClickListener {
            val name = nameInput.text.toString()
            val city = cityInput.text.toString()
            val description = descriptionInput.text.toString()

            viewModel.createNewVacancy(name, city, description,
                onSuccess = { vacancyId ->
                    val bundle = Bundle()
                    bundle.putString("document_id", vacancyId)
                    controller.navigate(
                        R.id.action_createVacancyMainFragment_to_createVacancySecondFragment,
                        bundle
                    )
                },
                onFailure = {
                    Toast.makeText(context, "Не удалось создать вакансию", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
