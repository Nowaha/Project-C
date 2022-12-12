package xyz.nowaha.chengetawildlife.data.pojo

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val sessionKey: String,
    val isAdmin: Boolean
)
