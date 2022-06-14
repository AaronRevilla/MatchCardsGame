package revilla.aaron.showtime.viewmodels

import android.util.Log
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
import java.text.FieldPosition
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

    //Finish the Game Observable
    private val mGameIsOver = MutableLiveData<Boolean>()
    var gameIsOverObserver: LiveData<Boolean> = mGameIsOver

    //Game constants
    private val pairsOfCards = 8
    private val minNumberImages = pairsOfCards / 2
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
        if (show) {
            //initial load
            if (functionsLoadingInformation.get() == 0)
                mLoadingObserver.postValue(true)
            functionsLoadingInformation.incrementAndGet()
        } else {
            functionsLoadingInformation.decrementAndGet()
            if (functionsLoadingInformation.get() == 0)
                mLoadingObserver.postValue(false)
        }
    }

    /*
    * Function to provide the number of columns to the RV
    * */
    fun getGridNumber(): Int = 4

    /*
    * Function with the game rules
    * */
    fun cardFliped(position: Int) {
        cardsObserver.value?.let { currentCardBoard ->
            val flipedCard = currentCardBoard[position]
            currentCardBoard.find { findCard ->
                //Look up for a card which front side is up and hasn't have a pair yet
                findCard.isFrontSideUp == true && findCard.hasFoundThePair == false
            }?.let { foundedCard ->
                //has found the pair
                if (foundedCard.equals(flipedCard)) {
                    //keep cards flipped up
                    flipedCard.hasFoundThePair = true
                    foundedCard.hasFoundThePair = true
                } else {
                    //user didnt found the pair, flip both cards over
                    foundedCard.isFrontSideUp = false
                    flipedCard.isFrontSideUp = false
                    //increment tries count
                    gameObserver.value?.let { currentGame ->
                        currentGame.flips++
                        mGameOberver.postValue(currentGame)
                    }
                }
            } ?: kotlin.run {
                //first card of a pair flipped
                flipedCard.isFrontSideUp = true
            }

            //check if the user has finished the game
            currentCardBoard.find {
                it.isFrontSideUp == false
            }?.let {
                mGameIsOver.postValue(false)
            } ?: kotlin.run {
                //game is over all cards are flipped
                gameObserver.value?.let { currentGame ->
                    currentGame.wins++
                    mGameOberver.postValue(currentGame)
                }
                mGameIsOver.postValue(true)
            }

            //update the view with the latest changes
            mCardsOberver.postValue(currentCardBoard)
        }
    }

    fun printBoard(board: List<Card>) {
        var idx = 0
        val columns = getGridNumber()
        for (colums in 0 until columns) {
            val subList = board.subList(idx, columns + idx)
            Log.d("Aaron ${colums}", subList.toString())
            idx += colums
        }
    }
}