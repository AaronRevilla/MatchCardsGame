package revilla.aaron.showtime.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.models.Card
import revilla.aaron.showtime.models.ImagesURL
import revilla.aaron.showtime.repositories.CardsRepository
import kotlin.random.Random

class MainActivityViewModel(private val cardsRepository: CardsRepository) : ViewModel() {

    private val mLoadingObserver = MutableLiveData<Boolean?>()
    val loadingObserver: LiveData<Boolean?> = mLoadingObserver
    private val mCardsOberver = MutableLiveData<List<Card>>()
    var cardsObserver: LiveData<List<Card>> = mCardsOberver
    val minNumberImages = 3
    val pairsOfCards = 2

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mLoadingObserver.postValue(true)
                cardsRepository.getImages()?.let { mutableList ->
                    getGameCards(mutableList.value)?.let {
                        mCardsOberver.postValue(it.toList())
                        mLoadingObserver.postValue(false)
                    } ?: kotlin.run {
                        //throw error
                        mLoadingObserver.postValue(false)
                    }
                }
            }
        }
    }

    private fun getGameCards(imagesURL: List<ImagesURL>?): MutableList<Card>? {
        if (!imagesURL.isNullOrEmpty() && imagesURL.size >= minNumberImages) {
            val cards = ArrayList<Card>()
            val randomPositionList = generateRandomPositionList(pairsOfCards, imagesURL.size - 1)
            for(randomNumber in randomPositionList) {
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

    private fun generateRandomPositionList(listSize: Int, randomRange: Int): List<Int> {
        val randomNumberList = ArrayList<Int>()
        while (randomNumberList.size < listSize) {
            val randomPosition = Random.nextInt(0, randomRange)
            if(!randomNumberList.contains(randomPosition)){
                randomNumberList.add(randomPosition)
            }
        }
        return randomNumberList
    }
}