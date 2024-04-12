package com.example.mypet

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MessageAdapter(
    private val context: Context,
    private val messageList: List<message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            val view = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == currentUserId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        if (holder is SentViewHolder) {
            holder.sentMessage.text = message.message
            // Set the username in the SentViewHolder
            setUsername(message.senderId, holder.sentUser)
        } else if (holder is ReceiveViewHolder) {
            holder.receiveMessage.text = message.message
            // Set the username in the ReceiveViewHolder
            setUsername(message.senderId, holder.receiveUser)
        }
    }

    private fun setUsername(userId: String?, textView: TextView) {
        if (userId == null) return

        // Fetch the user document from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("name")
                    textView.text = username
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessageAdapter", "Failed to fetch user name: ${e.message}")
            }
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.sent_message)
        val sentUser: TextView = itemView.findViewById(R.id.sent_user)
    }

    inner class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_message)
        val receiveUser: TextView = itemView.findViewById(R.id.receive_user)
    }
}