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
import xyz.nowaha.chengetawildlife.databinding.FragmentAccountCreationBinding

class CreateAccountFragment : Fragment() {

    private val viewModel: CreateAccountViewModel by viewModels()

    private var _binding: FragmentAccountCreationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountCreationBinding.inflate(inflater, container, false)
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

            createFirstNameTextInputEditText.setText(viewModel.firstnameInput.value)
            createFirstNameTextInputEditText.addTextChangedListener {
                createFirstNameTextInputLayout.error = null
                viewModel.firstnameInput.postValue(it.toString())
            }

            createSurnameTextInputEditText.setText(viewModel.lastnameInput.value)
            createSurnameTextInputEditText.addTextChangedListener {
                createSurnameTextInputLayout.error = null
                viewModel.lastnameInput.postValue(it.toString())
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

            roleSelect.isChecked = viewModel.roleInput.value == 1
            roleSelect.setOnCheckedChangeListener { _, isChecked ->
                viewModel.roleInput.postValue(if (isChecked) 1 else 0)
            }

            backButton.setOnClickListener { findNavController().navigateUp() }
        }

        val createAccountButton = view.findViewById<Button>(R.id.createAccountButton)

        createAccountButton.setOnClickListener {
            if (viewModel.createAccountState.value !is CreateAccountViewModel.CreateAccountState.WaitingForUserInput) return@setOnClickListener

            var validAccountDetails = true

            with(binding) {
                usernameTextInputLayout.error = null
                passwordTextInputLayout.error = null
                passwordConfirmTextInputLayout.error = null
                createFirstNameTextInputLayout.error = null
                createSurnameTextInputLayout.error = null

                if (passwordTextInputEditText.text.toString().isBlank()) {
                    passwordTextInputLayout.error = "Please enter a password."
                    validAccountDetails = false
                }
                if (passwordConfirmTextInputEditText.text.toString().isBlank()) {
                    passwordConfirmTextInputLayout.error = "Please confirm your password."
                    validAccountDetails = false
                }
                if (createFirstNameTextInputEditText.text.toString().isBlank()) {
                    createFirstNameTextInputLayout.error = "Enter a first name."
                    validAccountDetails = false
                }
                if (createSurnameTextInputEditText.text.toString().isBlank()) {
                    createSurnameTextInputLayout.error = "Enter a surname."
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
                viewModel.attemptCreateAccount()
            }
        }

        viewModel.createAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is CreateAccountViewModel.CreateAccountState.WaitingForUserInput -> {
                    if (it.error != null) {
                        binding.usernameTextInputLayout.error = when (it.error) {
                            CreateAccountViewModel.CreateAccountState.CreateAccountErrorType.CONNECTION_FAILURE -> "Connection Failed"
                            CreateAccountViewModel.CreateAccountState.CreateAccountErrorType.UNKNOWN_ERROR -> "Something went wrong"
                            CreateAccountViewModel.CreateAccountState.CreateAccountErrorType.USERNAME_IN_USE -> "Account ${viewModel.usernameInput.value} already exists"
                        }
                    }
                }
                is CreateAccountViewModel.CreateAccountState.Loading -> {

                }

                is CreateAccountViewModel.CreateAccountState.AccountCreated -> {
                    Toast.makeText(
                        requireContext(),
                        "Account ${viewModel.usernameInput.value} successfully created",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }
        }

        MainActivity.offlineMode.observe(viewLifecycleOwner) {
            binding.createAccountButton.isEnabled = !it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}