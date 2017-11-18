package cat.xojan.random1.ui.browser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.entities.Section
import cat.xojan.random1.ui.BaseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.section_item.*
import java.util.*


class SectionListAdapter(private val activity: BaseActivity, private val program: Program)
    : RecyclerView.Adapter<SectionListAdapter.ViewHolder>() {

    var sections: List<Section> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.section_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(sections[position], program, activity)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    class ViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView),LayoutContainer {

        fun bind(item: Section, program: Program, activity: BaseActivity) {
            itemView.setOnClickListener {
                val podcastListFragment = PodcastListFragment.newInstance(item, program)
                activity.addFragment(podcastListFragment, PodcastListFragment.TAG, true)
            }
            section_title.text = item.title
            Picasso.with(itemView.context)
                    .load(program.imageUrl() + "?w=" + getWeekOfTheYear())
                    .resize(200, 200)
                    .transform(CircleTransform())
                    .placeholder(R.drawable.default_rac1)
                    .into(section_image)
        }

        private fun getWeekOfTheYear(): Int {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.WEEK_OF_YEAR)
        }
    }
}