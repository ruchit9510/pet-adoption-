package com.example.mypet

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mypet.databinding.ActivitySellBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class Sell : AppCompatActivity() {
    private lateinit var binding: ActivitySellBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Pets")
        storageReference = FirebaseStorage.getInstance().reference

        // Check and request storage permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_sell

        bottomNavigationView.setOnItemSelectedListener { item ->
            // Handle bottom navigation item selection
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(this, Home::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                R.id.bottom_favorite -> {
                    startActivity(Intent(this, Favorite::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                R.id.bottom_sell -> true
                R.id.bottom_forum -> {
                    startActivity(Intent(this, Forum::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set OnClickListener to petImage2 ImageView to select an image from gallery
        binding.petImage2.setOnClickListener {
            // Launch intent to open gallery
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        binding.petSaveBtn.setOnClickListener {
            if (validateFields()) {
                uploadPetInformation()
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

        // Check for Pet Age
        if (petAge.isEmpty()) {
            binding.petAge.error = "Please Enter Your Pet Age"
            isValid = false
        } else {
            val age = petAge.toIntOrNull()
            if (age == null || age <= 0 || age > 30) {
                binding.petAge.error = "Please enter a valid age"
                isValid = false
            }
        }

        // Validate pet price
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
        val petBreed = binding.petBreed.text.toString().trim()
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
                            savePetToDatabase(userId, petType, petAge.toString(), petName, petBreed, petPrice, petDescription, imageUrl)
                        }
                    } else {
                        Toast.makeText(this, "Failed to get image URL.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss() // Dismiss progress dialog on failure
                Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save pet information to Firebase Database
    private fun savePetToDatabase(
        userId: String,
        petType: String,
        petAge: String,
        petName: String,
        petBreed: String,
        petPrice: String,
        petDescription: String,
        imageUrl: String
    ) {
        val petId = databaseReference.push().key.toString() // Unique key
        val pet = Pet(petBreed, petDescription, petId, imageUrl, petName, petAge, petType, petPrice ,userId)
        databaseReference.child(petId).setValue(pet)
            .addOnSuccessListener {
                Toast.makeText(this, "Pet information saved successfully!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Failed to save pet information: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, do nothing
        } else {
//            Toast.makeText(this, "Permission denied. You cannot access images from gallery.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle result of gallery intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Get the Uri of the selected image
            selectedImageUri = data.data
            // Set the selected image to your ImageView
            binding.petImage2.setImageURI(selectedImageUri)
        }
    }
}
