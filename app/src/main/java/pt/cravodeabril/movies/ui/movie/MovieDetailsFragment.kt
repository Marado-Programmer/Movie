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
import pt.cravodeabril.movies.data.createCoilImageLoader
import pt.cravodeabril.movies.data.repository.MovieDetailsManual
import pt.cravodeabril.movies.databinding.FragmentMovieDetailsBinding

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private val args: MovieDetailsFragmentArgs by navArgs()
    private val viewModel: MovieDetailsViewModel by viewModels()

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieDetailsBinding.bind(view)

        viewModel.loadMovieById(args.movieId)

        // Observe movie details data
        viewModel.movie.observe(viewLifecycleOwner) { movie ->
            bindMovie(movie)
        }

        // Favorite button
        binding.favoriteButton.setOnClickListener {
            viewModel.toggleFavorite(args.movieId)
        }

        // Rate button
        binding.rateButton.setOnClickListener {
            // open a rating dialog
        }

        // Edit (admin only)
        binding.editButton.setOnClickListener {
            // navigate to EditMovieFragment
        }
    }

    private fun bindMovie(movieWithPictures: MovieDetailsManual?) {
        movieWithPictures ?: return
        val movie = movieWithPictures.movie

        binding.movieTitle.text = movie.title
        binding.movieSynopsis.text = movie.synopsis
        binding.movieDirector.text = getString(R.string.movie_director, movie.director ?: "Unknown")
        binding.movieGenres.text = movieWithPictures.genres.joinToString(", ")
        binding.movieAge.text = getString(R.string.movie_age, movie.minimumAge)
        // Use Coil to load the image
        val mainPicture = movieWithPictures.pictures.firstOrNull { it.mainPicture }
        mainPicture?.let {
            val posterUrl = "http://10.0.2.2:8080/movies/${movie.id}/pictures/${it.id}"
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
