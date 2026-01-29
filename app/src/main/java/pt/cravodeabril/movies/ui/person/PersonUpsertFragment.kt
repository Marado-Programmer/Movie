package pt.cravodeabril.movies.ui.person

import android.icu.text.DateFormat
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import pt.cravodeabril.movies.databinding.FragmentGenreUpsertBinding
import pt.cravodeabril.movies.databinding.FragmentMovieUpsertBinding
import pt.cravodeabril.movies.databinding.FragmentPersonUpsertBinding
import pt.cravodeabril.movies.ui.genre.GenreUpsertFragmentArgs
import pt.cravodeabril.movies.ui.genre.GenreUpsertFragmentDirections
import pt.cravodeabril.movies.ui.genre.GenreUpsertViewModel
import pt.cravodeabril.movies.ui.movie.MovieFormState
import pt.cravodeabril.movies.ui.movie.MovieUpsertFragmentArgs
import pt.cravodeabril.movies.ui.movie.MovieUpsertFragmentDirections
import pt.cravodeabril.movies.ui.movie.MovieUpsertViewModel
import pt.cravodeabril.movies.ui.movie.MovieUpsertViewModelFactory
import pt.cravodeabril.movies.utils.FormState
import java.util.Calendar

class PersonUpsertFragment : Fragment(R.layout.fragment_person_upsert) {
    private lateinit var datePickerDialog: MaterialStyledDatePickerDialog
    private lateinit var getContent: ActivityResultLauncher<String>
    private val args: PersonUpsertFragmentArgs by navArgs()
    private val viewModel: PersonUpsertViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return PersonUpsertViewModel(
                    requireActivity().application,
                    if (args.id == -1L) null else args.id
                ) as T
            }
        }
    }

    private var _binding: FragmentPersonUpsertBinding? = null
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
            val action = PersonUpsertFragmentDirections.actionPersonUpsertFragmentToLogInFragment()
            findNavController().navigate(action)
            return
        }

        _binding = FragmentPersonUpsertBinding.bind(view)

        bindForm()
        bindActions()
        observeState()
    }

    private fun bindForm() {
        viewModel.name.observe(viewLifecycleOwner) {
            if (binding.nameInput.text.toString() != it) binding.nameInput.setText(it)
        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        datePickerDialog = MaterialStyledDatePickerDialog(
            requireContext(),
            { _, year, month, day -> binding.dateOfBirthInput.setText("${year}-${if (month < 9) "0" else ""}${month + 1}-${if (day < 10) "0" else ""}${day}") },
            year,
            month,
            day
        )

        viewModel.dateOfBirth.observe(viewLifecycleOwner) {
            if (LocalDate.parse(binding.dateOfBirthInput.text.toString()) != it) {
                binding.dateOfBirthInput.setText(
                    it.format(LocalDate.Formats.ISO)
                )
            }
        }
    }

    private fun bindActions() {
        binding.image.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.dateOfBirthInput.setOnClickListener { _ -> datePickerDialog.show() }

        binding.create.setOnClickListener {
            viewModel.name.postValue(binding.nameInput.text.toString())
            viewModel.dateOfBirth.postValue(
                LocalDate.Formats.ISO.parse(binding.dateOfBirthInput.text.toString())
            )

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
        binding.dateOfBirthLayout.isEnabled = !loading
    }
}