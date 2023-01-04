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
import xyz.nowaha.chengetawildlife.databinding.FragmentAccountNameBinding

class EditNameAccountFragment : Fragment() {

    private val viewModel: EditNameAccountViewModel by viewModels()

    private var _binding: FragmentAccountNameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            userNameTextInputEditText.setText(viewModel.usernameInput.value)
            userNameTextInputEditText.addTextChangedListener {
                userNameTextInputLayout.error = null
                viewModel.usernameInput.postValue(it.toString())
            }
            firstNameTextInputEditText.setText(viewModel.usernameInput.value)
            firstNameTextInputEditText.addTextChangedListener {
                firstNameTextInputLayout.error = null
                viewModel.firstnameInput.postValue(it.toString())
            }
            lastNameTextInputEditText.setText(viewModel.usernameInput.value)
            lastNameTextInputEditText.addTextChangedListener {
                lastNameTextInputLayout.error = null
                viewModel.lastnameInput.postValue(it.toString())
            }

            backButton.setOnClickListener { findNavController().navigateUp() }
        }

        val editNameAccountButton = view.findViewById<Button>(R.id.submitCredButton)

        editNameAccountButton.setOnClickListener {
            if (viewModel.editNameAccountState.value !is EditNameAccountViewModel.EditNameAccountState.WaitingForUserInput) return@setOnClickListener

            var validAccountDetails = true

            with(binding) {
                userNameTextInputLayout.error = null
                firstNameTextInputLayout.error = null
                lastNameTextInputLayout.error = null

                if (userNameTextInputEditText.text.toString().isBlank()) {
                    userNameTextInputLayout.error = "Please enter a username."
                    validAccountDetails = false
                }
                if (firstNameTextInputEditText.text.toString().isBlank()) {
                    firstNameTextInputLayout.error = "Please enter a firstname."
                    validAccountDetails = false
                }
                if (lastNameTextInputEditText.text.toString().isBlank()) {
                    lastNameTextInputLayout.error = "Please enter a lastname."
                    validAccountDetails = false
                }
            }

            if (!validAccountDetails) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.attemptEditNameAccount()
            }
        }

        viewModel.editNameAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is EditNameAccountViewModel.EditNameAccountState.WaitingForUserInput -> {
                    if (it.error != null) {
                        binding.userNameTextInputLayout.error = when (it.error) {
                            EditNameAccountViewModel.EditNameAccountState.EditNameAccountErrorType.CONNECTION_FAILURE -> "Connection Failed"
                            EditNameAccountViewModel.EditNameAccountState.EditNameAccountErrorType.UNKNOWN_ERROR -> "Something went wrong"
                            EditNameAccountViewModel.EditNameAccountState.EditNameAccountErrorType.USERNAME_NOT_FOUND -> "Account ${viewModel.usernameInput.value} does not exist"
                        }
                    }
                }
                is EditNameAccountViewModel.EditNameAccountState.Loading -> {

                }

                is EditNameAccountViewModel.EditNameAccountState.AccountNameEdited -> {
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
            binding.submitCredButton.isEnabled = !it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}