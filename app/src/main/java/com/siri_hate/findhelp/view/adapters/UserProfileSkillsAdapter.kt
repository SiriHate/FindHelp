package com.siri_hate.findhelp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.models.Skill

class UserProfileSkillsAdapter(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val userEmail: String,
    private var skillsList: List<Skill>
) : RecyclerView.Adapter<UserProfileSkillsAdapter.ViewHolder>() {

    companion object {
        private const val COLLECTION_NAME = "user_info"
        private const val SKILLS_FIELD = "skills"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val skillNameTextView: TextView = view.findViewById(R.id.skill_item_name)
        val skillCheckBox: CheckBox = view.findViewById(R.id.skill_item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.skills_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skill = skillsList[position]

        holder.skillCheckBox.setOnCheckedChangeListener(null)
        holder.skillCheckBox.isChecked = false

        holder.skillNameTextView.text = skill.name
        holder.skillCheckBox.isChecked = skill.isChecked

        holder.skillCheckBox.setOnCheckedChangeListener { _, isChecked ->
            db.collection(COLLECTION_NAME).document(userEmail)
                .update("$SKILLS_FIELD.${skill.name}", isChecked)
                .addOnSuccessListener {
                    skillsList[position].isChecked = isChecked
                }
        }
    }

    override fun getItemCount(): Int {
        return skillsList.size
    }
}









