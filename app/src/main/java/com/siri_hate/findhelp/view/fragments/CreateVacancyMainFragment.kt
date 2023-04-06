package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class CreateVacancyMainFragment : Fragment() {
    private lateinit var vacancyNameInput: EditText
    private lateinit var vacancyCityInput: EditText
    private lateinit var vacancyDescriptionInput: EditText
    private lateinit var createVacancyButton: Button
    private lateinit var newVacancyMainFragmentGoBackButton: ImageButton
    private lateinit var controller: NavController

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_vacancy_main_fragment, container, false)
        initViews(view)
        controller = findNavController()
        return view
    }

    private fun initViews(view: View) {
        vacancyNameInput = view.findViewById(R.id.new_vacancy_main_fragment_name_input)
        vacancyCityInput = view.findViewById(R.id.new_vacancy_main_fragment_fragment_city_input)
        vacancyDescriptionInput =
            view.findViewById(R.id.new_vacancy_main_fragment_description_input)
        createVacancyButton = view.findViewById(R.id.new_vacancy_main_fragment_continue_button)
        newVacancyMainFragmentGoBackButton =
            view.findViewById(R.id.new_vacancy_main_fragment_go_back_button)

        newVacancyMainFragmentGoBackButton.setOnClickListener {
            goBackToOrganizerPage()
        }

        createVacancyButton.setOnClickListener {
            if (isInputValid()) {
                val orgInfoDocRef = fetchOrganizationInfoDoc()
                orgInfoDocRef.get().addOnSuccessListener { orgInfoDoc ->
                    if (orgInfoDoc.exists()) {
                        val (contactPerson, organizationName, organizationPhone) = getOrganizationInfo(
                            orgInfoDoc
                        )
                        val skills = fetchInitData()

                        val vacancyDocRef = firestore.collection("vacancies_list").document()
                        val newVacancy = hashMapOf(
                            "creator_email" to currentUserEmail,
                            "contact_person" to contactPerson,
                            "organization_name" to organizationName,
                            "organization_phone" to organizationPhone,
                            "vacancy_name" to vacancyNameInput.text.toString(),
                            "vacancy_city" to vacancyCityInput.text.toString(),
                            "vacancy_description" to vacancyDescriptionInput.text.toString(),
                            "vacancy_skills_list" to skills
                        )
                        vacancyDocRef.set(newVacancy).addOnSuccessListener {
                            // Переключение на NewVacancySecondFragment с передачей ID документа
                            val bundle = Bundle()
                            bundle.putString("vacancy_id", vacancyDocRef.id)
                            controller.navigate(R.id.action_createVacancyMainFragment_to_createVacancySecondFragment)
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Необходимо заполнить все поля!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isInputValid(): Boolean {
        return vacancyNameInput.text.toString().isNotEmpty() && vacancyCityInput.text.toString()
            .isNotEmpty() && vacancyDescriptionInput.text.toString().isNotEmpty()
    }

    private fun fetchOrganizationInfoDoc(): DocumentReference {
        return firestore.collection("organization_info").document(currentUserEmail!!)
    }

    private fun getOrganizationInfo(orgInfoDoc: DocumentSnapshot): Triple<String, String, String> {
        val contactPerson = orgInfoDoc.getString("contact_person") ?: ""
        val organizationName = orgInfoDoc.getString("organization_name") ?: ""
        val organizationPhone = orgInfoDoc.getString("organization_phone") ?: ""

        return Triple(contactPerson, organizationName, organizationPhone)
    }

    private fun fetchInitData(): HashMap<*, *> {
        val initdataDocRef = firestore.collection("init_data").document("base_skills_init")
        val initdataDoc = initdataDocRef.get().result
        return initdataDoc?.get("skills") as HashMap<*, *>
    }

    private fun goBackToOrganizerPage() {
        controller.navigate(R.id.action_createVacancyMainFragment_to_organizerPageFragment)
    }
}