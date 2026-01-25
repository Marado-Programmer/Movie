package pt.cravodeabril.movies.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.databinding.FragmentMovieUpsertBinding

class MovieUpsertFragment : Fragment(R.layout.fragment_movie_upsert) {

    private val args: MovieUpsertFragmentArgs by navArgs()
    private val viewModel: MovieUpsertViewModel by viewModels {
        MovieUpsertViewModelFactory(
            requireActivity().application, args.movieId
        )
    }

    private var _binding: FragmentMovieUpsertBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(requireActivity().application as App).container.loginRepository.isLoggedIn) {
            val action =
                MovieUpsertFragmentDirections
                    .actionMovieUpsertFragmentToLogInFragment(
                        returnDestination = R.id.movieUpsertFragment
                    )

            findNavController().navigate(action)
            return
        }

        _binding = FragmentMovieUpsertBinding.bind(view)

        bindForm()
        bindActions()
        observeState()
    }

    private fun bindForm() {
        viewModel.title.observe(viewLifecycleOwner) {
            if (binding.titleInput.text.toString() != it)
                binding.titleInput.setText(it)
        }

        viewModel.synopsis.observe(viewLifecycleOwner) {
            if (binding.synopsisInput.text.toString() != it)
                binding.synopsisInput.setText(it)
        }

//        viewModel.minimumAge.observe(viewLifecycleOwner) {
//            binding.minimumAgeInput.setText(it.toString())
//        }

        // releaseDate, genres, director omitted for brevity
    }

    private fun bindActions() {
        binding.saveBtn.setOnClickListener {
            viewModel.save()
        }

        binding.deleteBtn.apply {
            visibility = if (viewModel.isEditMode) View.VISIBLE else View.GONE
            setOnClickListener { viewModel.delete() }
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                MovieFormState.Loading -> {
                    // binding.progress.visibility = View.VISIBLE
                }

                MovieFormState.Idle -> {
                    // binding.progress.visibility = View.GONE
                }

                MovieFormState.Saved,
                MovieFormState.Deleted -> {
                    findNavController().popBackStack()
                }

                is MovieFormState.Error -> {
                    // binding.progress.visibility = View.GONE
                    // Snackbar.make(binding.root, state.err.title, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
