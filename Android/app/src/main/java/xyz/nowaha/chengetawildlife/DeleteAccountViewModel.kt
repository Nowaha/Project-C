package xyz.nowaha.chengetawildlife

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.http.APIClient

import xyz.nowaha.chengetawildlife.pojo.AccountDeleteRequest
import xyz.nowaha.chengetawildlife.pojo.AccountDeleteResponse

class DeleteAccountViewModel: ViewModel() {

    val usernameInput = MutableLiveData("")

    val deleteAccountState =
        MutableLiveData<DeleteAccountViewModel.DeleteAccountState>(DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput(null))

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        if (deleteAccountState.value !is DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput) return@withContext
        deleteAccountState.postValue(DeleteAccountViewModel.DeleteAccountState.Loading)

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
                DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput(
                    DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.CONNECTION_FAILURE))
            return@withContext
        }

        if (deleteAccountResponse.errorBody() != null) {
            if (deleteAccountResponse.code() == 400)
                deleteAccountState.postValue(
                    DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput(
                        DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.USERNAME_IN_USE))
            else
                deleteAccountState.postValue(
                    DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput(
                        DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.UNKNOWN_ERROR))

            return@withContext
        }

        deleteAccountState.postValue(DeleteAccountViewModel.DeleteAccountState.AccountDeleted)
    }
    sealed class DeleteAccountState {
        data class WaitingForUserInput(val error: DeleteAccountErrorType?) : DeleteAccountState()
        object Loading : DeleteAccountState()
        object AccountDeleted : DeleteAccountState()

        enum class DeleteAccountErrorType {
            CONNECTION_FAILURE, USERNAME_IN_USE, UNKNOWN_ERROR
        }
    }

}
