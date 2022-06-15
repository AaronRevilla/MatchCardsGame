package revilla.aaron.showtime.datasources

import android.content.Context
import android.content.SharedPreferences

class GameScoreDS(context: Context) {

    private val SHARED_PREFERENCES_NAME = "revilla.aaron.showtime.preferences"
    private val sharedPreferences: SharedPreferences
    private val SAVED_GAME_SCORE_KEY = "saved_game_score"
    private val SAVED_GAME_CARDS_KEY = "saved_game_cards"
    init {
        sharedPreferences = context.applicationContext.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun saveGame(gameString: String) {
        sharedPreferences.edit().putString(SAVED_GAME_SCORE_KEY, gameString).apply()
    }

    fun loadGame(): String? {
        return sharedPreferences.getString(SAVED_GAME_SCORE_KEY, null)
    }

    fun saveCards(cards: String?) {
        sharedPreferences.edit().putString(SAVED_GAME_CARDS_KEY, cards).apply()
    }

    fun loadCards(): String? {
        return sharedPreferences.getString(SAVED_GAME_CARDS_KEY, null)
    }
}