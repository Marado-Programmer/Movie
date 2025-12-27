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

        viewModel.loadMovieDetails(args.movieId)
        viewModel.loadRatings(args.movieId)

        viewModel.movieDetails.observe(viewLifecycleOwner) { movie ->
            bindMovie(movie)
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
            viewModel.toggleFavorite(args.movieId)
        }

        binding.rateButton.setOnClickListener {
        }

        // Edit (admin only)
        binding.editButton.setOnClickListener {
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
