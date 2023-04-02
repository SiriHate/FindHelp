package com.siri_hate.findhelp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.VacancyCardActivity

class EditVacancyMainFragment : Fragment() {

    private lateinit var documentId: String
    private lateinit var nameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var goBackButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        documentId = requireActivity().intent.getStringExtra("document_id") ?: ""

        // Получаем ссылку на документ и устанавливаем значения в EditText
        FirebaseFirestore.getInstance().collection("vacancies_list").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                nameEditText.setText(documentSnapshot.getString("vacancy_name"))
                cityEditText.setText(documentSnapshot.getString("vacancy_city"))
                descriptionEditText.setText(documentSnapshot.getString("vacancy_description"))
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.edit_vacancy_main_fragment, container, false)

        nameEditText = view.findViewById(R.id.edit_vacancy_main_fragment_name_input)
        cityEditText = view.findViewById(R.id.edit_vacancy_main_fragment_fragment_city_input)
        descriptionEditText = view.findViewById(R.id.edit_vacancy_main_fragment_description_input)
        continueButton = view.findViewById(R.id.edit_vacancy_main_fragment_continue_button)
        goBackButton = view.findViewById(R.id.edit_vacancy_main_fragment_go_back_button)

        goBackButton.setOnClickListener {
                val intent = Intent(context, VacancyCardActivity::class.java)
                intent.putExtra("document_id", documentId)
                context?.startActivity(intent)
        }

        continueButton.setOnClickListener {
            // Проверяем поля на пустоту и устанавливаем соответствующие ошибки, если поле не заполнено
            var isNameValid = true
            var isCityValid = true
            var isDescriptionValid = true

            if (nameEditText.text.isBlank()) {
                nameEditText.error = "Поле не может быть пустым"
                isNameValid = false
            }
            if (cityEditText.text.isBlank()) {
                cityEditText.error = "Поле не может быть пустым"
                isCityValid = false
            }
            if (descriptionEditText.text.isBlank()) {
                descriptionEditText.error = "Поле не может быть пустым"
                isDescriptionValid = false
            }

            // Если все поля заполнены, то обновляем значения в документе
            if (isNameValid && isCityValid && isDescriptionValid) {
                val vacancyRef = FirebaseFirestore.getInstance().collection("vacancies_list").document(documentId)
                val updates = hashMapOf<String, Any>(
                    "vacancy_name" to nameEditText.text.toString(),
                    "vacancy_city" to cityEditText.text.toString(),
                    "vacancy_description" to descriptionEditText.text.toString()
                )
                vacancyRef.update(updates).addOnSuccessListener {
                    Log.d("EditVacancyMainFragment", "Изменения сохранены")
                }
            }
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.edit_vacancy_page_fragment_layout, EditVacancySecondFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}