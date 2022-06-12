package revilla.aaron.showtime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import revilla.aaron.showtime.repositories.CardsRepository

class MainActivityViewModel(private val cardsRepository: CardsRepository): ViewModel() {

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cardsRepository.getImages()?.let { mutableList ->
                    print(mutableList)
                }
            }
        }
    }
}