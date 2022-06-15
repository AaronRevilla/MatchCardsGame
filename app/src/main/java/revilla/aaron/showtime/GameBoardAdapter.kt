package revilla.aaron.showtime

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import revilla.aaron.showtime.customviews.CustomCardView
import revilla.aaron.showtime.models.Card

class GameBoardAdapter(cardDeck: List<Card>, val clickListener: ItemClickListener) :
    Adapter<GameBoardAdapter.GameBoardViewHolder>() {

    private var list = cardDeck

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameBoardViewHolder {
        val customCardView = CustomCardView(parent.context)
        customCardView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return GameBoardViewHolder(customCardView)
    }

    override fun onBindViewHolder(holder: GameBoardViewHolder, position: Int) {
        val card = list.get(position)
        holder.bind(card)
    }

    override fun getItemCount(): Int = list.size

    fun updateCardList(updatedList: List<Card>) {
        list = updatedList
        notifyDataSetChanged()
    }

    inner class GameBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        fun bind(card: Card) {
            val customCardView = itemView as CustomCardView
            customCardView.setImages(card.imgURL, card.backSideCardImgURL)

            if (card.isFrontSideUp)
                customCardView.showFrontSide()
            else
                customCardView.showBackSide()
            if (!card.hasFoundThePair)
                customCardView.setOnClickListener(this)
            else
                customCardView.setOnClickListener(null)
        }

        override fun onClick(view: View?) {
            clickListener.onItemClick(view, adapterPosition)
        }

    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}