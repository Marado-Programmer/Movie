package pt.cravodeabril.movies.data.remote

import kotlinx.serialization.Serializable
import pt.cravodeabril.movies.data.Resource

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return when (val result = MovieServiceClient.login(username, password)) {
            is Resource.Success -> Resource.Success(result.data)
            else -> result
        }
    }

    fun logout() {
        MovieServiceClient.setCredentials("", "")
    }
}

@Serializable
data class LoginResponse(val id: Int, val username: String, val role: String)