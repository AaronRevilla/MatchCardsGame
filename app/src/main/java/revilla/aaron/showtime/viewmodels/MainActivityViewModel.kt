package revilla.aaron.showtime.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.models.Card
import revilla.aaron.showtime.models.Game
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.repositories.CardsRepository
import revilla.aaron.showtime.repositories.GameScoreRepository
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class MainActivityViewModel(
    private val cardsRepository: CardsRepository,
    private val gameScoreRepository: GameScoreRepository
) : ViewModel() {

    //Loading Obervable
    private val mLoadingObserver = MutableLiveData<Boolean?>()
    val loadingObserver: LiveData<Boolean?> = mLoadingObserver
    //Card Observable
    private val mCardsOberver = MutableLiveData<List<Card>>()
    var cardsObserver: LiveData<List<Card>> = mCardsOberver
    //Game Observable
    private val mGameOberver = MutableLiveData<Game>()
    var gameObserver: LiveData<Game> = mGameOberver
    //Game constants
    private val minNumberImages = 3
    private val pairsOfCards = 2
    private var functionsLoadingInformation = AtomicInteger(0)

    /*
    * On ViewModel creates will fetch the images from the images repo
    * and convert those images to a List of Cards objects
    * */
    init {
        viewModelScope.launch {
            loadGame()
            loadImages()
        }
    }

    /*
     *Function to load the last state of the game
     */
    private suspend fun loadGame() {
        showLoadingScreen(true)
        gameScoreRepository.getSavedGame().let { savedGame ->
            mGameOberver.postValue(savedGame)
            showLoadingScreen(false)
        }
    }

    /*
     * Function to fetch the images from the images repo
     */
    private suspend fun loadImages() {
        showLoadingScreen(true)
        cardsRepository.getImages(Dispatchers.IO)?.let { mutableList ->
            getGameCards(mutableList.value)?.let {
                mCardsOberver.postValue(it.toList())
                showLoadingScreen(false)
            } ?: kotlin.run {
                //throw error
                showLoadingScreen(false)
            }
        }
    }

    /*
    * Function to convert the image list to a displayable card deck
    * */
    private fun getGameCards(imagesURL: List<ImagesURL>?): MutableList<Card>? {
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
    * Function to control the show/hide the loading screen
    * */
    private fun showLoadingScreen(show: Boolean) {
        if(show) {
            //initial load
            if(functionsLoadingInformation.get()==0)
                mLoadingObserver.postValue(true)
            functionsLoadingInformation.incrementAndGet()
        } else {
            functionsLoadingInformation.decrementAndGet()
            if(functionsLoadingInformation.get()==0)
                mLoadingObserver.postValue(false)
        }
    }

}