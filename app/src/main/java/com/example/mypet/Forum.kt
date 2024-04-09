package com.example.mypet

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypet.databinding.ActivityForumBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class Forum : AppCompatActivity() {

    private lateinit var binding: ActivityForumBinding
    private lateinit var msgRecyclerView: RecyclerView
    private lateinit var msgBox: EditText
    private lateinit var msgSendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentUserID: String

    private val messageList = ArrayList<message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the current user ID
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

        msgRecyclerView = binding.msgRecyclerView
        msgBox = binding.msgBox
        msgSendButton = binding.msgSendButton

        messageAdapter = MessageAdapter(this, messageList, currentUserID)
        msgRecyclerView.adapter = messageAdapter
        msgRecyclerView.layoutManager = LinearLayoutManager(this)

        msgSendButton.setOnClickListener {
            val messageText = msgBox.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val message = message(messageText, currentUser?.uid ?: "", Date())
                sendMessageToFirestore(message)
                msgBox.text.clear()
            } else {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show()
            }
        }

        // Listen for changes in the Firestore collection and update the RecyclerView
        db.collection("messages")
            .orderBy("timestamp") // Order messages by timestamp
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, "Error fetching messages: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    messageList.clear()
                    for (document in snapshot.documents) {
                        val message = document.toObject(message::class.java)
                        message?.let {
                            messageList.add(message)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    msgRecyclerView.scrollToPosition(messageList.size - 1) // Scroll to last message
                }
            }
    }

    private fun sendMessageToFirestore(message: message) {
        db.collection("messages")
            .add(message)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
