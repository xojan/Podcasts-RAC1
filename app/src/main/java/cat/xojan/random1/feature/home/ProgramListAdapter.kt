package cat.xojan.random1.feature.home

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.feature.browser.BrowseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.program_item.*
import java.util.*


class ProgramListAdapter: RecyclerView.Adapter<ProgramListAdapter.MediaItemViewHolder>() {

    var programs: List<MediaBrowserCompat.MediaItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = programs.size

    override fun onBindViewHolder(holder: MediaItemViewHolder?, position: Int) {
        holder?.bind(programs[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MediaItemViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.program_item, parent, false)
        return MediaItemViewHolder(itemView)
    }

    class MediaItemViewHolder(override val containerView: View) : RecyclerView
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
                    .placeholder(R.drawable.placeholder)
                    .into(programImage)
        }

        private fun getWeekOfTheYear(): Int {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.WEEK_OF_YEAR)
        }
    }
}