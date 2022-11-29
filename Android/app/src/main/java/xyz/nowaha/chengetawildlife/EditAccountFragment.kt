package xyz.nowaha.chengetawildlife

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditAccountFragment : Fragment(R.layout.fragment_account_editing) {

    private val viewModel: EditAccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val usernameInputLayout = view.findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val passwordConfirmInput = view.findViewById<TextInputEditText>(R.id.passwordConfirmTextInputEditText)
        val passwordConfirmInputLayout = view.findViewById<TextInputLayout>(R.id.passwordConfirmTextInputLayout)

        usernameInput.setText(viewModel.usernameInput.value)
        usernameInput.addTextChangedListener {
            usernameInputLayout.error = null
            viewModel.usernameInput.postValue(it.toString())
        }
        passwordInput.setText(viewModel.passwordInput.value)
        passwordInput.addTextChangedListener {
            passwordInputLayout.error = null
            passwordConfirmInputLayout.error = null
            viewModel.passwordInput.postValue(it.toString())
        }
        passwordConfirmInput.setText(viewModel.passwordConfirmInput.value)
        passwordConfirmInput.addTextChangedListener {
            passwordInputLayout.error = null
            passwordConfirmInputLayout.error = null
            viewModel.passwordConfirmInput.postValue(it.toString())
        }

        val editAccountButton = view.findViewById<Button>(R.id.editAccountButton)

        editAccountButton.setOnClickListener {
            if (viewModel.editAccountState.value !is EditAccountViewModel.EditAccountState.WaitingForUserInput) return@setOnClickListener

            usernameInputLayout.error = null
            passwordInputLayout.error = null
            passwordConfirmInputLayout.error = null

            var validAccountDetails = true

            if (passwordInput.text.toString().isBlank()) {
                passwordInputLayout.error = "Please enter a password."
                validAccountDetails = false
            }
            if (passwordConfirmInput.text.toString().isBlank()) {
                passwordConfirmInputLayout.error = "Please confirm your password."
                validAccountDetails = false
            }
            if (validAccountDetails && passwordInput.text.toString() != passwordConfirmInput.text.toString()) {
                passwordInputLayout.error = "Passwords must match."
                passwordConfirmInputLayout.error = "Passwords must match."
                validAccountDetails = false
            }
            if (usernameInput.text.toString().isBlank()) {
                usernameInputLayout.error = "Please enter a username."
                validAccountDetails = false
            }

            if (!validAccountDetails) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.attemptEditAccount()
            }
        }

        viewModel.editAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is EditAccountViewModel.EditAccountState.WaitingForUserInput -> {
                    if (it.error != null) {
                        usernameInputLayout.error = when (it.error) {
                            EditAccountViewModel.EditAccountState.EditAccountErrorType.CONNECTION_FAILURE -> "Connection Failed"
                            EditAccountViewModel.EditAccountState.EditAccountErrorType.UNKNOWN_ERROR -> "Something went wrong"
                            EditAccountViewModel.EditAccountState.EditAccountErrorType.USERNAME_NOT_FOUND -> "Account ${viewModel.usernameInput.value} does not exist"
                        }
                    }
                }
                is EditAccountViewModel.EditAccountState.Loading -> {

                }

                is EditAccountViewModel.EditAccountState.AccountEdited -> {
                    Toast.makeText(
                        requireContext(),
                        "Account ${viewModel.usernameInput.value} successfully edited",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}