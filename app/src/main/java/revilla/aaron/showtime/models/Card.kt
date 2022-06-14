package revilla.aaron.showtime.models

class Card(val imgURL: String, val backSideCardImgURL: String) {

    var isFrontSideUp = false
    var hasFoundThePair = false

    override fun equals(other: Any?): Boolean {
        if(other is Card) {
            return imgURL == other.imgURL
        }
        return false
    }

    override fun hashCode(): Int {
        var result = imgURL.hashCode()
        result = 31 * result + backSideCardImgURL.hashCode()
        return result
    }

    //clone this card
    fun clone(): Card {
        return Card(imgURL, backSideCardImgURL)
    }

    override fun toString(): String {
        if (isFrontSideUp)
            return "1"
        else
            return "0"
    }
}