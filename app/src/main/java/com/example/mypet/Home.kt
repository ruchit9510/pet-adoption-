package com.example.mypet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypet.databinding.ActivityHomeBinding
import com.google.firebase.database.*

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: PetAdapter
    private lateinit var petList: MutableList<Pet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.bottom_home

        // Initialize RecyclerView using binding
        val recyclerView: RecyclerView = binding.recyclerView

        // Set layout manager for RecyclerView
        val layoutManager = GridLayoutManager(this, 2) // 2 items per row
        recyclerView.layoutManager = layoutManager

        // Set up adapter
        petList = mutableListOf()
        adapter = PetAdapter(petList, object : PetAdapter.OnItemClickListener {
            override fun onItemClick(pet: Pet) {
                // Handle click event
            }
        })
        recyclerView.adapter = adapter

        // Fetch pet data from Firebase
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("Pets")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (petSnapshot in snapshot.children) {
                        val pet = petSnapshot.getValue(Pet::class.java)
                        pet?.let {
                            petList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                Toast.makeText(this@Home, "Failed to Fetch", Toast.LENGTH_SHORT).show()
            }
        })

        // Set bottom navigation click listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
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
                R.id.bottom_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

}
