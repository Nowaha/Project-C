package xyz.nowaha.chengetawildlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.databinding.FragmentLoginBinding
import xyz.nowaha.chengetawildlife.util.SoftInputUtils.hideSoftInput

// Should contain the code for the logging in process.
// After logging in, Session.key should be set to the session key retrieved.
class LoginFragment : Fragment(R.layout.fragment_login) {

    val viewModel: LoginViewModel by viewModels()
    lateinit var loadingCircle: ProgressBar

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInputEditText =
            view.findViewById<TextInputEditText>(R.id.usernameTextInputEditText)
        val usernameInputLayout = view.findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val passwordInputEditText =
            view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)

        loadingCircle = view.findViewById(R.id.loadingCircle)

        usernameInputEditText.setText(viewModel.usernameEntry.value)
        usernameInputEditText.addTextChangedListener {
            usernameInputLayout.error = null
            viewModel.usernameEntry.postValue(it.toString())
        }

        passwordInputEditText.setText(viewModel.passwordEntry.value)
        passwordInputEditText.addTextChangedListener {
            usernameInputLayout.error = null
            passwordInputLayout.error = null
            viewModel.passwordEntry.postValue(it.toString())
        }

        val loginButton = view.findViewById<Button>(R.id.loginButton)

        passwordInputEditText.setOnEditorActionListener { _, v, _ ->
            if (v == EditorInfo.IME_ACTION_GO) {
                if (!loginButton.isEnabled) return@setOnEditorActionListener true
                loginButton.performClick()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        loginButton?.setOnClickListener {
            if (viewModel.loginState.value !is LoginViewModel.LoginState.WaitingForUserInput) return@setOnClickListener

            var valid = true

            if (usernameInputEditText.text.toString().isBlank()) {
                usernameInputLayout.error = "Please enter your username"
                valid = false
            }

            if (passwordInputEditText.text.toString().isBlank()) {
                passwordInputLayout.error = "Please enter your password"
                valid = false
            }

            if (!valid) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.attemptLogin()
            }
        }

        viewModel.loginState.observe(viewLifecycleOwner) {
            when (it) {
                is LoginViewModel.LoginState.WaitingForUserInput -> {
                    loadingCircle.visibility = View.GONE
                    loginButton.textScaleX = 1f
                    if (it.error != null) {
                        usernameInputLayout.error = when (it.error) {
                            LoginViewModel.LoginState.LoginErrorType.CONNECTION_FAILURE -> "Connection failure. Please try again."
                            LoginViewModel.LoginState.LoginErrorType.INVALID_CREDENTIALS -> "Incorrect username or password."
                        }
                    }
                }
                is LoginViewModel.LoginState.Loading -> {
                    loadingCircle.visibility = View.VISIBLE
                    loginButton.textScaleX = 0f
                }
                is LoginViewModel.LoginState.LoggedIn -> {
                    requireActivity().hideSoftInput(passwordInputEditText)
                    findNavController().navigate(R.id.action_loginFragmentNav_to_eventMapFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

