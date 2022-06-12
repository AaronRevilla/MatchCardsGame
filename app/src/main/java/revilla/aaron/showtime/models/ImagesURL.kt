package revilla.aaron.showtime.models

import revilla.aaron.showtime.utils.Constants

class ImagesURL(private val file: String) {
    fun getImageURL(): String = Constants.SHOWTIME_URL + file
}