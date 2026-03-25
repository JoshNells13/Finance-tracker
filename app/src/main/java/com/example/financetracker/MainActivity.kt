package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to the correct MainActivity in the ui package
        val intent = Intent(this, com.example.financetracker.ui.MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}