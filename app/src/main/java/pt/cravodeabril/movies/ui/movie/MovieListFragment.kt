package pt.cravodeabril.movies.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.target
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.createCoilImageLoader
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.databinding.FragmentMovieListBinding
import pt.cravodeabril.movies.databinding.ItemMovieBinding
import pt.cravodeabril.movies.utils.diffCallbackOf

/**
 * A fragment representing a list of Items.
 */
class MovieListFragment : Fragment(R.layout.fragment_movie_list) {
    private val args: MovieListFragmentArgs by navArgs()
    private val adapter = ListAdapter()
    private val viewModel by viewModels<MovieListViewModel>()

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        viewModel.observeMovies(
            args.titleFilter.ifBlank { "" },
//            genre = args.genreFilter.ifBlank { null },
//            sortBy = args.sortBy.ifBlank { "releaseDate" },
//            sortOrder = args.sortOrder.ifBlank { "desc" },
//            favoritesOnly = args.favoritesOnly
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieListBinding.bind(view)

        setupToolbar()
        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.loading.visibility = View.VISIBLE
        }

        viewModel.movies.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiResult.Loading -> binding.loading.visibility = View.VISIBLE
                is ApiResult.Success -> {
                    binding.loading.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.swipeRefresh.isRefreshing = false
                }

                is ApiResult.Failure -> {
                    binding.loading.visibility = View.GONE
                    // showError(state.message)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        binding.create.setOnClickListener {
            // TODO: CREATE
            // findNavController().navigate(MovieListFragmentDirections.actionMovieListFragmentToLoginFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    findNavController().navigate(
                        MovieListFragmentDirections.actionMovieListFragmentToLoginFragment(),
                        NavOptions.Builder().apply {
                            this.setPopUpTo(R.id.movieListFragment, true)
                        }.build()
                    )
                    true
                }

                else -> false
            }
        }
    }

    /**
     * [androidx.recyclerview.widget.RecyclerView.Adapter] that can display a [pt.cravodeabril.movies.placeholder.PlaceholderContent.PlaceholderItem].
     * TODO: Replace the implementation with code for your data type.
     */
    inner class ListAdapter :
        androidx.recyclerview.widget.ListAdapter<MovieWithDetails, ListAdapter.ViewHolder>(
            diffCallbackOf(idSelector = { it.movie.id })
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemMovieBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
            private var movie: MovieWithDetails? = null

            init {
                binding.root.setOnClickListener {
                    movie?.let {
                        findNavController().navigate(
                            MovieListFragmentDirections.actionMovieListFragmentToMovieDetailsFragment(
                                it.movie.id
                            )
                        )
                    }
                }
            }

            val movieTitle: TextView = binding.movieSynopsis
            val movieSynopsis: TextView = binding.movieSynopsis
            val moviePoster: ImageView = binding.moviePoster
            val movieAge: TextView = binding.movieAge

            fun bind(movie: MovieWithDetails) {
                this.movie = movie

                movieTitle.text = movie.movie.title
                movieSynopsis.text = movie.movie.synopsis
                movieAge.text = getString(R.string.movie_age, movie.movie.minimumAge)

                // Load poster
                val mainPicture = movie.pictures.firstOrNull { it.mainPicture }
                mainPicture?.let { picture ->
                    val posterUrl =
                        "http://10.0.2.2:8080/movies/${movie.movie.id}/pictures/${picture.id}"
                    val imageLoader = createCoilImageLoader(binding.root.context)

                    val request = ImageRequest.Builder(binding.root.context).data(posterUrl)
                        .target(moviePoster).crossfade(true).build()

                    imageLoader.enqueue(request)
                } ?: run {
                    moviePoster.setImageResource(R.drawable.movie_border_24px)
                }
            }
        }
    }
}