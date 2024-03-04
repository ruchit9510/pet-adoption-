package com.example.mypet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mypet.databinding.ActivityMainBinding // Import generated binding class

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Use binding object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflate layout using binding
        setContentView(binding.root) // Set root view as content

        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }
    }
}
