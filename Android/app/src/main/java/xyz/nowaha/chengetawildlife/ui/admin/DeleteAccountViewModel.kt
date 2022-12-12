package xyz.nowaha.chengetawildlife.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.AccountDeleteRequest
import xyz.nowaha.chengetawildlife.data.pojo.AccountDeleteResponse

class DeleteAccountViewModel : ViewModel() {
    val usernameInput = MutableLiveData("")

    val deleteAccountState =
        MutableLiveData<DeleteAccountState>(DeleteAccountState.WaitingForUserInput(null))

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        if (deleteAccountState.value !is DeleteAccountState.WaitingForUserInput) return@withContext
        deleteAccountState.postValue(DeleteAccountState.Loading)

        delay(500)

        var deleteAccountResponse: Response<AccountDeleteResponse>? = null
        try {
            deleteAccountResponse = APIClient.getAPIInterface().deleteAccount(
                AccountDeleteRequest(
                    usernameInput.value.toString()
                )
            ).execute()
        } catch (_: Exception) {

        }
        if (deleteAccountResponse?.body() == null && (deleteAccountResponse?.errorBody() == null || deleteAccountResponse.code() == 404)) {
            deleteAccountState.postValue(
                DeleteAccountState.WaitingForUserInput(
                    DeleteAccountState.DeleteAccountErrorType.CONNECTION_FAILURE
                )
            )
            return@withContext
        }

        if (deleteAccountResponse.errorBody() != null) {
            if (deleteAccountResponse.code() == 400)
                deleteAccountState.postValue(
                    DeleteAccountState.WaitingForUserInput(
                        DeleteAccountState.DeleteAccountErrorType.USERNAME_NOT_FOUND
                    )
                )
            else
                deleteAccountState.postValue(
                    DeleteAccountState.WaitingForUserInput(
                        DeleteAccountState.DeleteAccountErrorType.UNKNOWN_ERROR
                    )
                )

            return@withContext
        }

        deleteAccountState.postValue(DeleteAccountState.AccountDeleted)
    }

    sealed class DeleteAccountState {
        data class WaitingForUserInput(val error: DeleteAccountErrorType?) : DeleteAccountState()
        object Loading : DeleteAccountState()
        object AccountDeleted : DeleteAccountState()

        enum class DeleteAccountErrorType {
            CONNECTION_FAILURE, USERNAME_NOT_FOUND, UNKNOWN_ERROR
        }
    }

}
