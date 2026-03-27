package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _totalIncome = MutableStateFlow(0L)
    val totalIncome: StateFlow<Long> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0L)
    val totalExpense: StateFlow<Long> = _totalExpense.asStateFlow()

    private val _balance = MutableStateFlow(0L)
    val balance: StateFlow<Long> = _balance.asStateFlow()

    // --- State untuk Analisis (Insights) ---
    private val _categoryData = MutableStateFlow<Map<String, Long>>(emptyMap())
    val categoryData: StateFlow<Map<String, Long>> = _categoryData.asStateFlow()

    private val _weeklyGrowth = MutableStateFlow(0)
    val weeklyGrowth: StateFlow<Int> = _weeklyGrowth.asStateFlow()

    // --- State untuk Analisis Harian ---
    private val _todayExpense = MutableStateFlow(0L)
    val todayExpense: StateFlow<Long> = _todayExpense.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getTransactions().collect { list ->
                _transactions.value = list
                calculateSummary(list)
                calculateInsights(list)
                calculateDaily(list)
            }
        }
    }

    private fun calculateSummary(list: List<Transaction>) {
        var income = 0L
        var expense = 0L
        for (item in list) {
            if (item.type == "income") {
                income += item.amount
            } else {
                expense += item.amount
            }
        }
        _totalIncome.value = income
        _totalExpense.value = expense
        _balance.value = income - expense
    }

    private fun calculateDaily(list: List<Transaction>) {
        val now = Calendar.getInstance()
        val startOfDay = now.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayTotal = list.filter { it.type == "expense" && it.date >= startOfDay }.sumOf { it.amount }
        _todayExpense.value = todayTotal
    }

    private fun calculateInsights(list: List<Transaction>) {
        val expenses = list.filter { it.type == "expense" }
        val categoryMap = mutableMapOf<String, Long>()
        expenses.forEach {
            categoryMap[it.category] = (categoryMap[it.category] ?: 0L) + it.amount
        }
        _categoryData.value = categoryMap

        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        
        val thisWeekExpense = expenses.filter { it.date > (now - oneWeekMs) }.sumOf { it.amount }
        val lastWeekExpense = expenses.filter { it.date in (now - 2 * oneWeekMs)..(now - oneWeekMs) }.sumOf { it.amount }

        if (lastWeekExpense > 0) {
            val growth = ((thisWeekExpense - lastWeekExpense) * 100 / lastWeekExpense).toInt()
            _weeklyGrowth.value = growth
        } else {
            _weeklyGrowth.value = 0
        }
    }

    fun addTransaction(transaction: Transaction) {
        repository.addTransaction(transaction) { }
    }

    fun updateTransaction(transaction: Transaction) {
        repository.updateTransaction(transaction) { }
    }

    fun deleteTransaction(id: String) {
        repository.deleteTransaction(id) { }
    }
    
    fun logout() {
        repository.logout()
    }
}