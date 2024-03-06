package com.example.mypet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddProfile : AppCompatActivity() {

    private lateinit var binding: ActivityAddProfileBinding
    private val storagePermission = Manifest.permission.READ_MEDIA_IMAGES
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private var selectedImageUri: Uri? = null
    private lateinit var sharedPref: SharedPreferences
    private val PREF_KEY_IS_LOGGED_IN = "isLoggedIn"

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
        binding = ActivityAddProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean(PREF_KEY_IS_LOGGED_IN, false).apply()

        binding.saveBtn.setOnClickListener{
            validateFields()
        }


        //Take storage Permission from device
        binding.imageView.setOnClickListener {
            if (checkSelfPermission(storagePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(storagePermission))
            } else {
                openGallery()
            }
        }


    }
// Function For Upload Profile Image : Firebase
private fun uploadProfilePic(){
    val uid = auth.currentUser?.uid
    uid?.let { userId ->
        val profileImageRef = FirebaseStorage.getInstance().getReference("Users/$userId.jpg")
        val uri = selectedImageUri
        if (uri != null) {
            profileImageRef.putFile(uri).addOnSuccessListener { _ ->
                Toast.makeText(this@AddProfile,"Profile successfully added",Toast.LENGTH_SHORT).show()
                binding.progressBar6.visibility = View.INVISIBLE
                binding.profileAddress.text.clear()
                binding.profileEmail.text.clear()
                binding.profileMobilenumber.text.clear()
                binding.profileName.text.clear()
                startActivity(Intent(this, Home::class.java))
                sharedPref.edit().putBoolean(PREF_KEY_IS_LOGGED_IN, true).apply()
            }.addOnFailureListener { exception ->
                Toast.makeText(this@AddProfile,"Failed to update profile: ${exception.message}",Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@AddProfile, "Selected image is null", Toast.LENGTH_SHORT).show()
        }
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
               selectedImageUri = data.data!!
                val imageStream = contentResolver.openInputStream(selectedImageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.imageView.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                // Handle exceptions when accessing or decoding image
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // BackPress Button
    private var backPressCount = 0
    override fun onBackPressed() {
        if (backPressCount == 0) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
            backPressCount++
        } else {
            super.onBackPressed()
            finishAffinity()
        }
    }


    //VALIDATION.
    private fun validateFields() {
        val name = binding.profileName.text.toString().trim()
        val email = binding.profileEmail.text.toString().trim()
        val number = binding.profileMobilenumber.text.toString().trim()
        val address = binding.profileAddress.text.toString().trim()

        var isValid = true

        // Validate name
        if (name.isEmpty()) {
            binding.profileName.error = "Please enter your name."
            isValid = false
        }

        // Validate mobile number
        if (number.isEmpty()) {
            binding.profileMobilenumber.error = "Please enter your mobile number."
            isValid = false
        } else if (!isValidIndianMobileNumber(number)) {
            binding.profileMobilenumber.error = "Please enter a valid Indian mobile number."
            isValid = false
        }

        // Validate email (Gmail specific)
        if (email.isEmpty()) {
            binding.profileEmail.error = "Please enter your email address."
            isValid = false
        } else if (!email.endsWith("@gmail.com")) {
            binding.profileEmail.error = "Please enter a valid Gmail address."
            isValid = false
        }

        //Validate address
        if(address.isEmpty()){
            binding.profileAddress.error = "Please enter your address"
        }

        //Validate Image
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image.", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isValid){
            binding.progressBar6.visibility = View.VISIBLE
            val uid = auth.currentUser?.uid
            val user = User(name,email,number,address)
            if(uid != null){

                databaseReference.child(uid).setValue(user).addOnCompleteListener{

                    if (it.isSuccessful){
                        uploadProfilePic()
                    }
                    else{
                        Toast.makeText(this@AddProfile,"Failed to update profile",Toast.LENGTH_SHORT).show()
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


}



