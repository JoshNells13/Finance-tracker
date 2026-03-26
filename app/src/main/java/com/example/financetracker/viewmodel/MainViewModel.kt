package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getTransactions().collect { list ->
                _transactions.value = list
                calculateSummary(list)
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