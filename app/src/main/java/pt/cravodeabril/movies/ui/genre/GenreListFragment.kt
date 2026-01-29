package pt.cravodeabril.movies.ui.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.databinding.FragmentGenreListBinding
import pt.cravodeabril.movies.databinding.ItemGenreListBinding
import pt.cravodeabril.movies.utils.diffCallbackOf

class GenreListFragment : Fragment(R.layout.fragment_genre_list) {
    private val adapter = ListAdapter()
    private val viewModel by viewModels<GenreListViewModel>()

    private var _binding: FragmentGenreListBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        viewModel.observeGenres()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGenreListBinding.bind(view)

//        binding.search.apply {
//            show()
//            addTransitionListener { _, previousState, newState ->
//                if ((previousState == SearchView.TransitionState.SHOWN && newState == SearchView.TransitionState.HIDING) || newState == SearchView.TransitionState.HIDDEN) {
//                    findNavController().popBackStack()
//                }
//            }
//            editText.addTextChangedListener { editable -> viewModel.observeGenres(editable.toString()) }
//        }

        binding.list.adapter = adapter

        viewModel.genres.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.loading.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.loading.visibility = View.GONE
                    adapter.submitList(state.data)
                }

                is Resource.Error -> {
                    binding.loading.visibility = View.GONE
                }
            }
        }

        binding.create.setOnClickListener {
            findNavController().navigate(GenreListFragmentDirections.actionGenreListUpsertFragmentToGenreCreateFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ListAdapter :
        androidx.recyclerview.widget.ListAdapter<GenreEntity, ListAdapter.ViewHolder>(
            diffCallbackOf(idSelector = { it.id })
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemGenreListBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(binding: ItemGenreListBinding) :
            RecyclerView.ViewHolder(binding.root) {
            private var genre: GenreEntity? = null

            val name = binding.name
            val edit = binding.edit
            val delete = binding.delete

            fun bind(genre: GenreEntity) {
                this.genre = genre

                name.text = this.genre!!.name

                edit.setOnClickListener {
                    findNavController().navigate(
                        GenreListFragmentDirections.actionGenreListUpsertFragmentToGenreEditFragment(
                            this.genre!!.id
                        )
                    )
                }

                delete.setOnClickListener {
                    AlertDialog.Builder(requireContext()).setTitle(R.string.delete)
                        .setMessage(R.string.confirm_deletion)
                        .setIcon(R.drawable.delete_forever_24px).setPositiveButton(
                            R.string.positive
                        ) { _, _ -> viewModel.deleteGenre(this.genre!!.id) }
                        .setNegativeButton(R.string.negative, null).setCancelable(false).create()
                        .show()
                }
            }
        }
    }
}