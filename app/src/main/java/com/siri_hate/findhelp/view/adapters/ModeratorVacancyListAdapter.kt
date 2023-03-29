package com.siri_hate.findhelp.view.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot
import com.siri_hate.findhelp.R
import com.siri_hate.findhelp.view.activities.VacancyCardActivity

class ModeratorVacancyListAdapter(context: Context, offers: List<DocumentSnapshot>) :
    ArrayAdapter<DocumentSnapshot>(context, 0, offers.toMutableList()) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertViewInner: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.vacancies_list_item, parent, false)

        val vacancyItemName = convertViewInner.findViewById<TextView>(R.id.vacancy_item_name)
        val vacancyItemDeleteButton =
            convertViewInner.findViewById<ImageButton>(R.id.vacancy_item_delete_button)

        val document: DocumentSnapshot? = getItem(position)
        document?.let { documentSnapshot ->
            vacancyItemName.text = documentSnapshot.getString("organization_name")
            val documentRef = documentSnapshot.reference

            // Слушатель нажатия на элемент списка
            vacancyItemName.setOnClickListener {
                documentSnapshot.id.let { documentId ->
                    val intent = Intent(context, VacancyCardActivity::class.java)
                    intent.putExtra("document_id", documentId)
                    context.startActivity(intent)
                }
            }

            vacancyItemDeleteButton.setOnClickListener {
                // Создание всплывающего окна
                val dialog = AlertDialog.Builder(context)
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

        return convertViewInner
    }
}









