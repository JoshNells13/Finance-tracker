package com.example.financetracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.databinding.ActivityMainBinding
import com.example.financetracker.databinding.DialogAddTransactionBinding
import com.example.financetracker.ui.adapter.TransactionAdapter
import com.example.financetracker.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onEdit = { showAddEditDialog(it) },
            onDelete = { showDeleteConfirmation(it) }
        )
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactions.collect { transactions ->
                        adapter.submitList(transactions)
                        binding.emptyState.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.totalIncome.collect { 
                        binding.tvTotalIncome.text = formatRupiah(it)
                    }
                }
                launch {
                    viewModel.totalExpense.collect { 
                        binding.tvTotalExpense.text = formatRupiah(it)
                    }
                }
                launch {
                    viewModel.balance.collect { 
                        binding.tvBalance.text = formatRupiah(it)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Keluar") { _, _ ->
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddEditDialog(transaction: Transaction?) {
        val dialogBinding = DialogAddTransactionBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        if (transaction != null) {
            dialogBinding.tvDialogTitle.text = "Edit Transaksi"
            dialogBinding.etAmount.setText(transaction.amount.toString())
            dialogBinding.etCategory.setText(transaction.category)
            dialogBinding.etNote.setText(transaction.note)
            if (transaction.type == "income") {
                dialogBinding.rbIncome.isChecked = true
            } else {
                dialogBinding.rbExpense.isChecked = true
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val amountStr = dialogBinding.etAmount.text.toString()
            val category = dialogBinding.etCategory.text.toString()
            val note = dialogBinding.etNote.text.toString()
            val type = if (dialogBinding.rbIncome.isChecked) "income" else "expense"

            if (amountStr.isEmpty() || amountStr.toLong() <= 0) {
                Toast.makeText(this, "Jumlah harus diisi dan lebih dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                Toast.makeText(this, "Kategori harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTransaction = Transaction(
                id = transaction?.id ?: "",
                type = type,
                amount = amountStr.toLong(),
                category = category,
                note = note,
                date = transaction?.date ?: System.currentTimeMillis()
            )

            if (transaction == null) {
                viewModel.addTransaction(newTransaction)
            } else {
                viewModel.updateTransaction(newTransaction)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteTransaction(transaction.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }
}