package com.siri_hate.findhelp.view.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R

class OrganizerVacancyListAdapter(
    private val context: Context,
    private var offers: List<DocumentSnapshot>,
    private val controller: NavController
) : RecyclerView.Adapter<OrganizerVacancyListAdapter.OrganizerViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val userEmail = auth.currentUser?.email

    private val changedPositions = mutableListOf<Int>()

    class OrganizerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vacancyItemName: TextView = view.findViewById(R.id.user_vacancies_list_item_vacancy_name)
        val vacancyItemDeleteButton: ImageButton = view.findViewById(R.id.vacancy_item_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.moderator_and_organizer_vacancies_list_item, parent, false)
        return OrganizerViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizerViewHolder, position: Int) {
        val document = offers[position]
        val creatorEmail = document.getString("creator_email")

        if (creatorEmail == userEmail) {
            holder.vacancyItemName.text = document.getString("vacancy_name")
            val documentRef = document.reference

            // Слушатель нажатия на элемент списка
            holder.vacancyItemName.setOnClickListener {
                document.id.let { documentId ->
                    val bundle = Bundle()
                    bundle.putString("document_id", documentId)
                    controller.navigate(R.id.action_organizerPageFragment_to_vacancyCardFragment, bundle)
                }
            }

            holder.vacancyItemDeleteButton.setOnClickListener {
                // Создание всплывающего окна
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Удаление вакансии")
                    .setMessage("Вы точно хотите удалить эту вакансию?")
                    .setPositiveButton("Да") { _, _ ->
                        // Удаление элемента
                        documentRef.delete()
                        updateChangedPositions(position)
                    }
                    .setNegativeButton("Нет", null)
                    .show()

                // Изменение цвета кнопок
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            }

            if (changedPositions.contains(position)) {
                changedPositions.remove(position)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    fun updateVacancies(newOffers: List<DocumentSnapshot>) {
        val diffResult = DiffUtil.calculateDiff(VacancyDiffCallback(offers, newOffers))
        offers = newOffers
        diffResult.dispatchUpdatesTo(this)
    }

    private fun updateChangedPositions(position: Int) {
        if (!changedPositions.contains(position)) {
            changedPositions.add(position)
        }
    }

    private class VacancyDiffCallback(
        private val oldList: List<DocumentSnapshot>,
        private val newList: List<DocumentSnapshot>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }
}
