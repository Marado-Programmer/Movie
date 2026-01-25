package pt.cravodeabril.movies.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.target
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.createCoilImageLoader
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.databinding.FragmentMovieDetailsBinding

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private val args: MovieDetailsFragmentArgs by navArgs()
    private val viewModel: MovieDetailsViewModel by viewModels()

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieDetailsBinding.bind(view)

        viewModel.observeMovie(args.movieId)

        viewModel.movie.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    bindMovie(state.data)
                }
                is Resource.Error -> {
                    // showError(state.message)
                }
            }
        }

//        viewModel.ratings.observe(viewLifecycleOwner) { ratings ->
//            binding.ratingsRecycler.adapter?.notifyDataSetChanged()
//            // or adapter.submitList(ratings)
//            binding.noRatingsText.visibility = if (ratings.isEmpty()) View.VISIBLE else View.GONE
//        }
//
//        viewModel.ratingsLoading.observe(viewLifecycleOwner) { loading ->
//            binding.ratingsProgress.visibility = if (loading) View.VISIBLE else View.GONE
//        }

        binding.favoriteButton.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.rateButton.setOnClickListener {
        }

        // Edit (admin only)
        binding.editButton.setOnClickListener {
        }
    }

    private fun bindMovie(movie: MovieWithDetails?) {
        movie ?: return

        binding.movieTitle.text = movie.movie.title
        binding.movieSynopsis.text = movie.movie.synopsis
        binding.movieDirector.text = getString(R.string.movie_director, movie.movie.directorId ?: "Unknown")
        binding.movieGenres.text = movie.genres.joinToString(", ")
        binding.movieAge.text = getString(R.string.movie_age, movie.movie.minimumAge)
        val mainPicture = movie.pictures.firstOrNull { it.mainPicture }
        mainPicture?.let {
            val posterUrl = "http://10.0.2.2:8080/movies/${movie.movie.id}/pictures/${it.id}"
            val imageLoader = createCoilImageLoader(binding.root.context)
            val request = ImageRequest.Builder(binding.root.context)
                .data(posterUrl)
                .target(binding.moviePoster)
                .crossfade(true)
                .build()
            imageLoader.enqueue(request)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
