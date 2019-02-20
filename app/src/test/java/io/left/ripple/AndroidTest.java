package io.left.ripple;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.IntegerRes;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class,
        sdk = 23)
public abstract class AndroidTest<T extends Activity> {

    protected Application app;
    protected T activity;

    /**
     * Set up activity and app before each test method.
     */
    @Before
    public void setUp() {
        app = RuntimeEnvironment.application;
        runActivity();
    }

    /**
     * Get class type.
     * @return Activity class type
     */
    protected abstract Class<T> getActivityClass();

    /**
     * Set up activity.
     */
    protected void runActivity() {
        Class<T> classActivity = getActivityClass();
        activity = Robolectric.setupActivity(classActivity);
    }



    protected <V extends View> V findViewById(@IdRes int id) {
        return activity.findViewById(id);
    }

    /**
     * Get String from string resource.
     *
     * @param intRes String Id
     * @return string
     */
    protected String getString(@IntegerRes int intRes) {
        return app.getString(intRes);
    }
}
