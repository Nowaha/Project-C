package xyz.nowaha.chengetawildlife.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.AccountListResponse

class AccountOverviewViewModel : ViewModel() {

    val usernameInput = MutableLiveData("")

    val searchForAccountState = MutableLiveData<SearchForAccountState>(
        SearchForAccountState.WaitingForUserInput(
            null
        )
    )

    suspend fun searchForUserAccount() = withContext(Dispatchers.IO) {
        if (searchForAccountState.value !is SearchForAccountState.WaitingForUserInput) return@withContext
        searchForAccountState.postValue(SearchForAccountState.Loading)

        delay(500)

        var searchForAccountResponse: Response<AccountListResponse>? = null
        try {
            searchForAccountResponse = APIClient.getAPIInterface()
                .getAccountListByUsername(username = usernameInput.value ?: "").execute()
        } catch (_: Exception) {
        }

        if (searchForAccountResponse == null || searchForAccountResponse.body() == null && searchForAccountResponse.errorBody() == null) {
            searchForAccountState.postValue(
                SearchForAccountState.WaitingForUserInput(
                    SearchForAccountState.SearchForAccountErrorType.CONNECTION_FAILURE
                )
            )
            return@withContext
        }

        if (searchForAccountResponse.errorBody() != null) {
            searchForAccountState.postValue(
                SearchForAccountState.WaitingForUserInput(
                    SearchForAccountState.SearchForAccountErrorType.UNKNOWN_ERROR
                )
            )
            return@withContext
        }

        searchForAccountResponse.body()?.let {
            println(it)
            searchForAccountState.postValue(SearchForAccountState.WaitingForUserInput(null))
        }
    }

    sealed class SearchForAccountState {
        data class WaitingForUserInput(val error: SearchForAccountErrorType?) :
            SearchForAccountState()

        object Loading : SearchForAccountState()

        enum class SearchForAccountErrorType {
            CONNECTION_FAILURE, UNKNOWN_ERROR
        }
    }

}