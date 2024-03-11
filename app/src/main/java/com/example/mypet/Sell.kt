package com.example.mypet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mypet.databinding.ActivityAddPetBinding
import com.example.mypet.databinding.ActivitySellBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Sell : AppCompatActivity() {
    private lateinit var binding: ActivitySellBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellBinding.inflate(this.layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_sell

        bottomNavigationView.setOnItemSelectedListener { item ->
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
        binding.addPet.setOnClickListener(){
            startActivity(Intent(this , AddPet::class.java))
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