package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var goBackButton: ImageButton
    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        documentId = arguments?.getString("document_id") ?: ""
        setVacancyValues()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_vacancy_info, container, false)
        bindViews(view)
        setupButtons()
        controller = findNavController()
        return view
    }

    private fun setVacancyValues() {
        FirebaseFirestore.getInstance().collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                nameEditText.setText(documentSnapshot.getString("vacancy_name"))
                cityEditText.setText(documentSnapshot.getString("vacancy_city"))
                descriptionEditText.setText(documentSnapshot.getString("vacancy_description"))
            }
    }

    private fun bindViews(view: View) {
        nameEditText = view.findViewById(R.id.edit_vacancy_main_fragment_name_input)
        cityEditText = view.findViewById(R.id.edit_vacancy_main_fragment_fragment_city_input)
        descriptionEditText = view.findViewById(R.id.edit_vacancy_main_fragment_description_input)
        continueButton = view.findViewById(R.id.edit_vacancy_main_fragment_continue_button)
        goBackButton = view.findViewById(R.id.edit_vacancy_main_fragment_go_back_button)
    }

    private fun setupButtons() {
        goBackButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("document_id", documentId)
            controller.navigate(R.id.action_editVacancyMainFragment_to_vacancyCardFragment, bundle)
        }

        continueButton.setOnClickListener {
            if (isInputValid()) {
                updateVacancy()
                navigateToSecondFragment()
            }
        }
    }

    private fun isInputValid(): Boolean {
        var isValid = true
        if (nameEditText.text.isBlank()) {
            nameEditText.error = "Поле не может быть пустым"
            isValid = false
        }
        if (cityEditText.text.isBlank()) {
            cityEditText.error = "Поле не может быть пустым"
            isValid = false
        }
        if (descriptionEditText.text.isBlank()) {
            descriptionEditText.error = "Поле не может быть пустым"
            isValid = false
        }
        return isValid
    }

    private fun updateVacancy() {
        val vacancyRef =
            FirebaseFirestore.getInstance().collection("vacancies_list").document(documentId)
        val updates = hashMapOf<String, Any>(
            "vacancy_name" to nameEditText.text.toString(),
            "vacancy_city" to cityEditText.text.toString(),
            "vacancy_description" to descriptionEditText.text.toString()
        )
        vacancyRef.update(updates).addOnSuccessListener {
            Log.d("EditVacancyMainFragment", "Изменения сохранены")
        }
    }

    private fun navigateToSecondFragment() {
        val bundle = Bundle()
        bundle.putString("document_id", documentId)
        controller.navigate(
            R.id.action_editVacancyMainFragment_to_editVacancySecondFragment,
            bundle
        )
    }
}
