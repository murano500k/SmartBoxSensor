package com.stc.smartbox.sensor;

import android.util.Log;

import com.stc.smartbox.sensor.data.SensorData;

import org.junit.Test;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;
import static com.stc.smartbox.sensor.RetrofitHelper.MY_CHANNEL;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {


        RetrofitHelper retrofitHelper =new RetrofitHelper();

        retrofitHelper.getData(MY_CHANNEL, 1).observeOn(Schedulers.newThread())
                .subscribe(new Action1<SensorData>() {
                    @Override
                    public void call(SensorData sensorData) {
                        String text = sensorData.getFeeds().get(0).getField1()+" °C";

                        System.out.println("call: "+text );
                        assertEquals(4, 2 + 2);
                        Log.w(TAG, "call: "+text );
                    }
                });



    }
}