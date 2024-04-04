package com.example.mypet

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.mypet.databinding.ActivityMainBinding // Import generated binding class

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Use binding object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflate layout using binding
        setContentView(binding.root) // Set root view as content
        if (!isNetworkAvailable(this)) {
            showNoInternetDialog(this)
        } else {
            binding.floatingActionButton2.setOnClickListener {
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
        }
        
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    fun showNoInternetDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Warning!")
            .setMessage("Internet connection is not available. Please connect to the internet and try again.")
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                if (!isNetworkAvailable(this)) {
                    showNoInternetDialog(this)
                }
               else{
                    binding.floatingActionButton2.setOnClickListener {
                        val intent = Intent(this, SignIn::class.java)
                        startActivity(intent)
                    }
                }
            }
            .setCancelable(false) // Prevent dismissing without clicking OK
        builder.create().show()
    }

}

