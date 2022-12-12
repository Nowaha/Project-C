package xyz.nowaha.chengetawildlife.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.AccountEditRequest
import xyz.nowaha.chengetawildlife.data.pojo.AccountEditResponse

class EditAccountViewModel : ViewModel() {
    val usernameInput = MutableLiveData("")
    val passwordInput = MutableLiveData("")
    val passwordConfirmInput = MutableLiveData("")

    val editAccountState =
        MutableLiveData<EditAccountState>(EditAccountState.WaitingForUserInput(null))

    suspend fun attemptEditAccount() = withContext(Dispatchers.IO) {
        if (editAccountState.value !is EditAccountState.WaitingForUserInput) return@withContext
        editAccountState.postValue(EditAccountState.Loading)

        delay(500)

        var editAccountResponse: Response<AccountEditResponse>? = null
        try {
            editAccountResponse = APIClient.getAPIInterface().attemptEditAccount(
                AccountEditRequest(
                    usernameInput.value.toString(),
                    passwordInput.value.toString()
                )
            ).execute()
        } catch (_: Exception) {

        }

        if (editAccountResponse?.body() == null && (editAccountResponse?.errorBody() == null || editAccountResponse.code() == 404)) {
            editAccountState.postValue(EditAccountState.WaitingForUserInput(EditAccountState.EditAccountErrorType.CONNECTION_FAILURE))
            return@withContext
        }

        if (editAccountResponse.errorBody() != null) {
            if (editAccountResponse.code() == 400)
                editAccountState.postValue(EditAccountState.WaitingForUserInput(EditAccountState.EditAccountErrorType.USERNAME_NOT_FOUND))
            else
                editAccountState.postValue(EditAccountState.WaitingForUserInput(EditAccountState.EditAccountErrorType.UNKNOWN_ERROR))

            return@withContext
        }

        editAccountState.postValue(EditAccountState.AccountEdited)
    }

    sealed class EditAccountState {
        data class WaitingForUserInput(val error: EditAccountErrorType?) : EditAccountState()
        object Loading : EditAccountState()
        object AccountEdited : EditAccountState()

        enum class EditAccountErrorType {
            CONNECTION_FAILURE, USERNAME_NOT_FOUND, UNKNOWN_ERROR
        }
    }

}