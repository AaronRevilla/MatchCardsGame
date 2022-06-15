package revilla.aaron.showtime.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import revilla.aaron.showtime.models.GetImagesResponse
import revilla.aaron.showtime.utils.Constants

interface ShowtimeServiceInterface {

    /*
    * GET Function to load the images from the showtime DS
    * */
    @GET(Constants.GET_IMAGES_ENDPOINT)
    fun getCardsImages(@HeaderMap headers: Map<String, String>) : Call<GetImagesResponse>
}