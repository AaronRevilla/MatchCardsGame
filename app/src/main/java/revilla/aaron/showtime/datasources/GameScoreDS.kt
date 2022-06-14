package revilla.aaron.showtime.datasources

import android.content.Context
import android.content.SharedPreferences

class GameScoreDS(context: Context) {

    private val SHARED_PREFERENCES_NAME = "revilla.aaron.showtime.preferences"
    private val sharedPreferences: SharedPreferences
    private val SAVED_GAME_KEY = "saved_game"
    init {
        sharedPreferences = context.applicationContext.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun saveGame(gameString: String) {
        sharedPreferences.edit().putString(SAVED_GAME_KEY, gameString).apply()
    }

    fun loadGame(): String? {
        return sharedPreferences.getString(SAVED_GAME_KEY, null)
    }
}