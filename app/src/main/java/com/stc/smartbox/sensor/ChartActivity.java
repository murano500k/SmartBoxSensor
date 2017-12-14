package com.stc.smartbox.sensor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_HUMIDITY;
import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_TEMP;

public class ChartActivity extends AppCompatActivity {
    private static final String TAG = "ChartActivity";
    LineChart chart ;
    DataManipulator dataManipulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        dataManipulator=new DataManipulator();
        chart = (LineChart) findViewById(R.id.chart);
        chart.getXAxis().setValueFormatter(new DayAxisValueFormatter(chart));
        updateValues();
    }



    private void updateValues() {
        final Date today=new Date();
        long dayMillis=60*60*24*1000;
        Date yesterday = new Date(today.getTime()-dayMillis);

        dataManipulator.getEntriesForPeriod(yesterday,today, FIELD_TEMP)
                .observeOn(AndroidSchedulers.mainThread())
                .zipWith(dataManipulator.getEntriesForPeriod(yesterday, today, FIELD_HUMIDITY), new Func2<List<Entry>, List<Entry>, Integer>() {
                    @Override
                    public Integer call(List<Entry> entries, List<Entry> entries2) {
                        Log.d(TAG, "call: entries="+entries.size());
                        LineData lineData = new LineData();
                        lineData.addDataSet(new LineDataSet(entries, "Temperature"));
                        lineData.addDataSet(new LineDataSet(entries2, "Humidity"));
                        chart.setData(lineData);

                        return 1;
                    }
                }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if(integer>0) chart.invalidate();
            }
        });


    }


}
