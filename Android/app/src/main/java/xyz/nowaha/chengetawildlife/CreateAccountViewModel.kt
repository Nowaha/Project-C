package xyz.nowaha.chengetawildlife

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.mutable.Mutable
import retrofit2.Response
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.AccountCreationResponse

class CreateAccountViewModel : ViewModel()
{
    val userNameInput = MutableLiveData("")
    val passwordInput = MutableLiveData("")

    val createAcountState = MutableLiveData<CreateAccountState>(CreateAccountState.WaitingForUserInput(null))

    suspend fun attemptCreateAccount() = withContext(Dispatchers.IO)
    {
        if(createAcountState.value !is CreateAccountState.WaitingForUserInput) return@withContext
        createAcountState.postValue(CreateAccountState.Loading)

        delay(500)

        var createAccountResponse : Response<AccountCreationResponse>? = null
        try {
            //createAccountResponse = APIClient.getAPIInterface().attemptCreateAccount().execute()
        }
        catch(Exception : Exception)
        {

        }






    }

    sealed class CreateAccountState
    {
        data class WaitingForUserInput(val error: createAccountErrorType?) : CreateAccountState()
        object Loading : CreateAccountState()
        object accountCreated : CreateAccountState()

        enum class createAccountErrorType
        {
            CONNECTION_FAILURE,USERNAME_IN_USE
        }



    }





}