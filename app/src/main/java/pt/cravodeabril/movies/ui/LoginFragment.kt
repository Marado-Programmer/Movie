package pt.cravodeabril.movies.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        requireActivity().getSharedPreferences("prefs", 0).apply {
                val username = getString("username", "")
                val password = getString("password", "")

                if (username != "" && password != "") {
                    setLoading(true)

                    binding.username.setText(username)
                    binding.password.setText(password)

                    lifecycleScope.launch {
                        val result =
                            (requireActivity().application as App).container.loginRepository.login(
                                username!!, password!!
                            )

                        setLoading(false)

                        if (result is Resource.Success) {
                            goBackAfterLogin()
                        } else {
                            // show error
                        }
                    }
                }
            }

        binding.loginBtn.setOnClickListener {
            setLoading(true)

            binding.username.text.toString()
            binding.password.text.toString()

            lifecycleScope.launch {
                val result = (requireActivity().application as App).container.loginRepository.login(
                    binding.username.text.toString(), binding.password.text.toString()
                )

                requireActivity().getSharedPreferences("prefs", 0).edit {
                        putString("username", binding.username.text.toString())
                        putString("password", binding.password.text.toString())
                    }

                setLoading(false)

                if (result is Resource.Success) {
                    goBackAfterLogin()
                } else {
                    // show error
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.loadingLayout.isVisible = loading
        binding.loginBtn.isEnabled = !loading
        binding.usernameLayout.isEnabled = !loading
        binding.passwordLayout.isEnabled = !loading

    }

    private fun goBackAfterLogin() {
        findNavController().popBackStack()
    }
}