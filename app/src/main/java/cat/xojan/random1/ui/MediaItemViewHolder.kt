package cat.xojan.random1.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import cat.xojan.random1.R
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.ui.browser.BrowseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.program_item.*
import java.util.*

class MediaItemViewHolder(override val containerView: View,
                          val interactor: ProgramDataInteractor) : RecyclerView
.ViewHolder(containerView), LayoutContainer {
    fun bind(item: MediaBrowserCompat.MediaItem) {
        itemView.setOnClickListener{
            val intent = BrowseActivity.newIntent(itemView.context, item)
            itemView.context.startActivity(intent)
        }
        val description = item.description
        programTitle.text = description.title
        Picasso.with(itemView.context)
                .load(description.iconUri.toString() + "?w=" + getWeekOfTheYear())
                .placeholder(R.drawable.default_rac1)
                .into(programImage)
    }

    private fun getWeekOfTheYear(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.WEEK_OF_YEAR)
    }
}