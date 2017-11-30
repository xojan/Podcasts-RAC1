package cat.xojan.random1.ui

import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import cat.xojan.random1.domain.interactor.MediaProvider.Companion.ERROR
import cat.xojan.random1.injection.HasComponent

abstract class BaseFragment : Fragment() {

    /**
     * Gets a component for dependency injection by its type.
     */
    protected fun <C> getComponent(componentType: Class<C>): C =
            componentType.cast((activity as HasComponent<*>).component)

    fun isChildrenError(children: List<MediaBrowserCompat.MediaItem>): Boolean {
        return children.isNotEmpty() && children[0].mediaId == ERROR
    }
}
