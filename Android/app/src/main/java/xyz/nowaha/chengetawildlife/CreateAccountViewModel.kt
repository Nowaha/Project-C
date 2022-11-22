package xyz.nowaha.chengetawildlife

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.AccountCreationRequest
import xyz.nowaha.chengetawildlife.pojo.AccountCreationResponse

class CreateAccountViewModel : ViewModel()
{
    val usernameInput = MutableLiveData("")
    val passwordInput = MutableLiveData("")
    val roleInput = MutableLiveData(0)

    val createAccountState = MutableLiveData<CreateAccountState>(CreateAccountState.WaitingForUserInput(null))

    suspend fun attemptCreateAccount() = withContext(Dispatchers.IO)
    {
        if(createAccountState.value !is CreateAccountState.WaitingForUserInput) return@withContext
        createAccountState.postValue(CreateAccountState.Loading)

        delay(500)

        var createAccountResponse : Response<AccountCreationResponse>? = null
        try {
            createAccountResponse = APIClient.getAPIInterface()
                .attemptCreateAccount(AccountCreationRequest(usernameInput.toString(),passwordInput.toString(), roleInput.value ?: 0 )
            ).execute()
        }
        catch(Exception : Exception)
        {

        }

        if (createAccountResponse?.body() == null && createAccountResponse?.errorBody() == null)
        {
            createAccountState.postValue(CreateAccountState.WaitingForUserInput(CreateAccountState.CreateAccountErrorType.CONNECTION_FAILURE))
            return@withContext
        }
        if (createAccountResponse.errorBody() == null)
        {
            createAccountState.postValue(CreateAccountState.WaitingForUserInput(CreateAccountState.CreateAccountErrorType.USERNAME_IN_USE))
            return@withContext
        }
        createAccountState.postValue(CreateAccountState.accountCreated)
    }

    sealed class CreateAccountState
    {
        data class WaitingForUserInput(val error: CreateAccountErrorType?) : CreateAccountState()
        object Loading : CreateAccountState()
        object accountCreated : CreateAccountState()

        enum class CreateAccountErrorType
        {
            CONNECTION_FAILURE,USERNAME_IN_USE
        }



    }





}