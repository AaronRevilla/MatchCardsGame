package revilla.aaron.showtime.repositories

import com.google.gson.Gson
import revilla.aaron.showtime.datasources.GameScoreDS
import revilla.aaron.showtime.models.Game

class GameScoreRepository(val gameScoreDS: GameScoreDS) {

    private val gson = Gson()

    fun saveGame(game: Game){
        gameScoreDS.saveGame(convertGameToJSON(game))
    }

    fun getSavedGame(): Game {
        gameScoreDS.loadGame()?.let {
            return convertJSONGameToObj(it)
        } ?: kotlin.run {
            return Game()
        }
    }

    private fun convertGameToJSON(game: Game): String {
        return gson.toJson(game)
    }

    private fun convertJSONGameToObj(json: String): Game {
        return gson.fromJson<Any>(
            json,
            Game::class.java
        ) as Game
    }
}