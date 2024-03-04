package com.example.mypet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.example.mypet.databinding.ActivitySingUpBinding// Import generated binding class
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySingUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater) // Inflate layout using binding
        setContentView(binding.root) // Set root view as content

        auth = FirebaseAuth.getInstance()

        binding.textViewSignin.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

        binding.signupBtn.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        val email = binding.editTextTextEmailAddress2.text.toString().trim()
        val password = binding.editTextTextPassword2.text.toString().trim()
        val confirmPassword = binding.editTextTextPassword3.text.toString().trim()

        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            binding.editTextTextEmailAddress2.error = "Please enter your email address."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextTextEmailAddress2.error = "Please enter a valid email address."
            isValid = false
        }

        // Validate passwords
        if (password.isEmpty()) {
            binding.editTextTextPassword2.error = "Please enter your password."
            isValid = false
        }
        if (confirmPassword.isEmpty()) {
            binding.editTextTextPassword3.error = "Please confirm your password."
            isValid = false
        } else if (password.length < 8) {
            binding.editTextTextPassword2.error = "Password must be 8 characters or more."
            isValid = false
        }
        if (password != confirmPassword) {
            binding.editTextTextPassword3.error = "Passwords do not match."
            isValid = false
        }

        // If all fields are valid, proceed with sign-up process
        if (isValid) {
            createUserWithEmailAndPassword(email, password)
            binding.progressBar2.visibility = View.VISIBLE
        }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar2.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign up success, navigate to the next activity
                    Log.d("SignUp", "createUserWithEmail:success")
                    auth.currentUser
                    binding.editTextTextEmailAddress2.text.clear()
                    binding.editTextTextPassword2.text.clear()
                    binding.editTextTextPassword3.text.clear()
                    finish()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: Exception) {
                        when(e){
                            is FirebaseAuthInvalidCredentialsException ->{
                                Toast.makeText(this,"Please Enter Valid Mail.",Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthUserCollisionException ->{
                                Toast.makeText(baseContext, "This email is already used.", Toast.LENGTH_SHORT).show()
                            }
                            is Exception ->{
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
    }
}
