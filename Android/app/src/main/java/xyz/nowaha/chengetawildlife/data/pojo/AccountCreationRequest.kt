package xyz.nowaha.chengetawildlife.data.pojo

data class AccountCreationRequest(
    val username: String,
    val password: String,
    val role: Int,
    val firstName: String,
    val lastName: String
)