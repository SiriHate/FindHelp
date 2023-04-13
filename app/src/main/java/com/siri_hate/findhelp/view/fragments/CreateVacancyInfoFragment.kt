package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import android.util.Log
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

class CreateVacancyInfoFragment : Fragment() {

    // View elements
    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var createVacancyButton: Button
    private lateinit var goBackButton: ImageButton
    private lateinit var controller: NavController

    // Firebase references
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    // Constants
    companion object {
        const val ORGANIZATION_INFO_COLLECTION = "organization_info"
        const val INIT_DATA_COLLECTION = "init_data"
        const val BASE_SKILLS_INIT_DOC_ID = "base_skills_init"
        const val TAG = "CreateVacancyInfoFragment"
    }

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
        goBackButton = view.findViewById(R.id.create_vacancy_info_fragment_go_back_button)

        setClickListeners()
    }

    private fun setClickListeners() {
        goBackButton.setOnClickListener {
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
                        fetchInitData { skills ->
                            val vacancyDocRef = firestore.collection("vacancies_list").document()
                            val newVacancy = hashMapOf(
                                "creator_email" to currentUserEmail,
                                "contact_person" to contactPerson,
                                "organization_name" to organizationName,
                                "organization_phone" to organizationPhone,
                                "vacancy_name" to nameInput.text.toString(),
                                "vacancy_city" to cityInput.text.toString(),
                                "vacancy_description" to descriptionInput.text.toString(),
                                "vacancy_skills_list" to skills
                            )
                            vacancyDocRef.set(newVacancy).addOnSuccessListener {
                                val bundle = Bundle()
                                bundle.putString("document_id", vacancyDocRef.id)
                                controller.navigate(
                                    R.id.action_createVacancyMainFragment_to_createVacancySecondFragment,
                                    bundle
                                )
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Необходимо заполнить все поля!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isInputValid(): Boolean {
        return nameInput.text.toString().isNotEmpty() && cityInput.text.toString()
            .isNotEmpty() && descriptionInput.text.toString().isNotEmpty()
    }

    private fun fetchOrganizationInfoDoc(): DocumentReference {
        return firestore.collection(ORGANIZATION_INFO_COLLECTION).document(currentUserEmail!!)
    }

    private fun getOrganizationInfo(orgInfoDoc: DocumentSnapshot): Triple<String, String, String> {
        val contactPerson = orgInfoDoc.getString("contact_person") ?: ""
        val organizationName = orgInfoDoc.getString("organization_name") ?: ""
        val organizationPhone = orgInfoDoc.getString("organization_phone") ?: ""

        return Triple(contactPerson, organizationName, organizationPhone)
    }

    private fun fetchInitData(callback: (HashMap<String, Any>) -> Unit) {
        val initdataDocRef =
            firestore.collection(INIT_DATA_COLLECTION).document(BASE_SKILLS_INIT_DOC_ID)
        initdataDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                callback(documentSnapshot.get("skills") as HashMap<String, Any>)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching init data", exception)
        }
    }

    private fun goBackToOrganizerPage() {
        controller.navigate(R.id.action_createVacancyMainFragment_to_organizerPageFragment)
    }
}
