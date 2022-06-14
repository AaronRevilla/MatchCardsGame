package revilla.aaron.showtime.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.datasources.CardsImagesDS
import revilla.aaron.showtime.models.DataCallback
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.models.Status
import javax.inject.Inject

class CardsRepository @Inject constructor(
    private val dataSource: CardsImagesDS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

//    private val cardImagesML = MutableLiveData<List<ImagesURL>>()
//    val cardImagesObserver: LiveData<List<ImagesURL>> = cardImagesML
    private var inMemoryCards: List<ImagesURL>? = null


    suspend fun getImages(): DataCallback<List<ImagesURL>?> {
        val result = withContext(ioDispatcher) {
            if(inMemoryCards.isNullOrEmpty())
                fetchImagesFromRemoteDS()
            else {
                DataCallback.success(inMemoryCards)
            }
        }
        return result
    }

    /*
    * Function to fetch the cards images from the remote data source
    * */
    private fun fetchImagesFromRemoteDS(): DataCallback<List<ImagesURL>?>{
        val result = dataSource.getImages()
        var cardImagesML: DataCallback<List<ImagesURL>?> = DataCallback.error("Unexpected Error", null)
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    inMemoryCards = it
                    cardImagesML = DataCallback.success(it)
                }
            }
            Status.ERROR -> {
                cardImagesML = DataCallback.error(result.message, null)
            }
            else -> {
                cardImagesML = DataCallback.error(result.message, null)
            }
        }
        return cardImagesML
    }
}
