package xyz.nowaha.chengetawildlife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.databinding.FragmentAccountOverviewBinding

class AccountOverviewFragment : Fragment() {

    private val viewModel: AccountOverviewViewModel by viewModels()

    private var _binding: FragmentAccountOverviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usernameTextInputEditText.setText(viewModel.usernameInput.value ?: "")
        binding.usernameTextInputEditText.addTextChangedListener {
            viewModel.usernameInput.postValue(it.toString())
            binding.usernameTextInputLayout.isErrorEnabled = false
            binding.usernameTextInputLayout.error = null
        }

        binding.usernameTextInputLayout.setEndIconOnClickListener { makeSearchRequest() }

        binding.usernameTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (viewModel.searchForAccountState.value !is AccountOverviewViewModel.SearchForAccountState.WaitingForUserInput) return@setOnEditorActionListener true
                makeSearchRequest()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        viewModel.searchForAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is AccountOverviewViewModel.SearchForAccountState.WaitingForUserInput -> {
                    binding.searchLoadingCircle.visibility = View.GONE
                    if (it.error != null) {
                        binding.usernameTextInputLayout.isErrorEnabled = true
                        binding.usernameTextInputLayout.error = when (it.error) {
                            AccountOverviewViewModel.SearchForAccountState.SearchForAccountErrorType.CONNECTION_FAILURE -> "Failed to connect."
                            AccountOverviewViewModel.SearchForAccountState.SearchForAccountErrorType.UNKNOWN_ERROR -> "Unknown error."
                        }
                    } else {
                        binding.usernameTextInputLayout.isErrorEnabled = false
                        binding.usernameTextInputLayout.error = null
                    }
                }
                AccountOverviewViewModel.SearchForAccountState.Loading -> {
                    binding.searchLoadingCircle.visibility = View.VISIBLE
                }
            }
        }
    }

    fun makeSearchRequest() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.searchForUserAccount()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}