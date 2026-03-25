package com.example.financetracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat

class TransactionAdapter(
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) {
            binding.apply {
                tvCategory.text = item.category
                tvNote.text = item.note.ifEmpty { "-" }
                tvDate.text = formatDate(item.date)
                
                val color = if (item.type == "income") {
                    ContextCompat.getColor(root.context, R.color.income)
                } else {
                    ContextCompat.getColor(root.context, R.color.expense)
                }
                
                val prefix = if (item.type == "income") "+ " else "- "
                tvAmount.text = "$prefix${formatRupiah(item.amount)}"
                tvAmount.setTextColor(color)

                root.setOnClickListener { onEdit(item) }
                btnDelete.setOnClickListener { onDelete(item) }
            }
        }
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp)
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}