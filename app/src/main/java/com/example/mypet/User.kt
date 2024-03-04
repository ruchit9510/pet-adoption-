package com.example.mypet

data class User(
    var name: String? = null,
    var mail: String? = null,
    var number: String? = null,
    var address: String? = null
) {
    // Add a no-argument constructor here
    constructor() : this(null, null, null, null) {}
}
