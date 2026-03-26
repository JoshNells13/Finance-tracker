package com.example.financetracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.data.model.User
import com.example.financetracker.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading (opsional: jika ada progress bar)
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Memproses..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val user = User(uid, name, email)
                    
                    // Simpan ke Firestore
                    firestore.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener { e ->
                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.text = "Daftar Sekarang"
                            Toast.makeText(this, "Gagal simpan data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar Sekarang"
                    // Menampilkan pesan error spesifik dari Firebase
                    val errorMessage = task.exception?.localizedMessage ?: "Terjadi kesalahan koneksi"
                    Toast.makeText(this, "Daftar Gagal: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}