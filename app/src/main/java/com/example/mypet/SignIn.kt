package com.example.mypet


import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.mypet.databinding.ActivitySignInBinding // Import generated binding class
import com.google.firebase.database.FirebaseDatabase


class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding // Use binding object
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private val PREF_KEY_IS_LOGGED_IN = "isLoggedIn"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater) // Inflate layout using binding
        setContentView(binding.root) // Set root view as content

        auth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)

       if (sharedPref.getBoolean(PREF_KEY_IS_LOGGED_IN, false)) {
          startActivity(Intent(this, Home::class.java)) // Or the appropriate activity
           finish()
          return // Skip login flow if already logged in
      }


        binding.signup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        binding.forgot.setOnClickListener {
            startActivity(Intent(this, Forgot::class.java))
        }

        binding.signinBtn.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        val email = binding.editTextText.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            binding.editTextText.error = "Please enter your email address."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextText.error = "Please enter a valid email address."
            isValid = false
        }

        if (password.isEmpty()) {
            binding.editTextTextPassword.error = "Please enter your password."
            isValid = false
        }

        if (isValid) {
            signInWithFirebase(email, password)
            binding.progressBar3.visibility = View.VISIBLE
        }

    }

    // Sign in with firebase and check for profile
    private fun signInWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignIn", "signInWithEmail:success")
                    val user = auth.currentUser
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("Users")
                    reference.child(user!!.uid).get().addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            binding.progressBar3.visibility = View.GONE
                            binding.editTextText.text.clear()
                            binding.editTextTextPassword.text.clear()
                            val profileSnapshot = profileTask.result
                            if (profileSnapshot.exists()) {
                                // Profile exists, go to home page
                                startActivity(Intent(this, Home::class.java))
                            } else {
                                // Profile doesn't exist, go to add profile page
                                startActivity(Intent(this, AddProfile::class.java))
                            }
                            sharedPref.edit().putBoolean(PREF_KEY_IS_LOGGED_IN, true).apply()
                        } else {
                            // Handle database error
                            Log.w("SignIn", "Error checking user profile", profileTask.exception)
                            Toast.makeText(baseContext, "Failed to check profile, try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else {
                    // If sign in fails, display a message to the user.
                    binding.progressBar3.visibility = View.GONE
                    Log.w("SignIn", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "The email address or password is incorrect.", Toast.LENGTH_LONG).show()
                    Toast.makeText(baseContext, "Please retry....", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SignIn::class.java))
                }
            }
    }
    // BackPress Button
    private var backPressCount = 0
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backPressCount == 0) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
            backPressCount++
        } else {
            super.onBackPressed()
            finishAffinity()
        }
    }
}


