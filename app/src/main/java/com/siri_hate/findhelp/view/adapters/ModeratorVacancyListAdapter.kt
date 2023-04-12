package com.siri_hate.findhelp.view.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class ModeratorVacancyListAdapter(
    private val controller: NavController
) : ListAdapter<DocumentSnapshot, ModeratorVacancyListAdapter.ViewHolder>(
    ModeratorVacancyDiffCallback()
) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vacancyItemName: TextView =
            view.findViewById(R.id.user_vacancies_list_item_vacancy_name)
        val vacancyItemDeleteButton: ImageButton =
            view.findViewById(R.id.vacancy_item_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.moderator_and_organizer_vacancies_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document: DocumentSnapshot = getItem(position)
        holder.vacancyItemName.text = document.getString("vacancy_name")
        val documentRef = document.reference

        // Слушатель нажатия на элемент списка
        holder.vacancyItemName.setOnClickListener {
            document.id.let { documentId ->
                val bundle = Bundle()
                bundle.putString("document_id", documentId)
                controller.navigate(
                    R.id.action_moderatorPageFragment_to_vacancyCardFragment,
                    bundle
                )
            }
        }

        holder.vacancyItemDeleteButton.setOnClickListener {
            // Создание всплывающего окна
            val dialog = AlertDialog.Builder(holder.itemView.context)
                .setTitle("Удаление вакансии")
                .setMessage("Вы точно хотите удалить эту вакансию?")
                .setPositiveButton("Да") { _, _ ->
                    // Удаление элемента
                    documentRef.delete()
                }
                .setNegativeButton("Нет", null)
                .show()

            // изменение цвета кнопок
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
    }

    private class ModeratorVacancyDiffCallback : DiffUtil.ItemCallback<DocumentSnapshot>() {
        override fun areItemsTheSame(
            oldItem: DocumentSnapshot,
            newItem: DocumentSnapshot
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DocumentSnapshot,
            newItem: DocumentSnapshot
        ): Boolean {
            return oldItem == newItem
        }
    }
}











