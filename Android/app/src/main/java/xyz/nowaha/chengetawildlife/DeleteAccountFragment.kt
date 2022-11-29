package xyz.nowaha.chengetawildlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.databinding.FragmentAccountDeleteBinding

class DeleteAccountFragment : Fragment() {
    val viewModel: DeleteAccountViewModel by viewModels()

    private var _binding: FragmentAccountDeleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.usernameDeleteTextInputEditText.setText(viewModel.usernameInput.value)
        binding.usernameDeleteTextInputEditText.addTextChangedListener {
            binding.usernameDeleteTextInputLayout.error = null
            viewModel.usernameInput.postValue(it.toString())
        }

        binding.deleteAccountButton.setOnClickListener {
            if (viewModel.deleteAccountState.value !is DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput) return@setOnClickListener

            binding.usernameDeleteTextInputLayout.error = null

            var validAccountDetails = true
            if (binding.usernameDeleteTextInputEditText.text.toString().isBlank()) {
                binding.usernameDeleteTextInputLayout.error = "Please enter a username."
                validAccountDetails = false
            }
            if (!validAccountDetails) return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteAccount()
            }
        }

        viewModel.deleteAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is DeleteAccountViewModel.DeleteAccountState.WaitingForUserInput -> {
                    if (it.error != null) {
                        binding.usernameDeleteTextInputLayout.error = when (it.error) {
                            DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.CONNECTION_FAILURE -> "Connection Failed"
                            DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.UNKNOWN_ERROR -> "Something went wrong"
                            DeleteAccountViewModel.DeleteAccountState.DeleteAccountErrorType.USERNAME_NOT_FOUND -> "Account ${viewModel.usernameInput.value} doesn't exists"
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
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}