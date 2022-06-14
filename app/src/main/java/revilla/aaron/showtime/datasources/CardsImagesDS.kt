package revilla.aaron.showtime.datasources

import revilla.aaron.showtime.models.DataCallback
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.network.RestAPI
import java.io.IOException
import javax.inject.Inject

class CardsImagesDS @Inject constructor(private val api: RestAPI) {

    fun getImages(): DataCallback<List<ImagesURL>> {
        return try {
            val response = api.getCardsImages().execute()
            if (response.isSuccessful) {
                DataCallback.success(response.body()?.images)
            } else {
                DataCallback.error(IOException("Error getting the images").toString(), null)
            }
        } catch (e: Throwable) {
            DataCallback.error(IOException("Error getting the images", e).toString(), null)
        }
    }
}