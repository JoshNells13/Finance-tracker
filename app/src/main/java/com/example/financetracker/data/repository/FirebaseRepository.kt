package com.example.financetracker.data.repository

import com.example.financetracker.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val transactionCollection = firestore.collection("transactions")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        if (currentUserId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        // Kita menghapus .orderBy(...) di query Firestore untuk menghindari error "Index Required".
        // Sebagai gantinya, kita melakukan pengurutan secara manual di memori (client-side).
        val subscription = transactionCollection
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                
                // Urutkan data berdasarkan tanggal terbaru (descending) secara manual
                val sortedTransactions = transactions.sortedByDescending { it.date }
                
                trySend(sortedTransactions)
            }
        awaitClose { subscription.remove() }
    }

    fun addTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        val id = transactionCollection.document().id
        val data = transaction.copy(id = id, userId = currentUserId)
        transactionCollection.document(id).set(data)
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

    fun logout() {
        auth.signOut()
    }
}