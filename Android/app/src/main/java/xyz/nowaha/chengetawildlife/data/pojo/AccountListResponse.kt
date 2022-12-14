package xyz.nowaha.chengetawildlife.data.pojo

data class AccountListResponse(
    val success: Boolean,
    val message: String,
    val data: ArrayList<Account>?
)