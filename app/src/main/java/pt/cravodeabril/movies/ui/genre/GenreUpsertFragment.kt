package pt.cravodeabril.movies.ui.genre

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.databinding.FragmentGenreUpsertBinding
import pt.cravodeabril.movies.ui.movie.FormState
import pt.cravodeabril.movies.utils.FormState

class GenreUpsertFragment : Fragment(R.layout.fragment_genre_upsert) {

    private val args: GenreUpsertFragmentArgs by navArgs()
    private val viewModel: GenreUpsertViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return GenreUpsertViewModel(
                    requireActivity().application,
                    if (args.id == -1L) null else args.id
                ) as T
            }
        }
    }

    private var _binding: FragmentGenreUpsertBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(requireActivity().application as App).container.loginRepository.isLoggedIn) {
            val action = GenreUpsertFragmentDirections.actionGenreUpsertFragmentToLogInFragment()
            findNavController().navigate(action)
            return
        }

        _binding = FragmentGenreUpsertBinding.bind(view)

        bindForm()
        bindActions()
        observeState()
    }

    private fun bindForm() {
        viewModel.name.observe(viewLifecycleOwner) {
            if (binding.nameInput.text.toString() != it) binding.nameInput.setText(it)
        }

        viewModel.description.observe(viewLifecycleOwner) {
            if (binding.descriptionInput.text.toString() != it) binding.descriptionInput.setText(it)
        }
    }

    private fun bindActions() {
        binding.create.setOnClickListener {
            viewModel.name.postValue(binding.nameInput.text.toString())
            viewModel.description.postValue(binding.descriptionInput.text.toString())

            viewModel.save()
        }

        if (viewModel.isEditMode) {
            binding.create.setText(R.string.edit)
            binding.delete.setOnClickListener {
                AlertDialog.Builder(requireContext()).setTitle(R.string.delete)
                    .setMessage(R.string.confirm_deletion)
                    .setIcon(R.drawable.delete_forever_24px).setPositiveButton(
                        R.string.positive
                    ) { _, _ -> {
                        viewModel.delete()
                        findNavController().popBackStack()
                    } }
                    .setNegativeButton(R.string.negative, null).setCancelable(false).create()
                    .show()
            }
        } else {
            binding.delete.visibility = View.GONE
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                FormState.Loading -> {
                    setLoading(true)
                }

                FormState.Idle -> {
                    setLoading(false)
                }

                FormState.Saved, FormState.Deleted -> {
                    setLoading(false)
                    findNavController().popBackStack()
                }

                is FormState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), state.err?.title, Toast.LENGTH_LONG)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLoading(loading: Boolean) {
        binding.loading.isVisible = loading
        binding.create.isEnabled = !loading
        binding.nameLayout.isEnabled = !loading
        binding.descriptionLayout.isEnabled = !loading
    }
}