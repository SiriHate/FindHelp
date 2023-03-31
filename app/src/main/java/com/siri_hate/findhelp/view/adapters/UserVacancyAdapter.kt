package com.siri_hate.findhelp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class UserVacancyAdapter(
    private val vacancies: List<DocumentSnapshot>,
    private val userDoc: DocumentSnapshot
) : BaseAdapter() {

    override fun getCount(): Int {
        return vacancies.size
    }

    override fun getItem(position: Int): Any {
        return vacancies[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.user_vacancies_list_item, parent, false)

        val vacancyNameTextView = view.findViewById<TextView>(R.id.user_vacancies_list_item_vacancy_name)
        val vacancy = vacancies[position]
        vacancyNameTextView.text = vacancy.getString("vacancy_name")

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

        val matchRatioTextView = view.findViewById<TextView>(R.id.user_vacancies_list_item_match_ratio)
        matchRatioTextView.text = "$matchCount/$vacancyCount"

        // Вычисляем процентное соотношение совпавших навыков
        val matchPercent = if (vacancyCount == 0) 0 else (matchCount * 100 / vacancyCount)
        val matchPercentTextView = view.findViewById<TextView>(R.id.user_vacancies_list_item_match_percent)
        matchPercentTextView.text = "$matchPercent%"

        return view
    }
}