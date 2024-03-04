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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[storagePermission] == true) {
                openGallery()
            } else {
                // Handle permission denial: explain why permission is needed, offer retry option
                Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show()
            }
        }

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

        //Take Photo From Gallery
        binding.updateimageView.setOnClickListener {
            if (checkSelfPermission(storagePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(storagePermission))
            } else {
                openGallery()
            }
        }
    }

    // Function For Upload Profile Image : Firebase
    private fun uploadProfilePic(){
        if (selectedImageUri != null){
            storageReference  = FirebaseStorage.getInstance().getReference("Users/"+auth.currentUser?.uid+".jpg")
            storageReference.putFile(selectedImageUri).addOnSuccessListener {

                Toast.makeText(this@UpdatePage,"Profile successfully added",Toast.LENGTH_SHORT).show()
                binding.updateprogressBar.visibility = View.INVISIBLE
                finish()

            }.addOnFailureListener{
                Toast.makeText(this@UpdatePage,"Failed to update profile",Toast.LENGTH_SHORT).show()
            }
        }
        else {
            finish()
        }

    }

    //Take Photo From Gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, 1)
        } catch (e: Exception) {
            // Handle exception when starting activity for result fails
            Toast.makeText(this, "Failed to open gallery.", Toast.LENGTH_SHORT).show()
        }
    }
    // Image
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
//            try {
//                selectedImageUri = data.data!!
//                val imageStream = contentResolver.openInputStream(selectedImageUri)
//                val selectedImage = BitmapFactory.decodeStream(imageStream)
//                binding.updateimageView.setImageBitmap(selectedImage)
//            } catch (e: Exception) {
//                // Handle exceptions when accessing or decoding image
//                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImageUri = data.data!!
                val imageStream = contentResolver.openInputStream(selectedImageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.updateimageView.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                // Handle exceptions when accessing or decoding image
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
            }
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

        //Validate Image
        if (binding.updateimageView.drawable == null) {
            Toast.makeText(this, "Please select a profile image.", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isValid){
            val uid = auth.currentUser?.uid
            val user = User(name,email,number,address)
            if(uid != null){

                databaseReference.child(uid).setValue(user).addOnCompleteListener{

                    if (it.isSuccessful){

                        uploadProfilePic()
                        binding.updateprogressBar.visibility = View.VISIBLE
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
                    getUserProfile()
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

    //GET USER IMAGE
    private fun getUserProfile() {

        storageReference = FirebaseStorage.getInstance().reference.child("Users/$uid.jpg")
        val localFile = File.createTempFile("tempImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener {

            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.updateimageView.setImageBitmap(bitmap)
        }.addOnFailureListener{

            Toast.makeText(this@UpdatePage,"Failed to retrieve image",Toast.LENGTH_SHORT).show()
        }
    }
}


