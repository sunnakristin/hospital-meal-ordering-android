package com.example.matarpontun.ui.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matarpontun.R

class PatientListAdapter(
    private val onOrderClicked: (Long) -> Unit,
    private val onToggleClicked: (Long) -> Unit,
    private val onRoomToggled: (String) -> Unit,
    private val onEditClicked: (PatientListViewModel.PatientRowUi) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<PatientListViewModel.PatientListItem> = emptyList()

    fun submitItems(newItems: List<PatientListViewModel.PatientListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is PatientListViewModel.PatientListItem.RoomHeader -> VIEW_TYPE_ROOM
        is PatientListViewModel.PatientListItem.PatientRow -> VIEW_TYPE_PATIENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ROOM) {
            RoomHeaderViewHolder(inflater.inflate(R.layout.item_room_header, parent, false))
        } else {
            PatientViewHolder(inflater.inflate(R.layout.item_patient, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PatientListViewModel.PatientListItem.RoomHeader -> (holder as RoomHeaderViewHolder).bind(item)
            is PatientListViewModel.PatientListItem.PatientRow -> (holder as PatientViewHolder).bind(item.data)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class RoomHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvRoomName: TextView = view.findViewById(R.id.tvRoomName)
        private val tvQrCode: TextView = view.findViewById(R.id.tvQrCode)
        private val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        private val ivChevron: ImageView = view.findViewById(R.id.ivChevron)

        fun bind(item: PatientListViewModel.PatientListItem.RoomHeader) {
            tvRoomName.text = "Room: ${item.roomName}"
            if (item.qrCode != null) {
                tvQrCode.text = item.qrCode
                tvQrCode.visibility = View.VISIBLE
            } else {
                tvQrCode.visibility = View.GONE
            }
            if (item.allOrdered) {
                tvOrderStatus.text = "✓"
                tvOrderStatus.setTextColor(0xFF2E7D32.toInt())
            } else {
                tvOrderStatus.text = "✗"
                tvOrderStatus.setTextColor(0xFFC62828.toInt())
            }
            ivChevron.rotation = if (item.isExpanded) 180f else 0f
            itemView.setOnClickListener {
                ivChevron.animate().rotation(if (item.isExpanded) 0f else 180f).setDuration(200).start()
                onRoomToggled(item.roomName)
            }
        }
    }

    inner class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvFoodType: TextView = view.findViewById(R.id.tvFoodType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnOrder: Button = view.findViewById(R.id.btnOrder)
        val btnToggle: Button = view.findViewById(R.id.btnToggle)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val detailsContainer: View = view.findViewById(R.id.detailsContainer)
        val tvRestrictions: TextView = view.findViewById(R.id.tvRestrictions)
        val tvBreakfast: TextView = view.findViewById(R.id.tvBreakfast)
        val tvLunch: TextView = view.findViewById(R.id.tvLunch)
        val tvAfternoonSnack: TextView = view.findViewById(R.id.tvAfternoonSnack)
        val tvDinner: TextView = view.findViewById(R.id.tvDinner)
        val tvNightSnack: TextView = view.findViewById(R.id.tvNightSnack)

        fun bind(row: PatientListViewModel.PatientRowUi) {
            tvName.text = "Bed ${row.bedNumber}: ${row.name}"
            tvFoodType.text = "Food Type: ${row.foodTypeName}"
            tvStatus.text = row.statusText

            btnOrder.text = row.primaryButtonText
            btnOrder.isEnabled = row.primaryButtonEnabled
            btnOrder.setOnClickListener { if (row.primaryButtonEnabled) onOrderClicked(row.patientId) }

            btnToggle.isEnabled = row.hasOrder
            btnToggle.text = if (row.expanded) "Hide Details ▲" else "Show Details ▼"
            btnToggle.setOnClickListener { if (row.hasOrder) onToggleClicked(row.patientId) }

            btnEdit.setOnClickListener { onEditClicked(row) }

            detailsContainer.visibility = if (row.expanded && row.hasOrder) View.VISIBLE else View.GONE

            tvRestrictions.text = if (row.restrictions.isNotEmpty())
                "Restrictions: ${row.restrictions.joinToString(", ")}"
            else
                "Restrictions: None"

            if (row.hasOrder) {
                tvBreakfast.text = "Breakfast: ${row.breakfast ?: "-"}"
                tvLunch.text = "Lunch: ${row.lunch ?: "-"}"
                tvAfternoonSnack.text = "Afternoon snack: ${row.afternoonSnack ?: "-"}"
                tvDinner.text = "Dinner: ${row.dinner ?: "-"}"
                tvNightSnack.text = "Night snack: ${row.nightSnack ?: "-"}"
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ROOM = 0
        private const val VIEW_TYPE_PATIENT = 1
    }
}
