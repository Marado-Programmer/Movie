package pt.cravodeabril.movies.data

import android.content.Context
import android.util.Base64
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BasicAuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        val credentials = "$username:$password"
        val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val newRequest = chain.request().newBuilder()
            .header("Authorization", auth)
            .build()

        return chain.proceed(newRequest)
    }
}

fun createCoilImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .crossfade(true)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient.Builder()
                            .addInterceptor(BasicAuthInterceptor(context))
                            .build()
                    }
                )
            )
        }
        .build()
}
