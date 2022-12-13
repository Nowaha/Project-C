package xyz.nowaha.chengetawildlife.data.pojo

data class SessionValidationResponse(
    val success: Boolean,
    val message: String,
    val isAdmin: Boolean?,
    val valid: Boolean?,
    val username: String?
)

