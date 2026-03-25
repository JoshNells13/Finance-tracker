package com.example.financetracker.data.repository

import com.example.financetracker.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val transactionCollection = firestore.collection("transactions")

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { subscription.remove() }
    }

    fun addTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        val id = transactionCollection.document().id
        transaction.id = id
        transactionCollection.document(id).set(transaction)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun updateTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        transactionCollection.document(transaction.id).set(transaction)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteTransaction(id: String, onComplete: (Boolean) -> Unit) {
        transactionCollection.document(id).delete()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}