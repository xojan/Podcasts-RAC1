package cat.xojan.random1.feature.browser

import android.app.Activity
import android.graphics.drawable.Animatable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_FILE_PATH
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import cat.xojan.random1.domain.model.PodcastState
import cat.xojan.random1.feature.mediaplayback.QueueManager.Companion.MEDIA_ID_PLAY_ALL
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.podcast_item.*
import java.util.*


class PodcastListAdapter(private val viewModel: BrowserViewModel,
                         private val activity: Activity) : RecyclerView
.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    var podcasts = emptyList<MediaBrowserCompat.MediaItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updatePodcastsState(updatedStatePodcasts: List<MediaBrowserCompat.MediaItem>) {
        for (mediaItem in podcasts) {
            val podcast = mediaItem.description
            podcast.extras?.putString(PODCAST_FILE_PATH, null)
            podcast.extras?.putString(PODCAST_STATE, PodcastState.LOADED.name)
        }

        for (p in updatedStatePodcasts) {
            val updatedPodcast = p.description
            val currentPodcast = podcasts.firstOrNull { it.mediaId == p.mediaId }
            currentPodcast?.let {
                val description = currentPodcast.description
                description.extras?.putString(PODCAST_FILE_PATH,
                        updatedPodcast.extras?.getString(PODCAST_FILE_PATH))
                description.extras?.putString(PODCAST_STATE,
                        updatedPodcast.extras?.getString(PODCAST_STATE))
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.podcast_item_header, parent, false)
            HeaderViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.podcast_item, parent,false)
            MediaItemViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind()
        } else {
            (holder as MediaItemViewHolder).bind(getItem(position), viewModel, activity)
        }
    }

    override fun getItemCount(): Int = podcasts.size + 1

    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position)) {
            return TYPE_HEADER
        }
        return TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): MediaBrowserCompat.MediaItem {
        return podcasts[position - 1]
    }

    class MediaItemViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val STATE_NONE = 0
        private val STATE_PAUSED = 1
        private val STATE_PLAYING = 2
        private val STATE_BUFFERING = 3

        fun bind(item: MediaBrowserCompat.MediaItem, viewModel: BrowserViewModel,
                 activity: Activity) {

            val podcast = item.description

            itemView.setOnClickListener {
                MediaControllerCompat.getMediaController(itemView.context as Activity)
                        .transportControls.playFromMediaId(item.mediaId, null)
            }

            val state = PodcastState.fromString(podcast.extras!!.getString(PODCAST_STATE)!!)
            when (state) {
                PodcastState.LOADED -> podcast_icon.setImageResource(R.drawable.ic_arrow_down)
                PodcastState.DOWNLOADING -> {
                    podcast_icon.setImageResource(R.drawable.animated_arrow)
                    if (podcast_icon.drawable is Animatable) {
                        (podcast_icon.drawable as Animatable).start()
                    }
                }
                PodcastState.DOWNLOADED -> podcast_icon.setImageResource(R.drawable.ic_delete)
            }

            podcast_icon.setOnClickListener {
                when (state) {
                    PodcastState.LOADED -> viewModel.downloadPodcast(podcast)
                    PodcastState.DOWNLOADING -> {}
                    PodcastState.DOWNLOADED -> viewModel.deletePodcast(podcast)
                }
                viewModel.refreshDownloadedPodcast()
            }

            podcast_title.text = podcast.title
            Glide.with(itemView.context)
                    .load(podcast.iconUri.toString() + "?w=" + getWeekOfTheYear())
                    .apply(RequestOptions()
                            .placeholder(R.drawable.placeholder)
                            .circleCrop())
                    .into(podcast_image)

            val playbackState = getMediaItemState(activity, item)
            when (playbackState) {
                STATE_PLAYING -> {
                    playback_state.setImageResource(R.drawable.ic_equalizer_white_36dp)
                    if (playback_state.drawable is Animatable) {
                        (playback_state.drawable as Animatable).start()
                    }
                }
                STATE_PAUSED -> playback_state.setImageResource(R.drawable.ic_equalizer_white_24px)
                STATE_BUFFERING -> playback_state.setImageResource(R.drawable.ic_equalizer_white_24px)
                else -> playback_state.setImageResource(R.drawable.ic_play_arrow)
            }
        }

        private fun getWeekOfTheYear(): Int {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.WEEK_OF_YEAR)
        }

        private fun getMediaItemState(activity: Activity,
                                      mediaItem: MediaBrowserCompat.MediaItem): Int {
            val controller = MediaControllerCompat.getMediaController(activity)
            controller?.let {
                val controllerMediaId = controller.metadata?.description?.mediaId
                if (controllerMediaId == mediaItem.mediaId) {
                    val playbackState = controller.playbackState
                    playbackState?.let {
                        return when (playbackState.state) {
                            PlaybackStateCompat.STATE_PLAYING -> STATE_PLAYING
                            PlaybackStateCompat.STATE_PAUSED -> STATE_PAUSED
                            PlaybackStateCompat.STATE_BUFFERING -> STATE_BUFFERING
                            else -> STATE_NONE
                        }
                    }
                }
            }
            return STATE_NONE
        }
    }

    class HeaderViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind() {
            itemView.setOnClickListener {
               MediaControllerCompat.getMediaController(itemView.context as Activity)
                        .transportControls.playFromMediaId(MEDIA_ID_PLAY_ALL, null)
            }
        }
    }
}