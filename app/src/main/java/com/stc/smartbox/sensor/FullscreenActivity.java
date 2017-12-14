package com.stc.smartbox.sensor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.stc.smartbox.sensor.data.Feed;
import com.stc.smartbox.sensor.data.SensorData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_HUMIDITY;
import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_TEMP;
import static com.stc.smartbox.sensor.RetrofitHelper.MY_CHANNEL;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private static final String TAG = "FullscreenActivity";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            textViewSensorData.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    TextView textViewSensorData;
    DataManipulator dataManipulator;
    TextView textViewTodayData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        textViewSensorData=findViewById(R.id.text_sensor_data);
        textViewTodayData=findViewById(R.id.text_today_data);


        // Set up the user interaction to manually show or hide the system UI.
        textViewSensorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.dummy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGraph();
            }
        });
        dataManipulator=new DataManipulator();
    }

    private void showGraph() {
        startActivity(new Intent(this,ChartActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        textViewSensorData.setText("updating...");
        dataManipulator.getRetrofit().getData(MY_CHANNEL, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SensorData>() {
                    @Override
                    public void call(SensorData sensorData) {
                        int results_size=sensorData.getFeeds().size();
                        Log.d(TAG, "results_size: "+results_size);
                        String text = sensorData.getFeeds().get(0).getField1()+"°C ";
                        text+="\n"+sensorData.getFeeds().get(0).getField2()+"%";
                        Log.w(TAG, "call: " + text);
                        textViewSensorData.setText(text);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call: ",throwable );

                        textViewSensorData.setText("ERROR \n no DATA");
                    }
                });
        updateValuesFor1Day();
    }

    private void updateValuesFor1Day() {
        final Date today=new Date();
        long dayMillis=60*60*24*1000;
        Date yesterday = new Date(today.getTime()-dayMillis);

        textViewTodayData.setText("loading...");
        dataManipulator.getDataForPeriod(yesterday,today)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Feed>>() {
                    @Override
                    public void call(List<Feed> feeds) {
                        String minTemp, maxTemp;
                        String minHumidity, maxHumidity;
                        String avgTemp, avgHumidity;

                        SimpleDateFormat format=new SimpleDateFormat("HH:mm");

                        Feed feed =dataManipulator.getValueForPeriod(feeds, DataManipulator.VALUE_TYPE.MIN, FIELD_TEMP);
                        minTemp = feed.getField1()+" ("+format.format(feed.getCreatedAt())+")";

                        feed =dataManipulator.getValueForPeriod(feeds, DataManipulator.VALUE_TYPE.MAX, FIELD_TEMP);
                        maxTemp = feed.getField1()+" ("+format.format(feed.getCreatedAt())+")";

                        avgTemp = dataManipulator.getAvg(feeds, FIELD_TEMP).toString();

                        feed =dataManipulator.getValueForPeriod(feeds, DataManipulator.VALUE_TYPE.MIN, FIELD_HUMIDITY);

                        minHumidity = feed.getField2()+" ("+format.format(feed.getCreatedAt())+")";

                        feed =dataManipulator.getValueForPeriod(feeds, DataManipulator.VALUE_TYPE.MAX, FIELD_HUMIDITY);
                        maxHumidity = feed.getField2()+" ("+format.format(feed.getCreatedAt())+")";

                        avgHumidity = dataManipulator.getAvg(feeds, FIELD_HUMIDITY).toString();

                        String todayString="";
                        todayString+="Temp:\nmin("+minTemp+")\nmax("+maxTemp+")\navg("+avgTemp+")\n\n";
                        todayString+="Humidity:\nmin("+minHumidity+")\nmax("+maxHumidity+")\navg("+avgHumidity+")";
                        textViewTodayData.setText(todayString);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call: ", throwable);
                    }
                });



    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        textViewSensorData.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
