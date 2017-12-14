package com.stc.smartbox.sensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DataManipulator dataManipulator;
    private TextView textViewSensorData, textViewTodayData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewSensorData=findViewById(R.id.text_sensor_data);
        textViewTodayData=findViewById(R.id.text_today_data);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGraph();
            }
        });
        dataManipulator=new DataManipulator();

    }

    private void showGraph() {
        startActivity(new Intent(this,CombinedChartActivity.class));
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


}
