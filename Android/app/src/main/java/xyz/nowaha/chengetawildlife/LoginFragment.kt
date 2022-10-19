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
import xyz.nowaha.chengetawildlife.extensions.getBoolean
import xyz.nowaha.chengetawildlife.extensions.getString

// Should contain the code for the logging in process.
// After logging in, Session.key should be set to the session key retrieved.
class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val password = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val loginbtn = view.findViewById<Button>(R.id.loginButton)


        loginbtn?.setOnClickListener {
            // TODO: Compare input to databade

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
            if (! valid){
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                var jsonData = ApiAccessor.attemptLogin(username.text.toString(), password.text.toString())
                withContext(Dispatchers.Main){
                    if (jsonData == null) {
                        Toast.makeText(activity, "Please try again later!", Toast.LENGTH_SHORT).show()
                    }
                    //Correct Password and Username
                    else if(jsonData.getBoolean("success", false)) {
                        var sessionKey = jsonData.getString("sessionKey")
                        Session.key = sessionKey
                        findNavController().navigate(R.id.action_loginFragmentNav_to_testTableFragment)
                    }
                    else{ // Incorrect Password or Username
                        Toast.makeText(activity, "Username or Password is incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

