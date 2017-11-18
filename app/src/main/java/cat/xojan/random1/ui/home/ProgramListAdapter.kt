package cat.xojan.random1.ui.home

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.ui.MediaItemViewHolder


class ProgramListAdapter(private val interactor: ProgramDataInteractor)
    : RecyclerView.Adapter<MediaItemViewHolder>() {

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
        return MediaItemViewHolder(itemView, interactor)
    }
}