package com.sample.budgetingapplicationfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate your fragment_register.xml via its binding
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Email-info icon (optional)
        binding.tilRegEmail.setEndIconOnClickListener {
            showInfo(
                "Email format",
                "Enter an address like name@domain.com"
            )
        }

        // Password-info icon (optional)
        binding.tilRegPassword.setEndIconOnClickListener {
            showInfo(
                "Password rules",
                "At least 8 chars, include uppercase, number & symbol"
            )
        }

        // Submit registration
        binding.btnSubmitRegister.setOnClickListener {
            val email = binding.etRegEmail.text.toString().trim()
            val pwd   = binding.etRegPassword.text.toString()

            // Basic validation
            if (email.isEmpty()) {
                binding.tilRegEmail.error = "Email required"
                return@setOnClickListener
            } else {
                binding.tilRegEmail.error = null
            }

            if (pwd.length < 6) {
                binding.tilRegPassword.error = "Min 6 characters"
                return@setOnClickListener
            } else {
                binding.tilRegPassword.error = null
            }

            // Create user
            auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(
                            Intent(this, IncomeActivity::class.java)
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun showInfo(title: String, msg: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }
}
