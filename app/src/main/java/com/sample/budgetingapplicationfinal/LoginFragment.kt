package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        auth     = Firebase.auth

        // Show email rules on icon tap
        binding.tilUsername.setEndIconOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.username_rules_title)
                .setMessage(R.string.username_rules)
                .setPositiveButton("OK", null)
                .show()
        }

        // Show password rules on icon tap
        binding.tilPassword.setEndIconOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.password_rules_title)
                .setMessage(R.string.password_rules)
                .setPositiveButton("OK", null)
                .show()
        }

        // Navigate to Register screen
        binding.btnRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        // Handle login taps
        binding.btnLogin.setOnClickListener { btn ->
            // Bounce animation
            val bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            btn.startAnimation(bounce)

            // Read inputs
            val email = binding.etUsername.text.toString().trim()
            val pw    = binding.etPassword.text.toString()
            var valid = true

            // Validate email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilUsername.error = getString(R.string.error_invalid_email)
                valid = false
            } else {
                binding.tilUsername.error = null
            }

            // Validate password
            val special = "!@#\$%^&*()_+[]{}|;:',.<>?/~"
            if (pw.length < 8 ||
                pw.none { it.isUpperCase() } ||
                pw.none { it.isDigit() } ||
                pw.none { special.contains(it) }
            ) {
                binding.tilPassword.error = getString(R.string.error_password_rules)
                valid = false
            } else {
                binding.tilPassword.error = null
            }

            if (!valid) return@setOnClickListener

            // Attempt sign-in
            auth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.login_failed, task.exception?.message),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnCompleteListener
                    }

                    // Success toast
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_successful),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Check profile in Realtime DB
                    val uid = auth.currentUser!!.uid
                    val ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("profile")

                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.container, GoalInputFragment())
                                    .commit()
                            } else {
                                auth.signOut()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_no_profile),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_db, error.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
