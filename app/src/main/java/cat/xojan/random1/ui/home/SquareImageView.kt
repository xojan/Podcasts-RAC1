package cat.xojan.random1.ui.home

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView


class SquareImageView : ImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}