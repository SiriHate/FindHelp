package com.siri_hate.findhelp.view.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class UserVacancyAdapter(
    private var vacancies: List<DocumentSnapshot>,
    private val userDoc: DocumentSnapshot,
    private val controller: NavController
) : RecyclerView.Adapter<UserVacancyAdapter.ViewHolder>() {

    companion object {
        private const val DOCUMENT_ID = "document_id"
        private const val VACANCY_NAME_FIELD = "vacancy_name"
        private const val VACANCY_SKILLS_LIST_FIELD = "vacancy_skills_list"
        private const val SKILLS_FIELD = "skills"
    }

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

        holder.vacancyNameTextView.text = vacancy.getString(VACANCY_NAME_FIELD)

        holder.userVacanciesListItemLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(DOCUMENT_ID, vacancy.id)
            controller.navigate(R.id.action_userPageFragment_to_vacancyCardFragment, bundle)
        }

        @Suppress("UNCHECKED_CAST")
        val vacancySkillsList = vacancy[VACANCY_SKILLS_LIST_FIELD] as? HashMap<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val userSkills = userDoc[SKILLS_FIELD] as? HashMap<String, Boolean>
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

        val color = when (matchPercent) {
            in 0..39 -> R.color.red_indicator
            in 40..69 -> R.color.yellow_indicator
            else -> R.color.green_indicator
        }
        holder.itemView.findViewById<TextView>(R.id.user_vacancies_list_item_match_percent_num)
            .let { textView ->
                textView.setTextColor(textView.context.getColor(color))
            }
    }

    fun updateList(newVacancies: List<DocumentSnapshot>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = vacancies.size

            override fun getNewListSize(): Int = newVacancies.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return vacancies[oldItemPosition].id == newVacancies[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return vacancies[oldItemPosition] == newVacancies[newItemPosition]
            }
        })

        vacancies = newVacancies
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vacancyNameTextView: TextView =
            itemView.findViewById(R.id.user_vacancies_list_item_vacancy_name)
        val matchPercentTextView: TextView =
            itemView.findViewById(R.id.user_vacancies_list_item_match_percent_num)
        val userVacanciesListItemLayout: LinearLayout =
            itemView.findViewById(R.id.user_vacancies_list_item_layout)
    }
}
