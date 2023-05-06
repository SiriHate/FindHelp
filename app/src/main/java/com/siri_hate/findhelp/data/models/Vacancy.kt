package com.siri_hate.findhelp.data.models

data class Vacancy(
    val creator_email: String,
    val contact_person: String,
    val organization_name: String,
    val organization_phone: String,
    val vacancy_name: String,
    val vacancy_city: String,
    val vacancy_description: String,
    val vacancy_skills_list: Map<String, Boolean>
) {
    constructor(
        vacancy_name: String,
        vacancy_city: String,
        vacancy_description: String,
        vacancy_skills_list: Map<String, Boolean>
    ) : this("", "", "", "", vacancy_name, vacancy_city, vacancy_description, vacancy_skills_list)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "vacancy_name" to vacancy_name,
            "vacancy_city" to vacancy_city,
            "vacancy_description" to vacancy_description,
            "vacancy_skills_list" to vacancy_skills_list
        )
    }

}