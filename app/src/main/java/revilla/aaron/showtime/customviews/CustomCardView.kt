package revilla.aaron.showtime.customviews

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import revilla.aaron.showtime.R

class CustomCardView: CardView {
    lateinit var frontPart: ImageView
    lateinit var backPart: ImageView
    private val picasso = Picasso.get()
    private var wPixels = 0
    private var hPixels = 0
    val TAG = "CustomCardView"

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context, attrs, defStyle)
    }

    private fun initView(context: Context, attributes: AttributeSet? = null, defStyle: Int? = null) {
        val view = inflate(context, R.layout.custom_card_layout, this)
        frontPart = view.findViewById(R.id.front_part_card)
        backPart = view.findViewById(R.id.back_part_card)
        val resources: Resources = context.resources
        val cardDimen = resources.getDimension(R.dimen.game_board_header_cards)
        wPixels = cardDimen.toInt()
        hPixels = wPixels
    }

    @Synchronized
    fun flipCard(): Throwable? {
        if(frontPart.isVisible) {
//            showFrontSide(false)
            return flipSides(backPart, frontPart)
        } else {
//            showFrontSide(true)
            return flipSides(frontPart, backPart)
        }
        return null
    }

    @Synchronized
    fun showFrontSide() {
        frontPart.visibility = View.VISIBLE
        backPart.visibility = View.INVISIBLE
    }

    @Synchronized
    fun showBackSide() {
        frontPart.visibility = View.INVISIBLE
        backPart.visibility = View.VISIBLE
    }

    fun setImages(frontSideImgURL: String, backSideImgURL: String) {
        picasso.load(backSideImgURL)
            .resize(wPixels, hPixels)
            .centerCrop()
            .into(backPart)
        picasso.load(frontSideImgURL)
            .resize(wPixels, hPixels)
            .centerCrop()
            .into(frontPart)
    }

    private fun flipSides(sideA: View, sideB: View): Throwable? {
        try {
            sideA.visibility = View.VISIBLE
            val scale = context.resources.displayMetrics.density
            val cameraDist = 8000 * scale
            sideA.cameraDistance = cameraDist
            sideB.cameraDistance = cameraDist
            val flipOutAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.flip_card_out
                ) as AnimatorSet
            flipOutAnimatorSet.setTarget(sideB)
            val flipInAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.flip_card_in
                ) as AnimatorSet
            flipInAnimatorSet.setTarget(sideA)
            flipOutAnimatorSet.start()
            flipInAnimatorSet.start()
            flipInAnimatorSet.doOnEnd { sideB.visibility = View.INVISIBLE }
            return null
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return e
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}