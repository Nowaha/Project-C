package xyz.nowaha.chengetawildlife.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.AccountEditNameRequest
import xyz.nowaha.chengetawildlife.data.pojo.AccountEditNameResponse

class EditNameAccountViewModel : ViewModel() {
    val usernameInput = MutableLiveData("")
    val firstnameInput = MutableLiveData("")
    val lastnameInput = MutableLiveData("")

    val editNameAccountState =
        MutableLiveData<EditNameAccountState>(EditNameAccountState.WaitingForUserInput(null))

    suspend fun attemptEditNameAccount() = withContext(Dispatchers.IO) {
        if (editNameAccountState.value !is EditNameAccountState.WaitingForUserInput) return@withContext
        editNameAccountState.postValue(EditNameAccountState.Loading)

        delay(100)

        var editNameAccountResponse: Response<AccountEditNameResponse>? = null
        try {
            editNameAccountResponse = APIClient.getAPIInterface().attemptEditNameAccount(
                AccountEditNameRequest(
                    usernameInput.value.toString(),
                    firstnameInput.value.toString(),
                    lastnameInput.value.toString()
                )
            ).execute()
        } catch (_: Exception) {

        }

        if (editNameAccountResponse?.body() == null && (editNameAccountResponse?.errorBody() == null || editNameAccountResponse.code() == 404)) {
            editNameAccountState.postValue(EditNameAccountState.WaitingForUserInput(
                EditNameAccountState.EditNameAccountErrorType.CONNECTION_FAILURE))
            return@withContext
        }

        if (editNameAccountResponse.errorBody() != null) {
            if (editNameAccountResponse.code() == 400)
                editNameAccountState.postValue(
                    EditNameAccountViewModel.EditNameAccountState.WaitingForUserInput(
                        EditNameAccountViewModel.EditNameAccountState.EditNameAccountErrorType.USERNAME_NOT_FOUND))
            else
                editNameAccountState.postValue(
                    EditNameAccountViewModel.EditNameAccountState.WaitingForUserInput(
                        EditNameAccountViewModel.EditNameAccountState.EditNameAccountErrorType.UNKNOWN_ERROR))

            return@withContext
        }

        editNameAccountState.postValue(EditNameAccountState.AccountNameEdited)
    }

    sealed class EditNameAccountState {
        data class WaitingForUserInput(val error: EditNameAccountErrorType?) : EditNameAccountState()
        object Loading : EditNameAccountState()
        object AccountNameEdited : EditNameAccountState()

        enum class EditNameAccountErrorType {
            CONNECTION_FAILURE, USERNAME_NOT_FOUND, UNKNOWN_ERROR
        }
    }

}