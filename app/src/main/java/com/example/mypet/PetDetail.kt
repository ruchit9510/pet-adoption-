package com.example.mypet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.example.mypet.databinding.PetItemsDetailBinding

class PetDetail : AppCompatActivity() {

    private lateinit var petId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var petRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    private lateinit var binding: PetItemsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase components
        auth = Firebase.auth
        petRef = FirebaseDatabase.getInstance().getReference("Pets")
        userRef = FirebaseDatabase.getInstance().getReference("Users")

        // Inflate layout using view binding
        binding = PetItemsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the pet ID passed from the previous activity
        petId = intent.getStringExtra("petId") ?: ""

        // Fetch pet details from Firebase
        petRef.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pet = snapshot.getValue(Pet::class.java)
                    pet?.let {
                        // Populate UI with pet details
                        binding.petName2.text = it.name
                        binding.petBreed2.text = it.breed
                        binding.petType2.text = it.petType
                        binding.petAge2.text = it.petAge
                        binding.perPrice2.text = it.price
                        binding.petDescription2.text = it.description

                        // Fetch owner details from Firebase
                        userRef.child(it.userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                if (userSnapshot.exists()) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    user?.let {
                                        // Populate UI with owner details
                                        binding.perOwnerName.text = it.name
                                        binding.petCallButton.text = it.number
                                        binding.petEmailButton.text = it.mail
                                        binding.petAddress2.text = it.address
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle errors
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }
}