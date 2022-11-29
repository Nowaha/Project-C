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

class DeleteAccountFragment : Fragment() {
    private val viewModel: DeleteAccountViewModel by viewModels();

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.usernameDeleteTextInputEditText)
        val usernameInputLayout = view.findViewById<TextInputLayout>(R.id.usernameDeleteTextInputLayout)

        usernameInput.setText(viewModel.usernameInput.value)
        usernameInput.addTextChangedListener {
            usernameInputLayout.error = null
            viewModel.usernameInput.postValue(it.toString())
        }
        val deleteAccountButton = view.findViewById<Button>(R.id.delete_account_Button)
        deleteAccountButton.setOnClickListener {
            if (viewModel.deleteAccountState.value !is DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput) return@setOnClickListener
            usernameInputLayout.error = null
            var validAccountDetails = true
            if (usernameInput.text.toString().isBlank()) {
                usernameInputLayout.error = "Please enter a username."
                validAccountDetails = false
            }
            if (!validAccountDetails) return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteAccount()
            }
            viewModel.deleteAccountState.observe(viewLifecycleOwner) {
                when (it) {
                    is DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput -> {
                        if (it.error != null) {
                            usernameInputLayout.error = when (it.error) {
                                DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.CONNECTION_FAILURE -> "Connection Failed"
                                DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.UNKNOWN_ERROR -> "Something went wrong"
                                DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.USERNAME_IN_USE -> "Account ${viewModel.usernameInput.value} doesn't exists"
                            }
                        }
                    }
                    is DeleteAccountViewModel.DeleteAccountState.Loading -> {

                    }

                    is DeleteAccountViewModel.DeleteAccountState.AccountDeleted -> {
                        Toast.makeText(
                            requireContext(),
                            "Account ${viewModel.usernameInput.value} successfully deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }
}