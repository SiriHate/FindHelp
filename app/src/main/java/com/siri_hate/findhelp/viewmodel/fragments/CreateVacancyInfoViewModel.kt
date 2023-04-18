package com.siri_hate.findhelp.viewmodel.fragments

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class CreateVacancyInfoViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    companion object {
        const val ORGANIZATION_INFO_COLLECTION = "organization_info"
        const val INIT_DATA_COLLECTION = "init_data"
        const val BASE_SKILLS_INIT_DOC_ID = "base_skills_init"
        const val TAG = "CreateVacancyViewModel"
    }

    fun createNewVacancy(
        name: String,
        city: String,
        description: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        if (isInputValid(name, city, description)) {
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
                            "vacancy_name" to name,
                            "vacancy_city" to city,
                            "vacancy_description" to description,
                            "vacancy_skills_list" to skills
                        )
                        vacancyDocRef.set(newVacancy).addOnSuccessListener {
                            onSuccess(vacancyDocRef.id)
                        }.addOnFailureListener {
                            onFailure()
                        }
                    }
                } else {
                    onFailure()
                }
            }.addOnFailureListener {
                onFailure()
            }
        } else {
            onFailure()
        }
    }

    private fun isInputValid(name: String, city: String, description: String): Boolean {
        return name.isNotEmpty() && city.isNotEmpty() && description.isNotEmpty()
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
}