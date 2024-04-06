package com.example.mypet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mypet.databinding.PetItemsBinding
import com.squareup.picasso.Picasso

class PetAdapter(private val petList: List<Pet>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<PetAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: PetItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(petList[position].id) // Pass pet ID to listener
                }
            }
            binding.imageButton2.setOnClickListener {
                // Handle favorite button click event
            }
        }

        fun bind(pet: Pet) {
            binding.apply {
                petName3.text = pet.name
                petType3.text = pet.petType
                // Load image using Picasso
                Picasso.get().load(pet.imageUrl1).into(imageView6)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PetItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(petList[position])
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    interface OnItemClickListener {
        fun onItemClick(petId: String)
    }
}
