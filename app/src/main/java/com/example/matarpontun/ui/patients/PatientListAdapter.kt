package com.example.matarpontun.ui.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matarpontun.R
import com.example.matarpontun.domain.model.Patient

class PatientListAdapter(
    private var patients: List<Patient>,
    private var orderedPatientIds: Set<Long>,
    private var orderingPatientIds: Set<Long>,
    private val onOrderClicked: (Patient) -> Unit
) : RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {
    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInfo: TextView = view.findViewById(R.id.tvPatientInfo)
        val btnOrder: Button = view.findViewById(R.id.btnOrder)
    }

    fun updateData(
        newPatients: List<Patient>,
        newOrderedIds: Set<Long>,
        newOrderingIds: Set<Long>
    ) {
        patients = newPatients
        orderedPatientIds = newOrderedIds
        orderingPatientIds = newOrderingIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]

        val isOrdered = patient.patientId in orderedPatientIds
        val isOrdering = patient.patientId in orderingPatientIds

        holder.tvInfo.text = "${patient.name} — Room ${patient.room}"

        holder.btnOrder.text = when {
            isOrdered -> "ORDERED"
            isOrdering -> "ORDERING..."
            else -> "ORDER"
        }

        holder.btnOrder.isEnabled = !isOrdered && !isOrdering

        holder.btnOrder.setOnClickListener {
            if (!isOrdered && !isOrdering) {
                onOrderClicked(patient)
            }
        }
    }

    override fun getItemCount(): Int = patients.size
}