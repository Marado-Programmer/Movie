package pt.cravodeabril.movies.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        _binding = FragmentLoginBinding.bind(view)

        binding.loginBtn.setOnClickListener {
            setLoading(true)

            binding.username.text.toString()
            binding.password.text.toString()


            lifecycleScope.launch {


            }

        }


    }

    private fun setLoading(loading: Boolean) {
        binding.loadingLayout.isVisible = loading
        binding.loginBtn.isEnabled = !loading
        binding.usernameLayout.isEnabled = !loading
        binding.passwordLayout.isEnabled = !loading

    }

    private fun goToMain() {
        findNavController()
            .navigate(
                LoginFragmentDirections.actionLoginFragmentToMovieFragment(),
                NavOptions.Builder().apply {
                    this.setPopUpTo(R.id.loginFragment, true)
                }.build()
            )
    }


}