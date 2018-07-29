package cat.xojan.random1.feature.browser

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.section_item.*
import java.util.*


class SectionListAdapter(private val activity: BrowseActivity)
    : RecyclerView.Adapter<SectionListAdapter.MediaItemViewHolder>() {

    var sections: List<MediaBrowserCompat.MediaItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.section_item, parent, false)
        return MediaItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
        holder.bind(sections[position], activity)
    }

    override fun getItemCount(): Int = sections.size

    class MediaItemViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView),LayoutContainer {

        fun bind(item: MediaBrowserCompat.MediaItem, activity: BrowseActivity) {
            itemView.setOnClickListener {
                val podcastListFragment = SectionPodcastListFragment.newInstance(item.mediaId)
                activity.addFragment(podcastListFragment, SectionPodcastListFragment.TAG, true)
            }
            val section = item.description
            section_title.text = section.title
            Glide.with(itemView.context)
                    .load(section.iconUri.toString() + "?w=" + getWeekOfTheYear())
                    .apply(RequestOptions()
                            .override(200, 200)
                            .circleCrop()
                            .placeholder(R.drawable.placeholder))
                    .into(section_image)
        }

        private fun getWeekOfTheYear(): Int {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.WEEK_OF_YEAR)
        }
    }
}