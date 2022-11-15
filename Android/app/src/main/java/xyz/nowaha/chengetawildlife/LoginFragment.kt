package xyz.nowaha.chengetawildlife

import android.R.attr.button
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.extensions.getBoolean
import xyz.nowaha.chengetawildlife.extensions.getString
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.LoginResponse

// Should contain the code for the logging in process.
// After logging in, Session.key should be set to the session key retrieved.
class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val password = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val loginbtn = view.findViewById<Button>(R.id.loginButton)

        loginbtn?.setOnClickListener {
            var valid = true

            // Empty Username
            if (username.text.toString().isBlank()){
                username.error = "Please enter Username"
                valid = false
            }

            // Empty Password
            if (password.text.toString().isBlank()) {
                password.error = "Please enter Password"
                valid = false
            }

            if (!valid){
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                var loginResponse: Response<LoginResponse>? = null
                try {
                    loginResponse = APIClient.getAPIInterface().attemptLogin(username.text.toString(), password.text.toString()).execute()
                } catch (_: Exception) { }

                withContext(Dispatchers.Main) {
                    if (loginResponse?.body() == null && loginResponse?.errorBody() == null) {
                        Toast.makeText(activity, "Could not connect to server. Please try again.", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }
                    if (loginResponse.errorBody() != null) {
                        Toast.makeText(activity, "Username or password is incorrect.", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }

                    Session.key = loginResponse.body()!!.sessionKey
                    findNavController().navigate(R.id.action_loginFragmentNav_to_eventMapFragment)
                }
            }
        }
    }
}

