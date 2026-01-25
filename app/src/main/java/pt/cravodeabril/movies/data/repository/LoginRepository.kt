package pt.cravodeabril.movies.data.repository

import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.dao.UserDao
import pt.cravodeabril.movies.data.local.entity.UserEntity
import pt.cravodeabril.movies.data.remote.LoginDataSource
import kotlin.time.Instant

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, private val userDao: UserDao) {
    var user: UserEntity? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    suspend fun login(username: String, password: String): Resource<UserEntity> {
        return when (val result = dataSource.login(username, password)) {
            is Resource.Success -> {
                val entity = UserEntity(
                    id = result.data.id.toLong(),
                    username = result.data.username,
                    role = result.data.role,
                    dateOfBirth = null,
                    createdAt = Instant.fromEpochSeconds(0),
                    updatedAt = null
                )
                userDao.upsert(entity)
                user = entity
                Resource.Success(entity)
            }
            is Resource.Error -> result
            is Resource.Loading -> result
        }
    }

    private fun setLoggedInUser(loggedInUser: UserEntity) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}