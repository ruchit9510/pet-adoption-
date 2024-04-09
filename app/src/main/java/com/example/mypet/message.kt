package com.example.mypet

import java.util.*

class message {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Date? = null // Add timestamp field

    constructor() {}
    constructor(message: String?, senderId: String?, timestamp: Date?) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }
}
