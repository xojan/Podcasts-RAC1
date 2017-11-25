package cat.xojan.random1.ui.browser

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.ui.mediaplayer.MediaPlaybackActivity
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.podcast_item.*
import java.util.*


class PodcastListAdapter: RecyclerView.Adapter<PodcastListAdapter.MediaItemViewHolder>() {

    var podcasts: List<MediaBrowserCompat.MediaItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updateWithDownloaded(downloadedPodcasts: List<Podcast>) {
       /* for (podcast in podcasts) {
            podcast.filePath = null
            podcast.state = Podcast.State.LOADED
        }

        for (download in downloadedPodcasts) {
            val index = podcasts.indexOf(download)
            if (index >= 0) {
                val podcast = podcasts[index]
                podcast.filePath = download.filePath
                podcast.state = download.state
            }
        }*/
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.podcast_item, parent, false)
        return MediaItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder?, position: Int) {
        holder?.bind(podcasts[position])
    }

    override fun getItemCount(): Int = podcasts.size

    class MediaItemViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: MediaBrowserCompat.MediaItem) {
            itemView.setOnClickListener {
                //val intent = Intent(itemView.context, MediaPlaybackActivity::class.java)
//                val intent = Intent(itemView.context, RadioPlayerActivity::class.java)
//                intent.putExtra(RadioPlayerActivity.EXTRA_PODCAST, podcast)
                //itemView.context.startActivity(intent)
                val extras = Bundle()

                extras.putParcelable("mediaUrl", item.description.mediaUri)
                MediaControllerCompat.getMediaController(itemView.context as BrowseActivity).transportControls
                        .playFromMediaId(item.mediaId, extras)
            }

            /*podcast_icon.setOnClickListener {
                when (podcast.state) {
                    Podcast.State.LOADED -> interactor.download(podcast)
                    Podcast.State.DOWNLOADING -> {}
                    Podcast.State.DOWNLOADED -> interactor.deleteDownload(podcast)
                }
                interactor.refreshDownloadedPodcasts()
            }*/

           /* when (podcast.state) {
                Podcast.State.LOADED -> podcast_icon.setImageResource(R.drawable.ic_arrow_down)
                Podcast.State.DOWNLOADING -> {
                    podcast_icon.setImageResource(R.drawable.animated_arrow)
                    if (podcast_icon.drawable is Animatable) {
                        (podcast_icon.drawable as Animatable).start()
                    }
                }
                Podcast.State.DOWNLOADED -> podcast_icon.setImageResource(R.drawable.ic_delete)
            }*/

            val podcast = item.description
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