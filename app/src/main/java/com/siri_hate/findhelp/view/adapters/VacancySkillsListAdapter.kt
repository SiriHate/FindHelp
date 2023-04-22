package com.siri_hate.findhelp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siri_hate.findhelp.R


class VacancySkillsListAdapter : ListAdapter<String, VacancySkillsListAdapter.ViewHolder>(SkillsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.vacancy_card_skills_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skillName = getItem(position)
        holder.skillNameTextView.text = skillName
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skillNameTextView: TextView = itemView.findViewById(R.id.vacancy_skills_list_item_name)
    }

    class SkillsDiffCallback : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}


