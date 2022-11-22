package xyz.nowaha.chengetawildlife

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAccountFragment : Fragment(R.layout.fragment_account_creation) {

val viewModel: CreateAccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val usernameInputLayout = view.findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val roleInput = view.findViewById<SwitchMaterial>(R.id.roleSelect)


        usernameInput.setText(viewModel.usernameInput.value)
        usernameInput.addTextChangedListener {
            usernameInputLayout.error = null
            viewModel.usernameInput.postValue(it.toString())
        }
        passwordInput.setText(viewModel.usernameInput.value)
        passwordInput.addTextChangedListener {
            passwordInputLayout.error = null
            viewModel.passwordInput.postValue(it.toString())
        }

        roleInput.setText(viewModel.roleInput.value ?: 0)
        roleInput.addTextChangedListener {
            viewModel.roleInput.postValue(viewModel.roleInput.value)
        }

        val createAccountButton = view.findViewById<Button>(R.id.createAccountButton)

        passwordInput.setOnEditorActionListener { _, v, _ ->
            if(v == EditorInfo.IME_ACTION_GO)
            {
                if(!createAccountButton.isEnabled) return@setOnEditorActionListener true
                createAccountButton.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        createAccountButton.setOnClickListener {
            if (viewModel.createAccountState.value !is CreateAccountViewModel.CreateAccountState.WaitingForUserInput) return@setOnClickListener

            var validAccountDetails = true

            if(usernameInput.text.toString().isBlank())
                {
                    usernameInput.error = "Please enter your username."
                    validAccountDetails = false
                }
            if(passwordInput.text.toString().isBlank())
            {
                passwordInput.error = "Please enter your password."
                validAccountDetails = false
            }

            if (!validAccountDetails) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO)
            {
                viewModel.attemptCreateAccount()
                Toast.makeText(requireContext(),"Account $usernameInput Successfully created", Toast.LENGTH_SHORT).show()

            }








        }
    }





}