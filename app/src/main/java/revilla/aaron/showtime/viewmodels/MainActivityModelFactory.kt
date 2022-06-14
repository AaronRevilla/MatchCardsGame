package revilla.aaron.showtime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import revilla.aaron.showtime.repositories.CardsRepository
import revilla.aaron.showtime.repositories.GameScoreRepository

class MainActivityModelFactory(
    val cardsRepository: CardsRepository,
    val gameScoreRepository: GameScoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(cardsRepository, gameScoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}