package com.useriq.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.useriq.sdk.capture.Capture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void getScreenshot() throws Exception {
        Capture.Info info = Capture.from(_activityRule.getActivity(), true);
        assertNotNull(info.image);
    }

    @Rule
    public ActivityTestRule<MainActivity> _activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void isFabAttached() throws Exception {
        Capture.Info info = Capture.from(_activityRule.getActivity(), true);
        assertNotNull(info.image);
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.useriq.demo", appContext.getPackageName());
    }

}
