package revilla.aaron.showtime.models

class Card(val imgURL: String, val backSideCardImgURL: String) {

    override fun equals(other: Any?): Boolean {
        if(other is Card) {
            return this.imgURL.equals(other.imgURL)
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
}