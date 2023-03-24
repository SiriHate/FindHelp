package com.siri_hate.findhelp.view.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.model.Skill

class UserSkillsAdapter(context: Context, skills: MutableList<Skill>) :
    ArrayAdapter<Skill>(context, 0, skills) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.user_skills_list,
            parent, false)

        // Получение элемента списка
        val skillItem = getItem(position)

        // Настройка отображения названия навыка
        val skillName = view.findViewById<TextView>(R.id.Vacancy_name)
        skillName.text = skillItem?.name

        // Настройка отображения состояния checkbox
        val skillCheckbox = view.findViewById<CheckBox>(R.id.skill_checkbox)
        skillCheckbox.isChecked = skillItem?.isSelected ?: false

        // Обработчик изменения состояния checkbox
        skillCheckbox.setOnCheckedChangeListener { _, isChecked ->
            skillItem?.isSelected = isChecked
            // Получение email пользователя
            val email = FirebaseAuth.getInstance().currentUser!!.email
            // Получение ссылки на документ пользователя
            val skillsRef = FirebaseFirestore.getInstance().
            collection("user_skills").document(email!!)
            // Обновление состояния навыка в Firestore
            skillItem?.let {
                skillsRef.update(it.name, it.isSelected)
                    .addOnSuccessListener {
                        Log.d(TAG, "Документ успешно обновлен!")
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.w(TAG, "Ошибка обновления документа", e)
                    }
            }
        }

        return view
    }

    // Обновление списка навыков
    fun updateSkills(skillsMap: Map<String, Boolean>) {
        clear()
        for ((name, isSelected) in skillsMap) {
            add(Skill(name, isSelected))
        }
    }
}