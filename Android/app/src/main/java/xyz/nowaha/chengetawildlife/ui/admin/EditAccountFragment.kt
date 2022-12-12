package xyz.nowaha.chengetawildlife.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.MainActivity
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.databinding.FragmentAccountEditingBinding

class EditAccountFragment : Fragment() {

    private val viewModel: EditAccountViewModel by viewModels()

    private var _binding: FragmentAccountEditingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountEditingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            usernameTextInputEditText.setText(viewModel.usernameInput.value)
            usernameTextInputEditText.addTextChangedListener {
                usernameTextInputLayout.error = null
                viewModel.usernameInput.postValue(it.toString())
            }
            passwordTextInputEditText.setText(viewModel.passwordInput.value)
            passwordTextInputEditText.addTextChangedListener {
                passwordTextInputLayout.error = null
                passwordConfirmTextInputLayout.error = null
                viewModel.passwordInput.postValue(it.toString())
            }
            passwordConfirmTextInputEditText.setText(viewModel.passwordConfirmInput.value)
            passwordConfirmTextInputEditText.addTextChangedListener {
                passwordTextInputLayout.error = null
                passwordConfirmTextInputLayout.error = null
                viewModel.passwordConfirmInput.postValue(it.toString())
            }

            backButton.setOnClickListener { findNavController().navigateUp() }
        }

        val editAccountButton = view.findViewById<Button>(R.id.editAccountButton)

        editAccountButton.setOnClickListener {
            if (viewModel.editAccountState.value !is EditAccountViewModel.EditAccountState.WaitingForUserInput) return@setOnClickListener

            var validAccountDetails = true

            with(binding) {
                usernameTextInputLayout.error = null
                passwordTextInputLayout.error = null
                passwordConfirmTextInputLayout.error = null

                if (passwordTextInputEditText.text.toString().isBlank()) {
                    passwordTextInputLayout.error = "Please enter a password."
                    validAccountDetails = false
                }
                if (passwordConfirmTextInputEditText.text.toString().isBlank()) {
                    passwordConfirmTextInputLayout.error = "Please confirm your password."
                    validAccountDetails = false
                }
                if (validAccountDetails && passwordTextInputEditText.text.toString() != passwordConfirmTextInputEditText.text.toString()) {
                    passwordTextInputLayout.error = "Passwords must match."
                    passwordConfirmTextInputLayout.error = "Passwords must match."
                    validAccountDetails = false
                }
                if (usernameTextInputEditText.text.toString().isBlank()) {
                    usernameTextInputLayout.error = "Please enter a username."
                    validAccountDetails = false
                }
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
                        binding.usernameTextInputLayout.error = when (it.error) {
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
                    findNavController().navigateUp()
                }
            }
        }

        MainActivity.offlineMode.observe(viewLifecycleOwner) {
            binding.editAccountButton.isEnabled = !it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}