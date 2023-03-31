package com.siri_hate.findhelp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.OrganizerPageActivity

class NewVacancyMainFragment : Fragment() {
    private lateinit var vacancyNameInput: EditText
    private lateinit var vacancyCityInput: EditText
    private lateinit var vacancyDescriptionInput: EditText
    private lateinit var createVacancyButton: Button
    private lateinit var newVacancyMainFragmentGoBackButton: ImageButton

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.new_vacancy_main_fragment, container, false)

        vacancyNameInput = view.findViewById(R.id.new_vacancy_main_fragment_name_input)
        vacancyCityInput = view.findViewById(R.id.new_vacancy_main_fragment_fragment_city_input)
        vacancyDescriptionInput = view.findViewById(R.id.new_vacancy_main_fragment_description_input)
        createVacancyButton = view.findViewById(R.id.new_vacancy_main_fragment_continue_button)
        newVacancyMainFragmentGoBackButton = view.findViewById(R.id.new_vacancy_main_fragment_go_back_button)

        newVacancyMainFragmentGoBackButton.setOnClickListener {
            requireActivity().finish()
            startActivity(Intent(requireActivity(), OrganizerPageActivity::class.java))
        }


        createVacancyButton.setOnClickListener {
            val organizationInfoDocRef =
                firestore.collection("organization_info").document(currentUserEmail!!)
            organizationInfoDocRef.get().addOnSuccessListener { orgInfoDoc ->
                if (orgInfoDoc.exists()) {
                    val contactPerson = orgInfoDoc.getString("contact_person") ?: ""
                    val organizationName = orgInfoDoc.getString("organization_name") ?: ""
                    val organizationPhone = orgInfoDoc.getString("organization_phone") ?: ""

                    // Fetch the base_skills_list document from the init_data collection
                    val initdataDocRef = firestore.collection("init_data")
                        .document("base_skills_init")
                    initdataDocRef.get().addOnSuccessListener { initdataDoc ->
                        if (initdataDoc.exists()) {
                            val skills = initdataDoc.get("skills") as HashMap<*, *>

                            val vacancyDocRef = firestore
                                .collection("vacancies_list").document()
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
                                val fragment = NewVacancySecondFragment()
                                fragment.arguments = bundle
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.create_vacancy_page_fragment_layout, fragment)
                                    .commit()
                            }
                        }
                    }
                }
            }
        }

        return view
    }
}