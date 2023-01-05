package xyz.nowaha.chengetawildlife.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountOverviewButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_accountOverviewFragment)
        }

        binding.createAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_createAccountFragment2)
        }

        binding.editAccountNameButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_editNameAccountFragment)
        }

        binding.editAccountPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_editAccountFragment)
        }

        binding.deleteAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_deleteAccountFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}