package com.example.mypet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.mypet.databinding.ActivityForgotBinding // Import generated binding class

class Forgot : AppCompatActivity() {

    private lateinit var binding: ActivityForgotBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater) // Inflate layout using binding
        setContentView(binding.root) // Set root view as content

        auth = FirebaseAuth.getInstance()

        binding.signinBtn2.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding.editTextText5.text.toString().trim()

        if (email.isEmpty()) {
            binding.editTextText5.error = "Please enter your email address."
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextText5.error = "Please enter a valid email address."
            return
        }

        binding.progressBar.visibility = View.VISIBLE // Show progress indicator

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE // Hide progress indicator

                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset password link sent to your email", Toast.LENGTH_SHORT).show()
                    finish() // Close the Forgot Password screen
                } else {
                    // Handle specific error cases here
                    Toast.makeText(this, "Failed to send reset email. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
