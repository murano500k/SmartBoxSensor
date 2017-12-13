package com.stc.smartbox.sensor;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.stc.smartbox.sensor.data.SensorData;

import org.junit.Test;
import org.junit.runner.RunWith;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.stc.smartbox.sensor.RetrofitHelper.MY_CHANNEL;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        RetrofitHelper retrofitHelper =new RetrofitHelper();

        retrofitHelper.getData(MY_CHANNEL, 1).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SensorData>() {
                    @Override
                    public void call(SensorData sensorData) {
                        String text = sensorData.getFeeds().get(0).getField1()+" °C";
                        Log.w(TAG, "call: "+text );
                    }
                });
        assertEquals("com.stc.smartbox.sensor", appContext.getPackageName());

    }
}
