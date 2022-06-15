package revilla.aaron.showtime.network

import retrofit2.Call
import revilla.aaron.showtime.models.GetImagesResponse

/*
* Interface that an be implemented by the network client
* for this project we will be using Retrofit
* */
interface RestAPI {

    fun getCardsImages(): Call<GetImagesResponse>

}