package com.example.mypet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.mypet.databinding.ActivityUpdatePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class UpdatePage : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var user: User
    private lateinit var uid : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_page)
        binding = ActivityUpdatePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        if (uid.isNotEmpty()){
            getUserData() // Fetch old data from Firebase
        }

        binding.updateBtn.setOnClickListener{
            validateFields()
        }

    }



    //VALIDATION.
    private fun validateFields() {
        val name = binding.updateName.text.toString().trim()
        val email = binding.updateEmail.text.toString().trim()
        val number = binding.updateMobilenumber.text.toString().trim()
        val address = binding.updateAddress.text.toString().trim()

        var isValid = true

        // Validate name
        if (name.isEmpty()) {
            binding.updateName.error = "Please enter your name."
            isValid = false
        }

        // Validate mobile number
        if (number.isEmpty()) {
            binding.updateMobilenumber.error = "Please enter your mobile number."
            isValid = false
        } else if (!isValidIndianMobileNumber(number)) {
            binding.updateMobilenumber.error = "Please enter a valid Indian mobile number."
            isValid = false
        }

        // Validate email (Gmail specific)
        if (email.isEmpty()) {
            binding.updateEmail.error = "Please enter your email address."
            isValid = false
        } else if (!email.endsWith("@gmail.com")) {
            binding.updateEmail.error = "Please enter a valid Gmail address."
            isValid = false
        }

        // Validate address
        if (address.isEmpty()) {
            binding.updateAddress.error = "Please enter your address."
            isValid = false
        }

        if (isValid) {
            val uid = auth.currentUser?.uid
            val user = User(name, email, number, address)
            if (uid != null) {
                binding.updateprogressBar.visibility = View.VISIBLE
                databaseReference.child(uid).setValue(user).addOnCompleteListener {
                    if (it.isSuccessful) {
                        finish()
                    } else {
                        Toast.makeText(this@UpdatePage, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Function to validate Indian mobile number
    private fun isValidIndianMobileNumber(number: String): Boolean {
        val regex = "^[6789]\\d{9}$".toRegex() // Matches 10 digit numbers starting with 6, 7, 8, or 9
        return number.matches(regex)
    }


    // FETCH USER DATA
    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if snapshot exists
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)!!
                    binding.updateName.setText(user.name)
                    binding.updateEmail.setText(user.mail)
                    binding.updateMobilenumber.setText(user.number)
                    binding.updateAddress.setText(user.address)
                } else {
                    // Handle non-existent user data
                    Toast.makeText(this@UpdatePage, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdatePage, "Failed to retrieve profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

}


