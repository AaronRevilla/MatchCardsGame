package revilla.aaron.showtime.datasources

import revilla.aaron.showtime.models.DataCallback
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.network.RestAPI
import java.io.IOException
import javax.inject.Inject

class CardsImagesDS(private val api: RestAPI) {

    fun getImages(): DataCallback<List<ImagesURL>> {
        try {
            val response = api.getCardsImages().execute()
            if (response.isSuccessful) {
                return DataCallback.success(response.body()?.images)
            } else {
                return DataCallback.error(IOException("Error getting the images").toString(), null)
            }
        } catch (e: Throwable) {
            return DataCallback.error(IOException("Error getting the images", e).toString(), null)
        }
    }
}