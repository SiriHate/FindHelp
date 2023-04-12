package com.siri_hate.findhelp.view.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class UserVacancyAdapter(
    private val vacancies: MutableList<DocumentSnapshot>,
    private val userDoc: DocumentSnapshot,
    private val controller: NavController
) : RecyclerView.Adapter<UserVacancyAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return vacancies.size
    }

    private fun getItem(position: Int): DocumentSnapshot {
        return vacancies[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_vacancies_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vacancy = getItem(position)

        holder.vacancyNameTextView.text = vacancy.getString("vacancy_name")

        holder.vacancyNameTextView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("document_id", vacancy.id)
            controller.navigate(R.id.action_userPageFragment_to_vacancyCardFragment, bundle)
        }

        @Suppress("UNCHECKED_CAST")
        val vacancySkillsList = vacancy["vacancy_skills_list"] as? HashMap<String, Boolean>
        @Suppress("UNCHECKED_CAST")
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
        val text = holder.itemView.context.getString(R.string.match_count, matchPercent, matchCount, vacancyCount)
        holder.matchPercentTextView.text = text
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vacancyNameTextView: TextView = itemView.findViewById(R.id.user_vacancies_list_item_vacancy_name)
        val matchPercentTextView: TextView = itemView.findViewById(R.id.user_vacancies_list_item_match_percent)
    }
}
