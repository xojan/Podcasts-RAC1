package cat.xojan.random1.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import cat.xojan.random1.Application;
import cat.xojan.random1.injection.component.AppComponent;
import cat.xojan.random1.injection.module.BaseActivityModule;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
    }

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return {@link AppComponent}
     */
    protected AppComponent getApplicationComponent() {
        return ((Application)getApplication()).getAppComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     *
     * @return {@link cat.xojan.random1.injection.component.BaseActivityComponent}
     */
    protected BaseActivityModule getActivityModule() {
        return new BaseActivityModule(this);
    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param containerViewId The container view to where add the fragment.
     * @param fragment The fragment to be added.
     */
    public void addFragment(int containerViewId, Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }
}