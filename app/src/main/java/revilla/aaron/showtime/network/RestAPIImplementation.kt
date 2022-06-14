package revilla.aaron.showtime.network

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import revilla.aaron.showtime.BuildConfig
import revilla.aaron.showtime.models.GetImagesResponse
import revilla.aaron.showtime.utils.Constants
import java.util.concurrent.TimeUnit

class RestAPIImplementation: RestAPI {

    val api: ShowtimeServiceInterface

    init {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.SHOWTIME_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ShowtimeServiceInterface::class.java)
    }

    override fun getCardsImages(): Call<GetImagesResponse> {
        val headers = HashMap<String, String>()
        headers.put(Constants.VERIFICATION_HEADER, BuildConfig.VERIFICATION_CODE)
        headers.put(Constants.AUTHORIZATION_HEADER, "Bearer ${BuildConfig.AUTH_TOKEN}")
        return api.getCardsImages(headers)
    }
}