package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import com.sample.budgetingapplicationfinal.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var binding: FragmentLoginBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        // Info-button listeners
        binding?.tilUsername?.setEndIconOnClickListener {
            showInfoDialog(
                title = "Email format",
                message = "Enter a valid address like name@domain.com"
            )
        }
        binding?.tilPassword?.setEndIconOnClickListener {
            showInfoDialog(
                title = "Password rules",
                message = "At least 8 chars, include an uppercase letter, a number & a symbol"
            )
        }
        binding?.btnRegister?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
        // Login handler
        binding?.btnLogin?.setOnClickListener {
            val email = binding?.etUsername?.text.toString().trim()
            val password = binding?.etPassword?.text.toString()
            var ok = true

            // Validate email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding?.tilUsername?.error = "Enter a valid email"
                ok = false
            } else {
                binding?.tilUsername?.error = null
            }

            // Validate password complexity
            val specialChars = "!@#\$%^&*()_+[]{}|;:',.<>?/~"
            if (password.length < 8 ||
                password.none { it.isUpperCase() } ||
                password.none { it.isDigit() } ||
                password.none { specialChars.contains(it) }
            ) {
                binding?.tilPassword?.error =
                    "Password must be ≥8 chars, include uppercase, digit & symbol"
                ok = false
            } else {
                binding?.tilPassword?.error = null
            }

            // Navigate on success
            if (ok) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, GoalInputFragment())
                    .commit()
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