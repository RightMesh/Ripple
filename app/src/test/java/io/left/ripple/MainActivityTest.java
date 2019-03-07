package io.left.ripple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.left.ripple.views.CustomViewRightMeshRecipient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class,
        sdk = 23)
public class MainActivityTest extends AndroidTest<MainActivity> {

    private Button buttonRed;
    private Button buttonGreen;
    private Button buttonBlue;
    private View layoutBackground;

    @Override
    protected Class<MainActivity> getActivityClass() {
        return MainActivity.class;
    }

    /**
     * Run activity and find view id.
     */
    @Before
    public void setUp() {
        super.setUp();
        runActivity();

        layoutBackground = findViewById(R.id.layout_background);
        buttonRed = findViewById(R.id.button_red);
        buttonBlue = findViewById(R.id.button_blue);
        buttonGreen = findViewById(R.id.button_green);
    }

    /**
     * Check if a view background color matches with {@link Colour}.
     *
     * @param view   View wanted to check
     * @param colour Value of {@link Colour}
     * @return match?
     */
    private boolean hasBackgroundColor(@NonNull View view, @NonNull Colour colour) {
        return ((ColorDrawable) view.getBackground()).getColor()
                == ContextCompat.getColor(activity, colour.getColourId());
    }

    @Test
    public void displayBackgroundColor() {
        buttonRed.callOnClick();
        assertTrue(hasBackgroundColor(layoutBackground, Colour.RED));

        buttonBlue.callOnClick();
        assertTrue(hasBackgroundColor(layoutBackground, Colour.BLUE));

        buttonGreen.callOnClick();
        assertTrue(hasBackgroundColor(layoutBackground, Colour.GREEN));
    }
}
