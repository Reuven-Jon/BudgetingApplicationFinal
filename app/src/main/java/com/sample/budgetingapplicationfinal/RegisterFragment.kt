package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sample.budgetingapplicationfinal.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var binding: FragmentRegisterBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        // Info-button listeners
        binding?.tilRegEmail?.setEndIconOnClickListener {
            showInfoDialog(
                title = "Email format",
                message = "Enter a valid address like name@domain.com"
            )
        }
        binding?.tilRegPassword?.setEndIconOnClickListener {
            showInfoDialog(
                title = "Password rules",
                message = "At least 8 chars, include uppercase, number & symbol"
            )
        }

        // Submit registration with empty-field checks
        binding?.btnSubmitRegister?.setOnClickListener {
            val email = binding?.etRegEmail?.text.toString().trim()
            val password = binding?.etRegPassword?.text.toString()
            var ok = true

            // Empty-field validation
            if (email.isEmpty()) {
                binding?.tilRegEmail?.error = "Required"
                ok = false
            } else {
                binding?.tilRegEmail?.error = null
            }

            if (password.isEmpty()) {
                binding?.tilRegPassword?.error = "Required"
                ok = false
            } else {
                binding?.tilRegPassword?.error = null
            }

            if (!ok) return@setOnClickListener

            // TODO: further validation & save new user
            Toast.makeText(requireContext(), "Registered $email", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
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