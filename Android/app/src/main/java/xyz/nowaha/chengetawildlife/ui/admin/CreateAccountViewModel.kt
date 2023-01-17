package xyz.nowaha.chengetawildlife.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.AccountCreationRequest
import xyz.nowaha.chengetawildlife.data.pojo.AccountCreationResponse

class CreateAccountViewModel : ViewModel() {
    val usernameInput = MutableLiveData("")
    val passwordInput = MutableLiveData("")
    val passwordConfirmInput = MutableLiveData("")
    val roleInput = MutableLiveData(0)
    val firstnameInput = MutableLiveData("")
    val lastnameInput = MutableLiveData("")


    val createAccountState =
        MutableLiveData<CreateAccountState>(CreateAccountState.WaitingForUserInput(null))

    suspend fun attemptCreateAccount() = withContext(Dispatchers.IO) {
        if (createAccountState.value !is CreateAccountState.WaitingForUserInput) return@withContext
        createAccountState.postValue(CreateAccountState.Loading)

        delay(100)

        var createAccountResponse: Response<AccountCreationResponse>? = null
        try {
            createAccountResponse = APIClient.getAPIInterface().attemptCreateAccount(
                AccountCreationRequest(
                    usernameInput.value.toString(),
                    passwordInput.value.toString(),
                    roleInput.value ?: 0,
                    firstnameInput.value.toString(),
                    lastnameInput.value.toString(),
                    )
            ).execute()
        } catch (_: Exception) {
        }

        if (createAccountResponse?.body() == null && (createAccountResponse?.errorBody() == null || createAccountResponse.code() == 404)) {
            createAccountState.postValue(CreateAccountState.WaitingForUserInput(CreateAccountState.CreateAccountErrorType.CONNECTION_FAILURE))
            return@withContext
        }

        if (createAccountResponse.errorBody() != null) {
            if (createAccountResponse.code() == 400)
                createAccountState.postValue(
                    CreateAccountState.WaitingForUserInput(
                        CreateAccountState.CreateAccountErrorType.USERNAME_IN_USE
                    )
                )
            else
                createAccountState.postValue(
                    CreateAccountState.WaitingForUserInput(
                        CreateAccountState.CreateAccountErrorType.UNKNOWN_ERROR
                    )
                )

            return@withContext
        }

        createAccountState.postValue(CreateAccountState.AccountCreated)
    }

    sealed class CreateAccountState {
        data class WaitingForUserInput(val error: CreateAccountErrorType?) : CreateAccountState()
        object Loading : CreateAccountState()
        object AccountCreated : CreateAccountState()

        enum class CreateAccountErrorType {
            CONNECTION_FAILURE, USERNAME_IN_USE, UNKNOWN_ERROR
        }
    }

}