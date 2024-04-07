package com.example.mypet

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class User(
    var name: String = "",
    @get:PropertyName("number") @set:PropertyName("number") var number: String = "",
    @get:PropertyName("mail") @set:PropertyName("mail") var mail: String = "",
    var address: String = ""
)