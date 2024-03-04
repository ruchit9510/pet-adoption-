package com.example.mypet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mypet.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.bottom_home

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
