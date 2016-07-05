package cat.xojan.random1.injection.component;

import cat.xojan.random1.injection.PerActivity;
import cat.xojan.random1.injection.module.BaseActivityModule;
import cat.xojan.random1.injection.module.HomeModule;
import cat.xojan.random1.injection.module.RadioPlayerModule;
import cat.xojan.random1.ui.activity.RadioPlayerActivity;
import cat.xojan.random1.ui.fragment.RadioPlayerFragment;
import dagger.Component;

@PerActivity
@Component(
        dependencies = AppComponent.class,
        modules = {
                BaseActivityModule.class,
                RadioPlayerModule.class
        }
)
public interface RadioPlayerComponent extends BaseActivityComponent {
    void inject(RadioPlayerActivity radioPlayerActivity);
    void inject(RadioPlayerFragment radioPlayerFragment);
}
