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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mypet.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var user: User
    private val storagePermission = Manifest.permission.READ_MEDIA_IMAGES
    private lateinit var selectedImageUri: Uri
    private lateinit var sharedPref: SharedPreferences
    private val PREF_KEY_IS_LOGGED_IN = "isLoggedIn"
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
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)


        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        if (uid.isNotEmpty()){
            getUserData()
        }

        //UPDATE BUTTON
        binding.updateBtn1.setOnClickListener{
            startActivity(Intent(this,UpdatePage::class.java))
        }


        //Logout Button
        binding.logoutBtn.setOnClickListener{
            Firebase.auth.signOut()
            sharedPref.edit().putBoolean(PREF_KEY_IS_LOGGED_IN, false).apply()
            startActivity(Intent(this,SignIn::class.java))
        }

        binding.bottomNavigation.selectedItemId = R.id.bottom_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
                R.id.bottom_sell -> {
                    startActivity(Intent(this, Sell::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                R.id.bottom_forum -> {
                    startActivity(Intent(this, Forum::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                R.id.bottom_profile -> true

                else -> false
            }
        }
        binding.profileImage.setOnClickListener {
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

                Toast.makeText(this@Profile,"Profile successfully added",Toast.LENGTH_SHORT).show()

            }.addOnFailureListener{
                Toast.makeText(this@Profile,"Failed to update profile",Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImageUri = data.data!!
                val imageStream = contentResolver.openInputStream(selectedImageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.profileImage.setImageBitmap(selectedImage)
                uploadProfilePic()
            } catch (e: Exception) {
                // Handle exceptions when accessing or decoding image
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
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
   // FETCH USER DATA
    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if snapshot exists
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)!!
                    binding.userName2.text = user.name
                    binding.userMail2.text = user.mail
                    binding.userNumber2.text = user.number
                    binding.userAddress2.text = user.address
                    getUserProfile()
                } else {
                    // Handle non-existent user data
                    Toast.makeText(this@Profile, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Failed to retrieve profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //GET USER IMAGE
    private fun getUserProfile() {

        storageReference = FirebaseStorage.getInstance().reference.child("Users/$uid.jpg")
        val localFile = File.createTempFile("tempImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener {

            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.profileImage.setImageBitmap(bitmap)
        }.addOnFailureListener{

            Toast.makeText(this@Profile,"Failed to retrieve image",Toast.LENGTH_SHORT).show()
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
