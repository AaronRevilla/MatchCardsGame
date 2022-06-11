package revilla.aaron.showtime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import revilla.aaron.showtime.repositories.CardsRepository

class MainActivityModelFactory(val cardsRepository: CardsRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(cardsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}