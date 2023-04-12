package com.siri_hate.findhelp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R

class CreateAndEditVacancySkillsListApdater(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val documentId: String,
    skillsList: List<String>,
    private val onCheckboxSelected: () -> Unit
) : RecyclerView.Adapter<CreateAndEditVacancySkillsListApdater.ViewHolder>() {

    private var skillsList = mutableListOf<String>()

    init {
        this.skillsList.addAll(skillsList)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val skillNameTextView: TextView = itemView.findViewById(R.id.skill_item_name)
        private val skillCheckBox: CheckBox = itemView.findViewById(R.id.skill_item_checkbox)

        fun bind(skillName: String) {
            skillNameTextView.text = skillName

            db.collection("vacancies_list").document(documentId).get()
                .addOnSuccessListener { documentSnapshot ->
                    @Suppress("UNCHECKED_CAST")
                    val skillsMap =
                        documentSnapshot.get("vacancy_skills_list") as Map<String, Boolean>
                    skillCheckBox.isChecked = skillsMap[skillName] ?: false
                }

            skillCheckBox.setOnCheckedChangeListener { _, isChecked ->
                db.collection("vacancies_list").document(documentId)
                    .update("vacancy_skills_list.$skillName", isChecked)
                onCheckboxSelected()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.skills_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(skillsList[position])
    }

    override fun getItemCount(): Int {
        return skillsList.size
    }

    fun updateSkillsList(newList: List<String>) {
        val diffResult = DiffUtil.calculateDiff(SkillsListDiffCallback(skillsList, newList))
        skillsList.clear()
        skillsList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class SkillsListDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {

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
            return true
        }
    }

}