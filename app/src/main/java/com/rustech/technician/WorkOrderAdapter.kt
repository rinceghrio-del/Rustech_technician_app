package com.rustech.technician

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class WorkOrderAdapter(
    private val onComplete: (WorkOrder) -> Unit,
    private val onDelay: (WorkOrder) -> Unit,
    private val onCancel: (WorkOrder) -> Unit
) : RecyclerView.Adapter<WorkOrderAdapter.WorkOrderViewHolder>() {

    private val items = mutableListOf<WorkOrder>()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

    fun submitList(newItems: List<WorkOrder>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class WorkOrderViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot: android.view.View = itemView.findViewById(R.id.cardRoot)
        val typeTag: android.widget.TextView = itemView.findViewById(R.id.typeTag)
        val customerName: android.widget.TextView = itemView.findViewById(R.id.customerName)
        val statusBadge: android.widget.TextView = itemView.findViewById(R.id.statusBadge)
        val scheduledText: android.widget.TextView = itemView.findViewById(R.id.scheduledText)
        val detailText: android.widget.TextView = itemView.findViewById(R.id.detailText)
        val contactText: android.widget.TextView = itemView.findViewById(R.id.contactText)
        val btnComplete: android.widget.Button = itemView.findViewById(R.id.btnComplete)
        val btnDelay: android.widget.Button = itemView.findViewById(R.id.btnDelay)
        val btnCancel: android.widget.Button = itemView.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_order, parent, false)
        return WorkOrderViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WorkOrderViewHolder, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context

        holder.typeTag.text = if (item.type == "install") "INSTALL" else "REPAIR"
        holder.customerName.text = item.displayName().ifBlank { "(walang pangalan)" }
        holder.detailText.text = item.displayDetail().ifBlank { "—" }
        holder.contactText.text = "Contact: " + item.displayContact().ifBlank { "—" }

        val isDelayed = item.status == "delayed"
        val badgeText = if (isDelayed) "Naantala" else if (item.status == "scheduled") "Naka-schedule" else item.status
        holder.statusBadge.text = badgeText

        val badgeColor = when {
            isDelayed -> R.color.danger
            item.status == "scheduled" -> R.color.scheduled_blue
            else -> R.color.text_dim
        }
        holder.statusBadge.setTextColor(ctx.getColor(badgeColor))

        holder.cardRoot.setBackgroundResource(if (isDelayed) R.drawable.bg_card_delayed else R.drawable.bg_card_scheduled)

        if (item.scheduledDate != null) {
            holder.scheduledText.text = "Iskedyul: " + dateFormat.format(item.scheduledDate.toDate())
            holder.scheduledText.visibility = android.view.View.VISIBLE
        } else {
            holder.scheduledText.visibility = android.view.View.GONE
        }

        // "Tapos na" label depends on the type — matches the admin dashboard's wording.
        holder.btnComplete.text = if (item.type == "install") "Na-install na" else "Naayos na"

        holder.btnComplete.setOnClickListener { onComplete(item) }
        holder.btnDelay.setOnClickListener { onDelay(item) }
        holder.btnCancel.setOnClickListener { onCancel(item) }
    }
}
