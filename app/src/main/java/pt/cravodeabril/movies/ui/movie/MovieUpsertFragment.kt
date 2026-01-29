package pt.cravodeabril.movies.ui.movie

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil3.load
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.databinding.FragmentMovieUpsertBinding
import pt.cravodeabril.movies.ui.person.PersonUpsertViewModel
import pt.cravodeabril.movies.utils.FormState
import java.util.Calendar

class MovieUpsertFragment : Fragment(R.layout.fragment_movie_upsert) {

    private lateinit var datePickerDialog: MaterialStyledDatePickerDialog
    private lateinit var getContent: ActivityResultLauncher<String>
    private val args: MovieUpsertFragmentArgs by navArgs()
    private val viewModel: MovieUpsertViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return MovieUpsertViewModel(
                    requireActivity().application,
                    if (args.movieId == -1L) null else args.movieId
                ) as T
            }
        }
    }

    private var _binding: FragmentMovieUpsertBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            binding.image.load(uri)
            viewModel.picture.postValue(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(requireActivity().application as App).container.loginRepository.isLoggedIn) {
            val action = MovieUpsertFragmentDirections.actionMovieUpsertFragmentToLogInFragment()

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
            if (binding.titleInput.text.toString() != it) binding.titleInput.setText(it)
        }

        viewModel.synopsis.observe(viewLifecycleOwner) {
            if (binding.synopsisInput.text.toString() != it) binding.synopsisInput.setText(it)
        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        datePickerDialog = MaterialStyledDatePickerDialog(
            requireContext(),
            { _, year, month, day -> binding.releaseDateInput.setText("${year}-${if (month < 9) "0" else ""}${month + 1}-${if (day < 10) "0" else ""}${day}") },
            year,
            month,
            day
        )

        viewModel.releaseDate.observe(viewLifecycleOwner) {
            if (LocalDate.parse(binding.releaseDateInput.text.toString()) != it) {
                binding.releaseDateInput.setText(
                    it.format(LocalDate.Formats.ISO)
                )
            }
        }

        viewModel.minimumAge.observe(viewLifecycleOwner) {
            if (binding.ageInput.text.toString()
                    .toIntOrNull() != it
            ) binding.ageInput.setText(it.toString())
        }

        viewModel.genres.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.loading.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.loading.visibility = View.GONE

                    state.data.forEach { genre ->
                        binding.genresChipGroup.addView(Chip(requireContext()).apply {
                            text = genre.name
                            isCheckable = true
                            isClickable = true
                            setOnCheckedChangeListener { _, isChecked ->
                                viewModel.checkGenre(
                                    genre.id,
                                    isChecked
                                )
                            }

                        })
                    }
                }

                is Resource.Error -> {
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    private fun bindActions() {
        binding.image.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.releaseDateInput.setOnClickListener { _ -> datePickerDialog.show() }

        binding.create.setOnClickListener {
            viewModel.title.postValue(binding.titleInput.text.toString())
            viewModel.synopsis.postValue(binding.synopsisInput.text.toString())
            viewModel.releaseDate.postValue(
                LocalDate.Formats.ISO.parse(binding.releaseDateInput.text.toString())
            )
            viewModel.minimumAge.postValue(binding.ageInput.text.toString().toInt())

            viewModel.save()
        }

        binding.manageGenres.setOnClickListener {
            findNavController().navigate(MovieUpsertFragmentDirections.manageGenres())
        }
        binding.managePersons.setOnClickListener {
            findNavController().navigate(MovieUpsertFragmentDirections.managePersons())
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
        binding.titleLayout.isEnabled = !loading
        binding.synopsisLayout.isEnabled = !loading
    }
}