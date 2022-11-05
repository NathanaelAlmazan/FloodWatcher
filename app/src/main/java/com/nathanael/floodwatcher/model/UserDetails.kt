package com.nathanael.floodwatcher.model

data class UserDetails(
    var uid: String = "",
    val username: String = "",
    val email: String = "",
    val contact: String = "",
    val admin: Boolean = false,
    val superuser: Boolean = false
)
