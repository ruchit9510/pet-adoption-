package com.example.mypet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mypet.databinding.ActivityAddProfileBinding
import com.example.mypet.databinding.ActivityUpdatePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class UpdatePage : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePageBinding
    private val storagePermission = Manifest.permission.READ_MEDIA_IMAGES
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference : StorageReference
    private lateinit var selectedImageUri: Uri
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
        }
        // Validate email
        if (email.isEmpty()) {
            binding.updateEmail.error = "Please enter your email address."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.updateEmail.error = "Please enter a valid email address."
            isValid = false
        }

        //Validate address
        if(address.isEmpty()){
            binding.updateAddress.error = "Please enter your address"
        }

        if (isValid){
            val uid = auth.currentUser?.uid
            val user = User(name,email,number,address)
            if(uid != null){
                binding.updateprogressBar.visibility = View.VISIBLE
                databaseReference.child(uid).setValue(user).addOnCompleteListener{

                    if (it.isSuccessful){
                        finish()
                    }
                    else{
                        Toast.makeText(this@UpdatePage,"Failed to update profile",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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


