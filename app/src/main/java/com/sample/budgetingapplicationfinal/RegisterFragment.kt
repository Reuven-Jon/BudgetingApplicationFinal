package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentRegisterBinding
import com.sample.budgetingapplicationfinal.FirebaseDatabaseManager
import com.sample.budgetingapplicationfinal.UserProfile

class RegisterFragment : Fragment(R.layout.fragment_register) {
    // Holds the binding to our fragment_register.xml views
    private var binding: FragmentRegisterBinding? = null

    // FirebaseAuth instance for creating users
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Link binding to the inflated layout
        binding = FragmentRegisterBinding.bind(view)
        // Initialize FirebaseAuth
        auth = Firebase.auth

        // If user taps “Back to login”, pop this fragment off the stack
        binding?.tvBackToLogin?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Show email rules when info icon is tapped
        binding?.tilRegEmail?.setEndIconOnClickListener {
            showInfoDialog("Email format", "Enter an address like name@domain.com")
        }
        // Show password rules when info icon is tapped
        binding?.tilRegPassword?.setEndIconOnClickListener {
            showInfoDialog("Password rules", "At least 8 chars, include uppercase, number & symbol")
        }

        // Handle the “Submit” button tap
        binding?.btnSubmitRegister?.setOnClickListener {
            val email = binding!!.etRegEmail.text.toString().trim() // get email
            val pwd = binding!!.etRegPassword.text.toString()        // get password

            // Simple validation checks
            var valid = true
            if (email.isEmpty()) {
                binding!!.tilRegEmail.error = "Required"  // show error
                valid = false
            } else {
                binding!!.tilRegEmail.error = null       // clear error
            }
            if (pwd.length < 6) {
                binding!!.tilRegPassword.error = "Min 6 characters"
                valid = false
            } else {
                binding!!.tilRegPassword.error = null
            }
            if (!valid) return@setOnClickListener           // abort if invalid

            // Create the user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Retrieve newly created user’s UID
                        val uid = auth.currentUser!!.uid
                        // Build a UserProfile object
                        val profile = UserProfile(
                            uid = uid,
                            email = email,
                            displayName = auth.currentUser?.displayName ?: "",
                            createdAt = System.currentTimeMillis()
                        )
                        // Save the profile under /users/{uid}/profile in Realtime DB
                        FirebaseDatabaseManager.saveUserProfile(profile)

                        // Let the user know registration succeeded
                        Toast.makeText(
                            requireContext(),
                            "Registered $email",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Navigate to IncomeActivity
                        val intent = Intent(requireContext(), IncomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish() // close the current activity
                    } else {
                        // Show the error message from Firebase
                        Toast.makeText(
                            requireContext(),
                            task.exception?.message ?: "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Utility to show an AlertDialog with a title and message
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
