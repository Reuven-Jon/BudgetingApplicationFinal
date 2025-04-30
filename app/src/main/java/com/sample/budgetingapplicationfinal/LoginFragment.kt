package com.sample.budgetingapplicationfinal

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

        binding!!.btnLogin.setOnClickListener {
            val email = binding!!.etUsername.text.toString().trim()
            val password = binding!!.etPassword.text.toString()

            var ok = true

            // 1) Validate email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding!!.tilUsername.error = "Enter a valid email"
                ok = false
            } else {
                binding!!.tilUsername.error = null
            }

            // 2) Validate password complexity
            val special = "!@#\$%^&*()_+[]{}|;:',.<>?/`~"
            if (password.length < 8 ||
                password.none { it.isUpperCase() } ||
                password.none { it.isDigit() } ||
                password.none { special.contains(it) }) {
                binding!!.tilPassword.error =
                    "Pwd ≥8 chars, include upper, digit & special"
                ok = false
            } else {
                binding!!.tilPassword.error = null
            }

            if (ok) {
                // on success → go to your goal‐input screen
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, GoalInputFragment())
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
