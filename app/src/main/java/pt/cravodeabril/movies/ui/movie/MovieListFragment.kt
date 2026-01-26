package pt.cravodeabril.movies.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.createCoilImageLoader
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.databinding.FragmentMovieListBinding
import pt.cravodeabril.movies.databinding.ItemMovieListBinding
import pt.cravodeabril.movies.utils.diffCallbackOf

class MovieListFragment : Fragment(R.layout.fragment_movie_list) {
    private val args: MovieListFragmentArgs by navArgs()
    private val adapter = ListAdapter()
    private val viewModel by viewModels<MovieListViewModel>()

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        viewModel.observeMovies(
            args.filter ?: "",
//            genre = args.genreFilter.ifBlank { null },
//            sortBy = args.sortBy.ifBlank { "releaseDate" },
//            sortOrder = args.sortOrder.ifBlank { "desc" },
//            favoritesOnly = args.favoritesOnly
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(requireActivity().application as App).container.loginRepository.isLoggedIn) {
            val action = MovieListFragmentDirections.actionMovieListFragmentToLogInFragment()

            findNavController().navigate(action)
            return
        }

        _binding = FragmentMovieListBinding.bind(view)

        setupToolbar()
        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.loading.visibility = View.VISIBLE
        }

        viewModel.movies.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.loading.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.loading.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.swipeRefresh.isRefreshing = false
                }

                is Resource.Error -> {
                    binding.loading.visibility = View.GONE
                    // showError(state.message)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        if (false && (requireActivity().application as App).container.loginRepository.user?.role !== "admin") {
            binding.create.visibility = View.GONE
            binding.create.isClickable = false
        } else {
            binding.create.setOnClickListener {
                findNavController().navigate(MovieListFragmentDirections.actionMovieListFragmentToMovieCreateFragment())
            }
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
                    (requireActivity().application as App).container.loginRepository.logout()

                    findNavController().navigate(
                        MovieListFragmentDirections.actionMovieListFragmentToLogInFragment(),
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

    inner class ListAdapter :
        androidx.recyclerview.widget.ListAdapter<MovieWithDetails, ListAdapter.ViewHolder>(
            diffCallbackOf(idSelector = { it.movie.id })
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemMovieListBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(binding: ItemMovieListBinding) :
            RecyclerView.ViewHolder(binding.root) {
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

            val movieTitle: TextView = binding.title
            val movieSynopsis: TextView = binding.synopsis
            val moviePicture: ImageView = binding.picture
            val movieFavorite: ImageButton = binding.favorite

            fun bind(movie: MovieWithDetails) {
                this.movie = movie

                movieTitle.text = movie.movie.title
                movieSynopsis.text = movie.movie.synopsis

                // Load poster
                val mainPicture = movie.pictures.firstOrNull { it.mainPicture }
                mainPicture?.let { picture ->
                    val posterUrl = viewModel.moviePictureUrl(movie.movie.id, picture.id)
                    val imageLoader = createCoilImageLoader(binding.root.context)

                    val request = ImageRequest.Builder(binding.root.context).data(posterUrl)
                        .target(moviePicture).crossfade(true).build()

                    imageLoader.enqueue(request)
                } ?: run {
                    moviePicture.setImageResource(R.drawable.movie_border_24px)
                }

                movieFavorite.setOnClickListener {
                    this.movie?.movie?.id?.let { id -> viewModel.toggleFavorite(id) }
                }
            }
        }
    }
}