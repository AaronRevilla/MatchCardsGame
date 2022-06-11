package revilla.aaron.showtime.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import revilla.aaron.showtime.datasources.CardsImagesDS
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.models.Status

class CardsRepository(private val dataSource: CardsImagesDS) {

    suspend fun getImages(): MutableLiveData<List<ImagesURL>>? {
        val result = dataSource.getImages()
        when(result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    val cardImagesML = MutableLiveData<List<ImagesURL>>()
                    cardImagesML.value = it
                    return cardImagesML
                }
            }
            Status.ERROR -> {
                return null
            }
            else -> return null
        }
        return null
    }
}
