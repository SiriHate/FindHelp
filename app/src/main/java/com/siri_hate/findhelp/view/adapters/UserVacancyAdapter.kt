package com.siri_hate.findhelp.view.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class UserVacancyAdapter(
    private var vacancies: MutableList<DocumentSnapshot>,
    private val userDoc: DocumentSnapshot,
    private val controller: NavController
) : BaseAdapter() {

    private var filteredVacancies = vacancies

    init {
        // Отсортировать список в конструкторе
        vacancies = vacancies.sortedByDescending { vacancy ->
            val vacancySkillsList = vacancy["vacancy_skills_list"] as? HashMap<String, Boolean>
            val userSkills = userDoc["skills"] as? HashMap<String, Boolean>
            var matchCount = 0
            var vacancyCount = 0

            vacancySkillsList?.forEach { (skill, value) ->
                if (value && userSkills?.get(skill) == true) {
                    matchCount++
                }
                if (value) {
                    vacancyCount++
                }
            }

            if (vacancyCount == 0) 0 else (matchCount * 100 / vacancyCount)
        }.toMutableList()
    }


    override fun getCount(): Int {
        return filteredVacancies.size
    }

    override fun getItem(position: Int): Any {
        return filteredVacancies[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.user_vacancies_list_item, parent, false)

        val vacancyNameTextView = view.findViewById<TextView>(R.id.user_vacancies_list_item_vacancy_name)
        val vacancy = filteredVacancies[position]
        vacancyNameTextView.text = vacancy.getString("vacancy_name")

        vacancyNameTextView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("document_id", vacancy.id)
            controller.navigate(R.id.action_userPageFragment_to_vacancyCardFragment, bundle)
        }

        val vacancySkillsList = vacancy["vacancy_skills_list"] as? HashMap<String, Boolean>
        val userSkills = userDoc["skills"] as? HashMap<String, Boolean>
        var matchCount = 0
        var vacancyCount = 0

        vacancySkillsList?.forEach { (skill, value) ->
            if (value && userSkills?.get(skill) == true) {
                matchCount++
            }
            if (value) {
                vacancyCount++
            }
        }

        // Вычисляем процентное соотношение совпавших навыков
        val matchPercent = if (vacancyCount == 0) 0 else (matchCount * 100 / vacancyCount)
        val matchPercentTextView = view.findViewById<TextView>(R.id.user_vacancies_list_item_match_percent)
        val text = parent?.context?.getString(R.string.match_count, matchPercent, matchCount, vacancyCount)
        matchPercentTextView.text = text

        return view
    }
}