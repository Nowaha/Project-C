package xyz.nowaha.chengetawildlife

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.LoginResponse

class LoginViewModel : ViewModel() {

    val usernameEntry = MutableLiveData("")
    val passwordEntry = MutableLiveData("")

    val loginState = MutableLiveData<LoginState>(LoginState.WaitingForUserInput(null))

    suspend fun attemptLogin() = withContext(Dispatchers.IO) {
        if (loginState.value !is LoginState.WaitingForUserInput) return@withContext
        loginState.postValue(LoginState.Loading)

        delay(500)

        var loginResponse: Response<LoginResponse>? = null
        try {
            loginResponse = APIClient.getAPIInterface().attemptLogin(usernameEntry.value ?: "", passwordEntry.value ?: "").execute()
        } catch (_: Exception) { }

        if (loginResponse?.body() == null && loginResponse?.errorBody() == null) {
            loginState.postValue(LoginState.WaitingForUserInput(LoginState.LoginErrorType.CONNECTION_FAILURE))
            return@withContext
        }
        if (loginResponse.errorBody() != null) {
            loginState.postValue(LoginState.WaitingForUserInput(LoginState.LoginErrorType.INVALID_CREDENTIALS))
            return@withContext
        }

        Session.key = loginResponse.body()!!.sessionKey
        loginState.postValue(LoginState.LoggedIn)
    }

    sealed class LoginState {
        data class WaitingForUserInput(val error: LoginErrorType?) : LoginState()
        object Loading : LoginState()
        object LoggedIn : LoginState()

        enum class LoginErrorType {
            CONNECTION_FAILURE, INVALID_CREDENTIALS
        }
    }

}