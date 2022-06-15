package revilla.aaron.showtime.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.datasources.CardsImagesDS
import revilla.aaron.showtime.datasources.GameScoreDS
import revilla.aaron.showtime.models.*
import javax.inject.Inject
import kotlin.random.Random

class CardsRepository @Inject constructor(
    private val dataSource: CardsImagesDS,
    private val localDS: GameScoreDS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

//    private val cardImagesML = MutableLiveData<List<ImagesURL>>()
//    val cardImagesObserver: LiveData<List<ImagesURL>> = cardImagesML
//    private var inMemoryCards: List<ImagesURL>? = null
    private val gson = Gson()

    /*
    * Load cards if there's an ongoing game if not it will create a new deck
    * */
    suspend fun getImages(pairsOfCards: Int): DataCallback<List<Card>?> {
        val result = withContext(ioDispatcher) {
            loadLocalGame()?.let {
                DataCallback.success(it)
            } ?: kotlin.run {
                //create a new deck of cards
                fetchImagesFromRemoteDS(pairsOfCards)
            }
        }
        return result
    }

    /*
    * Function to fetch the cards images from the remote data source
    * */
    private fun fetchImagesFromRemoteDS(pairsOfCards: Int): DataCallback<List<Card>?> {
        val result = dataSource.getImages()
        var cardImagesML: DataCallback<List<Card>?> = DataCallback.error("Unexpected Error", null)
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    val cardsDeck = getGameCards(it, pairsOfCards)
                    cardImagesML = DataCallback.success(cardsDeck)
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

    /*
    * Function to convert the image list to a displayable card deck
    * */
    private fun getGameCards(imagesURL: List<ImagesURL>?, pairsOfCards: Int): MutableList<Card>? {
        val minNumberImages = pairsOfCards / 2
        if (!imagesURL.isNullOrEmpty() && imagesURL.size >= minNumberImages) {
            val cards = ArrayList<Card>()
            val randomPositionList = generateRandomPositionList(pairsOfCards, imagesURL.size - 1)
            for (randomNumber in randomPositionList) {
                val newCard =
                    Card(imagesURL[randomNumber].getImageURL(), imagesURL.last().getImageURL())
                cards.add(newCard)
                cards.add(newCard.clone())
            }
            val mutableList = cards.toMutableList()
            mutableList.shuffle()
            return mutableList
        } else {
            //throw error that the game doesnt have enough images
            return null
        }
    }

    /*
    * Function to generate random card position numbers
    * */
    private fun generateRandomPositionList(listSize: Int, randomRange: Int): List<Int> {
        val randomNumberList = ArrayList<Int>()
        while (randomNumberList.size < listSize) {
            val randomPosition = Random.nextInt(0, randomRange)
            if (!randomNumberList.contains(randomPosition)) {
                randomNumberList.add(randomPosition)
            }
        }
        return randomNumberList
    }

    /*
    * Function to save current state of the cards
    * */
    fun saveCards(cards: List<Card>?) {
        localDS.saveCards(convertCardsToJSON(cards))
    }

    private fun loadLocalGame(): List<Card>? {
        localDS.loadCards()?.let {
            return convertJSONCardsToObj(it).toList()
        } ?: kotlin.run { return null }
    }

    private fun convertCardsToJSON(cards: List<Card>?): String? {
        if(cards.isNullOrEmpty())
            return null
        else
            return gson.toJson(cards.toTypedArray())
    }

    private fun convertJSONCardsToObj(json: String): Array<Card> {
        return gson.fromJson(
            json,
            Array<Card>::class.java
        )
    }
}
