package revilla.aaron.showtime.viewmodels

import android.os.Handler
import android.os.Looper
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
import revilla.aaron.showtime.models.Status
import revilla.aaron.showtime.repositories.CardsRepository
import revilla.aaron.showtime.repositories.GameScoreRepository
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger


class MainActivityViewModel(
    private val cardsRepository: CardsRepository,
    private val gameScoreRepository: GameScoreRepository
) : ViewModel() {

    //Loading Obervable
    private val mLoadingObserver = MutableLiveData<Boolean?>()
    val loadingObserver: LiveData<Boolean?> = mLoadingObserver

    //Card Observable
    private val mCardBoardObserver = MutableLiveData<List<Card>>()
    var cardBoardObserver: LiveData<List<Card>> = mCardBoardObserver

    //Card Observable
    private val mCardObserver = MutableLiveData<List<Pair<Int, Card>>>()
    var cardObserver: LiveData<List<Pair<Int, Card>>> = mCardObserver

    //Game Observable
    private val mGameOberver = MutableLiveData<Game>()
    var gameObserver: LiveData<Game> = mGameOberver

    //Finish the Game Observable
    private val mGameIsOver = MutableLiveData<Boolean>()
    var gameIsOverObserver: LiveData<Boolean> = mGameIsOver

    //Messages Observable
    private val mMessages = MutableLiveData<String>()
    var messagesObservable: LiveData<String> = mMessages

    //Game constants
    private val pairsOfCards = 8
    private var functionsLoadingInformation = AtomicInteger(0)

    /*
    * On ViewModel creates will fetch the images from the images repo
    * and convert those images to a List of Cards objects
    * */
    init {
        playAgainOrLoadGame()
    }

    /*
    * Function to load a game in memory or to star over a new one
    * */
    fun playAgainOrLoadGame() {
        viewModelScope.launch {
            if (isInternetAvailable()) {
                loadGame()
                loadImages()
            } else {
                mMessages.postValue("Looks like you don't have an Internet Connection, please connect and try again")
            }
        }
    }

    /*
     *Function to load the last state of the game
     */
    private suspend fun loadGame() {
        showLoadingScreen(true)
        gameScoreRepository.getSavedGame().let { savedGame ->
            mGameIsOver.postValue(savedGame.isGameOver)
            mGameOberver.postValue(savedGame)
            showLoadingScreen(false)
        }
    }

    /*
   * Function to save the current state of the game
   * */
    fun saveGame() {
        gameObserver.value?.let {
            gameScoreRepository.saveGame(it)
        }
        cardBoardObserver.value?.let {
            cardsRepository.saveCards(it)
        }
    }

    fun newGame() {
        gameObserver.value?.let {
            it.flips = 0
            it.isGameOver = false
            gameScoreRepository.saveGame(it)
        }
        cardsRepository.saveCards(null)
        playAgainOrLoadGame()
    }

    fun resetGame() {
        val newGame = Game()
        newGame.flips = 0
        newGame.wins = 0
        newGame.isGameOver = false
        gameScoreRepository.saveGame(newGame)
        cardsRepository.saveCards(null)
        playAgainOrLoadGame()
    }

    /*
     * Function to fetch the images from the images repo
     */
    private suspend fun loadImages() {
        showLoadingScreen(true)
        cardsRepository.getImages(pairsOfCards)?.let { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let {
                        mCardBoardObserver.postValue(it)
                        showLoadingScreen(false)
                    } ?: kotlin.run {
                        //throw error
                        showLoadingScreen(false)
                    }
                }
                else -> {
                    showLoadingScreen(false)
                    mMessages.postValue(result.message ?: "Unexpected Error loading the game cards")
                }
            }
        }
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
    * Delay the logic so that the user can see the second flipped card
    * */
    fun cardFliped(position: Int, looper: Looper) {
        Handler(looper).postDelayed({
            cardBoardObserver.value?.let { currentCardBoard ->
                val flipedCard = currentCardBoard[position]
                currentCardBoard.find { findCard ->
                    //Look up for a card which front side is up and hasn't have a pair yet
                    findCard.isFrontSideUp && !findCard.hasFoundThePair
                }?.let { lastFlippedCard ->
                    val lastFlippedCardPosition = currentCardBoard.indexOfFirst { findIndexCard ->
                        findIndexCard.isFrontSideUp && !findIndexCard.hasFoundThePair
                    }
                    //has found the pair
                    if (lastFlippedCard.equals(flipedCard)) {
                        //keep cards flipped up
                        flipedCard.hasFoundThePair = true
                        flipedCard.isFrontSideUp = true
                        lastFlippedCard.hasFoundThePair = true
                        val arrayListResponse = ArrayList<Pair<Int, Card>>()
                        arrayListResponse.add(Pair(position, flipedCard))
                        arrayListResponse.add(Pair(lastFlippedCardPosition, lastFlippedCard))
                        mCardObserver.postValue(arrayListResponse)
                        //increment tries count
                        gameObserver.value?.let { currentGame ->
                            currentGame.flips++
                            mGameOberver.postValue(currentGame)
                        }
                    } else {
                        //user didnt found the pair, flip both cards over
                        flipedCard.isFrontSideUp = false
                        lastFlippedCard.isFrontSideUp = false
                        val arrayListResponse = ArrayList<Pair<Int, Card>>()
                        arrayListResponse.add(Pair(position, flipedCard))
                        arrayListResponse.add(Pair(lastFlippedCardPosition, lastFlippedCard))
                        mCardObserver.postValue(arrayListResponse)
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
                        currentGame.isGameOver = true
                        mGameOberver.postValue(currentGame)
                        mMessages.postValue("You Win!!!")
                        saveGame()
                    }
                    mGameIsOver.postValue(true)
                }
            }
        }, 1000)
    }

    private suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            var result = false
            try {
                val address: InetAddress = InetAddress.getByName("www.google.com")
                result = !address.equals("")
            } catch (e: UnknownHostException) {
                // Log error
                Log.e("No Internet Connection", e.message, e)
            }
            result
        }
    }
}