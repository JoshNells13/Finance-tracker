package com.example.financetracker.data.model

data class Transaction(
    var id: String = "",
    val type: String = "", // "income" or "expense"
    val amount: Long = 0,
    val category: String = "",
    val note: String = "",
    val date: Long = 0
)