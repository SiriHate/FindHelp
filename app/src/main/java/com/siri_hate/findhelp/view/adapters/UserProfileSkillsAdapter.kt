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

class UserProfileSkillsAdapter(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val userEmail: String,
    private var skillsList: List<String>
) : RecyclerView.Adapter<UserProfileSkillsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val skillNameTextView: TextView = view.findViewById(R.id.skill_item_name)
        val skillCheckBox: CheckBox = view.findViewById(R.id.skill_item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.skills_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skillName = skillsList[position]
        holder.skillNameTextView.text = skillName

        db.collection("user_info").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                @Suppress("UNCHECKED_CAST")
                val skillsMap = documentSnapshot.get("skills") as Map<String, Boolean>
                holder.skillCheckBox.isChecked = skillsMap[skillName] ?: false
            }

        holder.skillCheckBox.setOnCheckedChangeListener { _, isChecked ->
            db.collection("user_info").document(userEmail)
                .update("skills.$skillName", isChecked)
        }
    }

    override fun getItemCount(): Int {
        return skillsList.size
    }

    fun updateSkillsList(newList: List<String>) {
        // Создаем объект DiffUtil
        val diffCallback = UserProfileSkillsDiffCallback(skillsList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        skillsList = newList.toMutableList()

        // Обновляем список с использованием DiffUtil
        diffResult.dispatchUpdatesTo(this)
    }

    class UserProfileSkillsDiffCallback(
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
            // Сравниваем элементы по индексу, так как они могут быть перемещены в списке
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Проверяем, является ли содержимое элементов одинаковым
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}








