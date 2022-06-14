package revilla.aaron.showtime.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.datasources.CardsImagesDS
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


    suspend fun getImages(coroutineDispatcher: CoroutineDispatcher): MutableLiveData<List<ImagesURL>>? {
        val result = withContext(ioDispatcher) {
            if(inMemoryCards.isNullOrEmpty())
                fetchImagesFromRemoteDS(coroutineDispatcher)
            else {
                val cardImagesML = MutableLiveData<List<ImagesURL>>()
                cardImagesML.postValue(inMemoryCards)
                cardImagesML
            }
        }
        return result
    }

    /*
    * Function to fetch the cards images from the remote data source
    * */
    private fun fetchImagesFromRemoteDS(coroutineDispatcher: CoroutineDispatcher): MutableLiveData<List<ImagesURL>>? {
        val result = dataSource.getImages()
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    inMemoryCards = it
                    val cardImagesML = MutableLiveData<List<ImagesURL>>()
                    cardImagesML.postValue(inMemoryCards)
                    return cardImagesML
                }
            }
            Status.ERROR -> {
                return null
            }
            else -> {
                return null
            }
        }
        return null
    }
}
