package xyz.nowaha.chengetawildlife

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateAccountFragment : Fragment(R.layout.fragment_account_creation) {

val viewModel: CreateAccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val userNameInput = view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val userNameInputLayout = view.findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)

    }





}