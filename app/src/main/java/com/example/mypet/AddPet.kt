package com.example.mypet

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RadioButton
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mypet.databinding.ActivityAddPetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class AddPet : AppCompatActivity() {
    private lateinit var binding: ActivityAddPetBinding
    private val storagePermission = Manifest.permission.READ_MEDIA_IMAGES
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[storagePermission] == true) {
                openGallery()
            } else {
                // Handle permission denial
                Toast.makeText(
                    this,
                    "Storage permission is required to access images.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Pets")
        storageReference = FirebaseStorage.getInstance().reference

        binding.petSaveBtn.setOnClickListener {
            if (validateFields()) {

                uploadPetInformation()
            }
        }

        // Image selection
        binding.petImage2.setOnClickListener {
            if (checkSelfPermission(storagePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(storagePermission))
            } else {
                openGallery()
            }
        }
    }

    // Open gallery for image selection
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open gallery.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImageUri = data.data!!
                val imageStream = contentResolver.openInputStream(selectedImageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.petImage2.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image.", LENGTH_SHORT).show()
            }
        }
    }

    // Validate fields
    private fun validateFields(): Boolean {
        var isValid = true

        val petAge = binding.petAge.text.toString().trim()
        val petBreed = binding.petBreed.text.toString().trim()
        val petPrice = binding.petPrice.text.toString().trim()

        val petDescription = binding.petDescription.text.toString().trim()

        // Check if Pet Breed is empty
        if (petBreed.isEmpty()) {
            binding.petBreed.error = "Please Enter Your Pet Breed"
            isValid = false
        }

        // Check if Description is empty
        if (petDescription.isEmpty()) {
            binding.petDescription.error = "Please Enter Your Pet Description"
            isValid = false
        }

        //Chek for Pet Age
        if (petAge.isEmpty()){
            binding.petAge.error = "Please Enter Your Pet Age"
            isValid = false
        }else{
            val age = petAge.toIntOrNull()
            if (age == null || age<=0 || age>30){
                binding.petAge.error = "Please enter a valid age"
                isValid = false
            }
        }

        // You can add further validations for pet price format (optional)
          try {
              val price = petPrice.toDouble()
              if (price <= 0) {
                  binding.petPrice.error = "Please enter a valid price."
                  isValid = false
              }
          } catch (e: Exception) {
              binding.petPrice.error = "Please enter a valid price."
              isValid = false
          }

        return isValid
    }


    // Upload pet information to Firebase
    private fun uploadPetInformation() {
        val checkedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val checkedRadioButton = findViewById<RadioButton>(checkedRadioButtonId)
        val petType = checkedRadioButton.text.toString().trim()
        val petAge = binding.petAge.text.trim()
        val petName = binding.petName.text.toString().trim()
        val petPrice = binding.petPrice.text.toString().trim()
        val petDescription = binding.petDescription.text.toString().trim()
        // Get current user ID
        val userId = auth.currentUser?.uid

        // Check if image is selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image for your pet.", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading pet information...")
        progressDialog.show()

// Create a unique filename for the image
        val filename = UUID.randomUUID().toString() + ".jpg" // Or any other image format

        // Upload image to Firebase Storage
        val imageRef = storageReference.child("pets/$filename")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                progressDialog.dismiss() // Dismiss progress dialog on success
                // Get the uploaded image URL
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val imageUrl = task.result.toString()
                        // Save pet information to Firebase Database with image URL
                        if (userId != null) {
                            savePetToDatabase(userId,petType,
                                petAge.toString(), petName, petPrice, petDescription, imageUrl)
                        }
                    } else {
                        Toast.makeText(this, "Failed to get image URL.", LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss() // Dismiss progress dialog on failure
                Toast.makeText(
                    this,
                    "Failed to upload image: ${exception.message}",
                    LENGTH_SHORT
                ).show()
            }
    }

    // Save pet information to Firebase Database
    private fun savePetToDatabase(
        userId: String,
        petType: String,
        petAge: String,
        petName: String,
        petPrice: String,
        petDescription: String,
        imageUrl: String
    ) {
        val petId = databaseReference.push().key.toString() // Unique key
        val pet = Pet(userId, petType, petAge , petName, petPrice, petDescription, imageUrl)
        databaseReference.child(petId).setValue(pet)
            .addOnSuccessListener {
                Toast.makeText(this, "Pet information saved successfully!", Toast.LENGTH_SHORT)
                    .show()
                // Clear fields after successful save (optional)
                binding.petName.text.clear()
                binding.petAge.text.clear()
                binding.petBreed.text.clear()
                binding.petPrice.text.clear()
                binding.petDescription.text.clear()
                binding.radioGroup.clearCheck()
                binding.petImage2.setImageBitmap(null)
                startActivity(Intent(this, Home::class.java))
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to save pet information: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}