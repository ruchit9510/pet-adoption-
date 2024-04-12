package com.example.mypet

data class Pet(
    var breed: String? = null, // Nullable because it's missing in some Firebase entries
    var description: String? = null, // Nullable because it's missing in some Firebase entries
    var id: String = "",
    var imageUrl1: String = "",
    var name: String = "",
    var petAge: String = "",
    var petType: String = "",
    var price: String = "",
    var userId: String = ""
)
