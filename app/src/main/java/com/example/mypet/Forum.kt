package com.example.mypet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.mypet.databinding.ActivityForumBinding

class Forum : AppCompatActivity() {

    private lateinit var binding: ActivityForumBinding
    private lateinit var msgRecyclerView: RecyclerView
    private lateinit var msgBox: EditText
    private lateinit var msgSendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.bottom_forum

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
                R.id.bottom_forum -> true // No need for further action as it's already on this Activity
                R.id.bottom_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    overridePendingTransition(R.anim.slide_right, R.anim.slide_left)
                    finish()
                    true
                }
                else -> false
            }
        }

        val chatRecyclerView = binding.msgRecyclerView
        val messageBox = binding.msgBox
        val sendButton = binding.msgSendButton
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
