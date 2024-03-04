package com.example.mypet


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
    private lateinit var sharedPref: SharedPreferences
    private val PREF_KEY_IS_LOGGED_IN = "isLoggedIn"
    private lateinit var uid : String

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
            binding.imageView2.setImageBitmap(bitmap)
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
