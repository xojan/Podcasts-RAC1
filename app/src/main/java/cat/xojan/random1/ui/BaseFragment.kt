package cat.xojan.random1.ui

import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat

import cat.xojan.random1.injection.HasComponent

abstract class BaseFragment : Fragment() {

    /**
     * Gets a component for dependency injection by its type.
     */
    protected fun <C> getComponent(componentType: Class<C>): C =
            componentType.cast((activity as HasComponent<*>).component)

    /**
     * Override for custom support for back button click event.
     */
    fun handleOnBackPressed(): Boolean {
        return false
    }
}
