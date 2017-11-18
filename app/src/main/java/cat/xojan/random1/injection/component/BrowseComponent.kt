package cat.xojan.random1.injection.component

import cat.xojan.random1.injection.PerActivity
import cat.xojan.random1.injection.module.BaseActivityModule
import cat.xojan.random1.injection.module.BrowseModule
import cat.xojan.random1.ui.browser.BrowseActivity
import cat.xojan.random1.ui.browser.HourByHourListFragment
import cat.xojan.random1.ui.browser.PodcastListFragment
import cat.xojan.random1.ui.browser.SectionFragment
import dagger.Component

@PerActivity
@Component(
        dependencies = arrayOf(AppComponent::class),
        modules = arrayOf(BaseActivityModule::class, BrowseModule::class))
interface BrowseComponent : BaseActivityComponent {
    fun inject(broweActivity: BrowseActivity)
    fun inject(podcastListFragment: PodcastListFragment)
    fun inject(sectionListFragment: SectionFragment)
    fun inject(hourByHourListFragment: HourByHourListFragment)
}