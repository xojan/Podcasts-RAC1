package cat.xojan.random1.feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cat.xojan.random1.Application
import cat.xojan.random1.R
import cat.xojan.random1.injection.component.AppComponent
import cat.xojan.random1.injection.module.BaseActivityModule


abstract class BaseActivity : AppCompatActivity() {

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return [AppComponent]
     */
    protected val applicationComponent: AppComponent
        get() = (application as Application).appComponent

    /**
     * Get an Activity module for dependency injection.
     *
     * @return [cat.xojan.random1.injection.component.BaseActivityComponent]
     */
    protected val activityModule: BaseActivityModule
        get() = BaseActivityModule(this)

    /**
     * Adds a [Fragment] to this activity's layout.
     */
    fun addFragment(fragment: Fragment, tag: String,
                    addToBackSTack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_fragment, fragment, tag)
        if (addToBackSTack) fragmentTransaction.addToBackStack(tag)
        fragmentTransaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationComponent.inject(this)
    }
}