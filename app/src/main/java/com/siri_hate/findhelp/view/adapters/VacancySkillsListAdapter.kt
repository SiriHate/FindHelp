package com.siri_hate.findhelp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.siri_hate.findhelp.R


class VacancySkillsListAdapter(context: Context, skillsList: List<String>) :
    ArrayAdapter<String>(context, 0, skillsList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).
            inflate(R.layout.vacancy_skills_list_item, parent, false)
        }

        val skillNameTextView: TextView =
            itemView?.findViewById(R.id.vacancy_skills_list_item_name) ?: TextView(context)
        skillNameTextView.text = getItem(position)

        return itemView ?: View(context)
    }
}