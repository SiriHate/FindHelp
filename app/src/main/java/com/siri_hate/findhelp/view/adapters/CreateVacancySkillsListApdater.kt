package com.siri_hate.findhelp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class CreateVacancySkillsListApdater(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val vacancyID: String,
    private var skillsList: List<String>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.skills_list_item, parent, false
        )

        val skillNameTextView: TextView = view.findViewById(R.id.skill_item_name)
        val skillCheckBox: CheckBox = view.findViewById(R.id.skill_item_checkbox)

        val skillName = skillsList[position]
        skillNameTextView.text = skillName

        db.collection("vacancies_list").document(vacancyID).get()
            .addOnSuccessListener { documentSnapshot ->
                val skillsMap = documentSnapshot.get("vacancy_skills_list") as Map<String, Boolean>
                skillCheckBox.isChecked = skillsMap[skillName] ?: false
            }

        skillCheckBox.setOnCheckedChangeListener { _, isChecked ->
            db.collection("vacancies_list").document(vacancyID)
                .update("vacancy_skills_list.$skillName", isChecked)
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return skillsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return skillsList.size
    }

    fun updateSkillsList(newList: List<String>) {
        skillsList.toMutableList().clear()
        skillsList = newList.toMutableList()
        notifyDataSetChanged()
    }
}