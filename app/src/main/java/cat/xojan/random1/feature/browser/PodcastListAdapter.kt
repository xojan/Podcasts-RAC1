package cat.xojan.random1.feature.browser

import android.graphics.drawable.Animatable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_FILE_PATH
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.podcast_item.*
import java.util.*


class PodcastListAdapter(private val viewModel: BrowserViewModel) : RecyclerView
.Adapter<PodcastListAdapter.MediaItemViewHolder>() {

    var podcasts = emptyList<MediaBrowserCompat.MediaItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updatePodcastsState(updatedStatePodcasts: List<MediaBrowserCompat.MediaItem>) {
        for (mediaItem in podcasts) {
            val podcast = mediaItem.description
            podcast.extras?.putString(PODCAST_FILE_PATH, null)
            podcast.extras?.putSerializable(PODCAST_STATE, Podcast.State.LOADED)
        }

        for (p in updatedStatePodcasts) {
            val updatedPodcast = p.description
            val currentPodcast = podcasts.firstOrNull { it.mediaId == p.mediaId }
            currentPodcast?.let {
                val description = currentPodcast.description
                description.extras?.putString(PODCAST_FILE_PATH,
                        updatedPodcast.extras?.getString(PODCAST_FILE_PATH))
                description.extras?.putSerializable(PODCAST_STATE,
                        updatedPodcast.extras?.getSerializable(PODCAST_STATE) as Podcast.State)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.podcast_item, parent, false)
        return MediaItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder?, position: Int) {
        holder?.bind(podcasts[position], viewModel)
    }

    override fun getItemCount(): Int = podcasts.size

    class MediaItemViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: MediaBrowserCompat.MediaItem, viewModel: BrowserViewModel) {
            val podcast = item.description

            itemView.setOnClickListener {
                MediaControllerCompat.getMediaController(itemView.context as BrowseActivity)
                        .transportControls.playFromMediaId(item.mediaId, null)
            }

            val state = podcast.extras?.getSerializable(PODCAST_STATE) as Podcast.State
            when (state) {
                Podcast.State.LOADED -> podcast_icon.setImageResource(R.drawable.ic_arrow_down)
                Podcast.State.DOWNLOADING -> {
                    podcast_icon.setImageResource(R.drawable.animated_arrow)
                    if (podcast_icon.drawable is Animatable) {
                        (podcast_icon.drawable as Animatable).start()
                    }
                }
                Podcast.State.DOWNLOADED -> podcast_icon.setImageResource(R.drawable.ic_delete)
            }

            podcast_icon.setOnClickListener {
                when (state) {
                    Podcast.State.LOADED -> viewModel.downloadPodcast(podcast)
                    Podcast.State.DOWNLOADING -> {}
                    Podcast.State.DOWNLOADED -> viewModel.deletePodcast(podcast)
                }
                viewModel.refreshDownloadedPodcast()
            }

            podcast_title.text = podcast.title
            Picasso.with(itemView.context)
                    .load(podcast.iconUri.toString() + "?w=" + getWeekOfTheYear())
                    .resize(200, 200)
                    .placeholder(R.drawable.default_rac1)
                    .transform(CircleTransform())
                    .into(podcast_image)
        }

        private fun getWeekOfTheYear(): Int {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.WEEK_OF_YEAR)
        }
    }
}