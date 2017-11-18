package cat.xojan.random1.ui;

import android.support.v4.app.Fragment;

import cat.xojan.random1.injection.HasComponent;

public abstract class BaseFragment extends Fragment {

    /**
     * Gets a component for dependency injection by its type.
     */
    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }

    /**
     * Override for custom support for back button click event.
     */
    public boolean handleOnBackPressed() {
        return false;
    }
}
