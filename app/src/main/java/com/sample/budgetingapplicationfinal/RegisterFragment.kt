package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var binding: FragmentRegisterBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        auth    = Firebase.auth

        binding?.tvBackToLogin?.setOnClickListener {
            parentFragmentManager.popBackStack() // return to login
        }


        // Info-icon tooltips
        binding?.tilRegEmail?.setEndIconOnClickListener {
            showInfoDialog("Email format", "Enter name@domain.com")
        }
        binding?.tilRegPassword?.setEndIconOnClickListener {
            showInfoDialog("Password rules", "At least 8 chars, uppercase, digit & symbol")
        }

        // Handle “Submit” registration
        binding?.btnSubmitRegister?.setOnClickListener {
            val email = binding!!.etRegEmail.text.toString().trim()
            val pwd   = binding!!.etRegPassword.text.toString()

            var valid = true
            if (email.isEmpty()) {
                binding!!.tilRegEmail.error = "Required"
                valid = false
            } else {
                binding!!.tilRegEmail.error = null
            }
            if (pwd.length < 6) {
                binding!!.tilRegPassword.error = "Min 6 characters"
                valid = false
            } else {
                binding!!.tilRegPassword.error = null
            }
            if (!valid) return@setOnClickListener

            // Create FirebaseAuth user
            auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Registered $email",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Go back to LoginFragment
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception?.message ?: "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
