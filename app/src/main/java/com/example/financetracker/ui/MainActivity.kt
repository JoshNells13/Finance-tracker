package com.example.financetracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.R
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.databinding.ActivityMainBinding
import com.example.financetracker.databinding.DialogAddTransactionBinding
import com.example.financetracker.databinding.FragmentDashboardBinding
import com.example.financetracker.databinding.FragmentInsightsBinding
import com.example.financetracker.databinding.FragmentProfileBinding
import com.example.financetracker.ui.adapter.TransactionAdapter
import com.example.financetracker.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
        
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_dashboard -> {
                    loadFragment(DashboardFragment())
                    binding.fabAdd.show()
                    true
                }
                R.id.menu_insights -> {
                    loadFragment(InsightsFragment())
                    binding.fabAdd.hide()
                    true
                }
                R.id.menu_profile -> {
                    loadFragment(ProfileFragment())
                    binding.fabAdd.hide()
                    true
                }
                else -> false
            }
        }
        binding.fabAdd.setOnClickListener { showAddEditDialog(null) }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun showAddEditDialog(transaction: Transaction?) {
        val dialogBinding = DialogAddTransactionBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        if (transaction != null) {
            dialogBinding.tvDialogTitle.text = "Edit Transaksi"
            dialogBinding.etAmount.setText(transaction.amount.toString())
            dialogBinding.etCategory.setText(transaction.category)
            dialogBinding.etNote.setText(transaction.note)
            if (transaction.type == "income") dialogBinding.rbIncome.isChecked = true 
            else dialogBinding.rbExpense.isChecked = true
        }

        dialogBinding.btnSave.setOnClickListener {
            val amountStr = dialogBinding.etAmount.text.toString()
            val category = dialogBinding.etCategory.text.toString()
            if (amountStr.isEmpty() || category.isEmpty()) return@setOnClickListener

            val updatedTransaction = Transaction(
                id = transaction?.id ?: "",
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                type = if (dialogBinding.rbIncome.isChecked) "income" else "expense",
                amount = amountStr.toLong(),
                category = category,
                note = dialogBinding.etNote.text.toString(),
                date = transaction?.date ?: System.currentTimeMillis()
            )

            if (transaction == null) viewModel.addTransaction(updatedTransaction)
            else viewModel.updateTransaction(updatedTransaction)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(this).setTitle("Hapus").setMessage("Hapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteTransaction(transaction.id) }
            .setNegativeButton("Batal", null).show()
    }

    fun showLogoutConfirmation() {
        AlertDialog.Builder(this).setTitle("Keluar").setMessage("Yakin ingin keluar?")
            .setPositiveButton("Keluar") { _, _ ->
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }.setNegativeButton("Batal", null).show()
    }

    // --- Fragments ---

    class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
        private var _binding: FragmentDashboardBinding? = null
        private val binding get() = _binding!!
        private lateinit var viewModel: MainViewModel
        private lateinit var adapter: TransactionAdapter

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            _binding = FragmentDashboardBinding.bind(view)
            viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
            setupRecyclerView()
            setupObservers()
        }

        private fun setupRecyclerView() {
            adapter = TransactionAdapter(
                onEdit = { (activity as? MainActivity)?.showAddEditDialog(it) },
                onDelete = { (activity as? MainActivity)?.showDeleteConfirmation(it) }
            )
            binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
            binding.rvTransactions.adapter = adapter
        }

        private fun setupObservers() {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch { viewModel.transactions.collect { 
                        adapter.submitList(it)
                        binding.emptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    } }
                    launch { viewModel.totalIncome.collect { binding.tvTotalIncome.text = formatRupiah(it) } }
                    launch { viewModel.totalExpense.collect { binding.tvTotalExpense.text = formatRupiah(it) } }
                    launch { viewModel.balance.collect { binding.tvBalance.text = formatRupiah(it) } }
                    launch { 
                        viewModel.todayExpense.collect { today ->
                            if (today > 500000L) {
                                binding.tvWeeklyStatus.text = "Hari ini: ${formatRupiah(today)} (Boros! )"
                                binding.tvWeeklyStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense))
                            } else {
                                binding.tvWeeklyStatus.text = "Hari ini: ${formatRupiah(today)} (Hemat )"
                                binding.tvWeeklyStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                            }
                            // Progress bar hari ini (maksimal 1jt untuk indikator)
                            binding.pbWeekly.progress = ((today * 100) / 500000).coerceAtMost(100L).toInt()
                        }
                    }
                }
            }
        }
        private fun formatRupiah(amount: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
        override fun onDestroyView() { super.onDestroyView() ; _binding = null }
    }

    class InsightsFragment : Fragment(R.layout.fragment_insights) {
        private var _binding: FragmentInsightsBinding? = null
        private val binding get() = _binding!!
        private lateinit var viewModel: MainViewModel

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            _binding = FragmentInsightsBinding.bind(view)
            viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
            
            observeInsights()
        }

        private fun observeInsights() {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewModel.categoryData.collect { data ->
                            updateCategoryList(data)
                        }
                    }

                }
            }
        }

        private fun updateCategoryList(data: Map<String, Long>) {
            binding.categoryListContainer.removeAllViews()
            if (data.isEmpty()) {
                binding.tvEmptyInsights.visibility = View.VISIBLE
                return
            }
            binding.tvEmptyInsights.visibility = View.GONE
            val totalExpense = data.values.sum()
            
            data.forEach { (category, amount) ->
                val percentage = if (totalExpense > 0) (amount * 100 / totalExpense).toInt() else 0
                val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_insight_bar, binding.categoryListContainer, false)
                itemView.findViewById<TextView>(R.id.tvCategoryName).text = "$category ($percentage%)"
                itemView.findViewById<ProgressBar>(R.id.pbCategory).progress = percentage
                binding.categoryListContainer.addView(itemView)
            }
        }


        override fun onDestroyView() { super.onDestroyView() ; _binding = null }
    }

    class ProfileFragment : Fragment(R.layout.fragment_profile) {
        private var _binding: FragmentProfileBinding? = null
        private val binding get() = _binding!!
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            _binding = FragmentProfileBinding.bind(view)
            val user = FirebaseAuth.getInstance().currentUser
            binding.tvUserEmail.text = user?.email ?: "Belum Login"
            FirebaseFirestore.getInstance().collection("users").document(user?.uid ?: "").get()
                .addOnSuccessListener { binding.tvUserName.text = it.getString("name") ?: "Pengguna" }
            binding.btnLogoutProfile.setOnClickListener { (activity as? MainActivity)?.showLogoutConfirmation() }
        }
        override fun onDestroyView() { super.onDestroyView() ; _binding = null }
    }
}