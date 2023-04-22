package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.viewmodel.fragments.EditVacancyInfoViewModel

class EditVacancyInfoFragment : Fragment() {

    private lateinit var documentId: String
    private lateinit var nameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var navController: NavController

    private lateinit var viewModel: EditVacancyInfoViewModel

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(DOCUMENT_ID_KEY, "") ?: ""
            Log.d("My_check2", documentId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_vacancy_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditVacancyInfoViewModel::class.java]
        initViews(view)
        setupButtons()
        viewModel.setVacancyValues(FirebaseFirestore.getInstance(), documentId)
        navController = findNavController()

        viewModel.vacancyName.observe(viewLifecycleOwner) { name ->
            nameEditText.setText(name)
        }
        viewModel.vacancyCity.observe(viewLifecycleOwner) { city ->
            cityEditText.setText(city)
        }
        viewModel.vacancyDescription.observe(viewLifecycleOwner) { description ->
            descriptionEditText.setText(description)
        }
    }

    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.edit_vacancy_info_fragment_name_input)
        cityEditText = view.findViewById(R.id.edit_vacancy_info_fragment_city_input)
        descriptionEditText = view.findViewById(R.id.edit_vacancy_info_fragment_description_input)
        continueButton = view.findViewById(R.id.edit_vacancy_info_fragment_continue_button)
    }

    private fun setupButtons() {

        continueButton.setOnClickListener {
            if (isInputValid()) {
                viewModel.updateVacancy(
                    FirebaseFirestore.getInstance(),
                    documentId,
                    nameEditText.text.toString(),
                    cityEditText.text.toString(),
                    descriptionEditText.text.toString()
                )
                navigateToSecondFragment()
            }
        }

    }

    private fun isInputValid(): Boolean {
        val isEmptyName = nameEditText.text.isBlank()
        val isEmptyCity = cityEditText.text.isBlank()
        val isEmptyDescription = descriptionEditText.text.isBlank()

        if (isEmptyName) nameEditText.error = "Поле не может быть пустым"
        if (isEmptyCity) cityEditText.error = "Поле не может быть пустым"
        if (isEmptyDescription) descriptionEditText.error = "Поле не может быть пустым"

        return !isEmptyName && !isEmptyCity && !isEmptyDescription
    }

    private fun navigateToSecondFragment() {
        val bundle = Bundle().apply { putString(DOCUMENT_ID_KEY, documentId) }
        navController.navigate(R.id.action_editVacancyMainFragment_to_editVacancySecondFragment, bundle)
    }
}


