package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class EditVacancyInfoFragment : Fragment() {

    private lateinit var documentId: String
    private lateinit var nameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var navController: NavController

    companion object {
        private const val DOCUMENT_ID_KEY = "document_id"
        private const val COLLECTION_VACANCIES_LIST = "vacancies_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(DOCUMENT_ID_KEY, "") ?: ""
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

        initViews(view)
        setupButtons()
        setVacancyValues()
        navController = findNavController()
    }

    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.edit_vacancy_main_fragment_name_input)
        cityEditText = view.findViewById(R.id.edit_vacancy_main_fragment_fragment_city_input)
        descriptionEditText = view.findViewById(R.id.edit_vacancy_main_fragment_description_input)
        continueButton = view.findViewById(R.id.edit_vacancy_main_fragment_continue_button)
    }

    private fun setupButtons() {

        continueButton.setOnClickListener {
            if (isInputValid()) {
                updateVacancy()
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

    private fun setVacancyValues() {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_VACANCIES_LIST)
            .document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                nameEditText.setText(documentSnapshot.getString("vacancy_name"))
                cityEditText.setText(documentSnapshot.getString("vacancy_city"))
                descriptionEditText.setText(documentSnapshot.getString("vacancy_description"))
            }
    }

    private fun updateVacancy() {
        val vacancyRef = FirebaseFirestore.getInstance()
            .collection(COLLECTION_VACANCIES_LIST)
            .document(documentId)

        val updates = hashMapOf(
            "vacancy_name" to nameEditText.text.toString(),
            "vacancy_city" to cityEditText.text.toString(),
            "vacancy_description" to descriptionEditText.text.toString()
        )

        vacancyRef.update(updates as Map<String, Any>).addOnSuccessListener {
            Log.d("EditVacancyMainFragment", "Изменения сохранены")
        }
    }

    private fun navigateToSecondFragment() {
        val bundle = Bundle().apply { putString(DOCUMENT_ID_KEY, documentId) }
        navController.navigate(R.id.action_editVacancyMainFragment_to_editVacancySecondFragment, bundle)
    }
}


