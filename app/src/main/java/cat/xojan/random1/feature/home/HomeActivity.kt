package cat.xojan.random1.feature.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.feature.MediaPlayerBaseActivity
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.DaggerHomeComponent
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.injection.module.HomeModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject

class HomeActivity : MediaPlayerBaseActivity(), HasComponent<HomeComponent> {
    companion object {
        private const val PERMISSION_WRITE_EXTERNAL_STORAGE = 20
    }

    @Inject
    internal lateinit var viewModel: HomeViewModel
    @Inject
    internal lateinit var crashReporter: CrashReporter
    private val compositeDisposable = CompositeDisposable()

    private var programFragment: ProgramFragment? = null
    private var downloadsFragment: DownloadsFragment? = null

    override val component: HomeComponent by lazy {
        DaggerHomeComponent.builder()
                .appComponent(applicationComponent)
                .baseActivityModule(activityModule)
                .homeModule(HomeModule(this))
                .build()
    }

    private val pageAdapter: HomePagerAdapter by lazy {
        HomePagerAdapter(supportFragmentManager, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initView(savedInstanceState)
        component.inject(this)
    }

    override fun onMediaControllerConnected() {
        programFragment?.onMediaControllerConnected()
        downloadsFragment?.onMediaControllerConnected()
    }

    private fun initView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            programFragment = ProgramFragment()
            downloadsFragment = DownloadsFragment()
        } else {
            programFragment = supportFragmentManager.getFragment(savedInstanceState,
                    ProgramFragment.TAG) as ProgramFragment
            downloadsFragment = supportFragmentManager.getFragment(savedInstanceState,
                    DownloadsFragment.TAG) as DownloadsFragment
        }

        pageAdapter.addFragment(programFragment!!)
        pageAdapter.addFragment(downloadsFragment!!)

        viewPager.adapter = pageAdapter as PagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_podcasts -> if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestWriteExternalStoragePermission()
            } else {
                exportPodcasts()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                exportPodcasts()
            }
        }
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_EXTERNAL_STORAGE)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (programFragment != null && programFragment!!.isAdded) {
            supportFragmentManager.putFragment(outState, ProgramFragment.TAG, programFragment as
                    ProgramFragment)
        }
        if (downloadsFragment != null && downloadsFragment!!.isAdded) {
            supportFragmentManager.putFragment(outState, DownloadsFragment.TAG, downloadsFragment
                    as DownloadsFragment)
        }
    }

    private fun exportPodcasts() {
        compositeDisposable.add(viewModel.exportPodcasts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { notifyUser() },
                        { e -> crashReporter.logException(e) }
                ))
    }

    private fun notifyUser() {
        Toast.makeText(this, getString(R.string.podcasts_exported), Toast.LENGTH_LONG)
                .show()
    }
}