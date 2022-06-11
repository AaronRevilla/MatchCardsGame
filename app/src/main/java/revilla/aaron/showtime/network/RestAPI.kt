package revilla.aaron.showtime.network

import retrofit2.Call
import revilla.aaron.showtime.models.GetImagesResponse

interface RestAPI {

    fun getCardsImages(): Call<GetImagesResponse>

}