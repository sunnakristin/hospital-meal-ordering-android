package com.example.matarpontun.ui.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matarpontun.R

class PatientListAdapter(
    private val onOrderClicked: (Long) -> Unit,
    private val onToggleClicked: (Long) -> Unit,
) : RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {

    private var rows: List<PatientListViewModel.PatientRowUi> = emptyList()

    fun submitRows(newRows: List<PatientListViewModel.PatientRowUi>) {
        rows = newRows
        notifyDataSetChanged()
    }

    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvFoodType: TextView = view.findViewById(R.id.tvFoodType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)

        val btnOrder: Button = view.findViewById(R.id.btnOrder)
        val btnToggle: Button = view.findViewById(R.id.btnToggle)

        val detailsContainer: View = view.findViewById(R.id.detailsContainer)

        val tvBreakfast: TextView = view.findViewById(R.id.tvBreakfast)
        val tvLunch: TextView = view.findViewById(R.id.tvLunch)
        val tvAfternoonSnack: TextView = view.findViewById(R.id.tvAfternoonSnack)
        val tvDinner: TextView = view.findViewById(R.id.tvDinner)
        val tvNightSnack: TextView = view.findViewById(R.id.tvNightSnack)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {

        val row = rows[position]

        holder.tvName.text = row.name
        holder.tvFoodType.text = "Food Type: ${row.foodTypeName}"
        holder.tvStatus.text = row.statusText//"Status: ${row.statusText}"

        holder.btnOrder.text = row.primaryButtonText
        holder.btnOrder.isEnabled = row.primaryButtonEnabled

        holder.btnOrder.setOnClickListener {
            if (row.primaryButtonEnabled)
                onOrderClicked(row.patientId)
        }

        holder.btnToggle.isEnabled = row.hasOrder
        holder.btnToggle.text =
            if (row.expanded) "Hide Details ▲" else "Show Details ▼"

        holder.btnToggle.setOnClickListener {
            if (row.hasOrder)
            onToggleClicked(row.patientId)
        }

        holder.detailsContainer.visibility =
            if (row.expanded && row.hasOrder) View.VISIBLE else View.GONE

        if (row.hasOrder) {
            holder.tvBreakfast.text = buildString {
                append("Breakfast: ")
                append(row.breakfast ?: "-")
            }
            holder.tvLunch.text = buildString {
                append("Lunch: ")
                append(row.lunch ?: "-")
            }
            holder.tvAfternoonSnack.text = buildString {
                append("Afternoon snack: ")
                append(row.afternoonSnack ?: "-")
            }
            holder.tvDinner.text = buildString {
                append("Dinner: ")
                append(row.dinner ?: "-")
            }
            holder.tvNightSnack.text = buildString {
                append("Night snack: ")
                append(row.nightSnack ?: "-")
            }
        }
    }

    override fun getItemCount(): Int = rows.size
}