package com.siri_hate.findhelp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.siri_hate.findhelp.R


class VacancySkillsListAdapter(private val context: Context) :
    RecyclerView.Adapter<VacancySkillsListAdapter.ViewHolder>() {

    private var skillsList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.vacancy_card_skills_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(skillsList[position])
    }

    override fun getItemCount(): Int {
        return skillsList.size
    }

    fun setSkillsList(newList: List<String>) {
        val diffResult = DiffUtil.calculateDiff(VacancySkillsListDiffCallback(skillsList, newList))
        skillsList.clear()
        skillsList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val skillTextView: TextView = itemView.findViewById(R.id.vacancy_skills_list_item_name)

        fun bind(skill: String) {
            skillTextView.text = skill
        }
    }

    private class VacancySkillsListDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ): DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
