package xyz.nowaha.chengetawildlife.pojo

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val sessionKey: String,
    val isAdmin: Boolean
)
