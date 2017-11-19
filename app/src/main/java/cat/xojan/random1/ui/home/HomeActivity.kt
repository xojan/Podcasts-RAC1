package cat.xojan.random1.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import cat.xojan.random1.R
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.DaggerHomeComponent
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.injection.module.HomeModule
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.viewmodel.PodcastsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject

class HomeActivity: BaseActivity(), HasComponent<HomeComponent> {
    companion object {
        private val PERMISSION_WRITE_EXTERNAL_STORAGE = 20
    }

    private val TAG = HomeActivity::class.simpleName

    @Inject internal lateinit var mViewModel: PodcastsViewModel
    private val mCompositeDisposable = CompositeDisposable()

    private lateinit var programFragment: ProgramFragment
    private lateinit var downloadsFragment: DownloadsFragment

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
        getProgramFragment().onMediaControllerConnected()
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

        pageAdapter.addFragment(programFragment)
        pageAdapter.addFragment(downloadsFragment)

        viewPager.adapter = pageAdapter
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
        mCompositeDisposable.clear()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, ProgramFragment.TAG, programFragment)
        supportFragmentManager.putFragment(outState, DownloadsFragment.TAG, downloadsFragment)
    }

    private fun exportPodcasts() {
        mCompositeDisposable.add(mViewModel.exportPodcasts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<Boolean> { this.notifyUser(it) }))
    }

    private fun notifyUser(b: Boolean?) {
        Toast.makeText(this, getString(R.string.podcasts_exported), Toast.LENGTH_LONG).show()
    }

    private fun getProgramFragment(): ProgramFragment = pageAdapter.getItem(0) as ProgramFragment
}