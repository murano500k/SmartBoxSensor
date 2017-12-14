
package com.stc.smartbox.sensor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_HUMIDITY;
import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_TEMP;

public class CombinedChartActivity extends DemoBase {
    private static final String TAG = "CombinedChartActivity";
    private CombinedChart mChart;
    private final int itemcount = 12;
    private DataManipulator dataManipulator;
    private XAxis xAxis;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManipulator=new DataManipulator();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_combined);

        progressBar=findViewById(R.id.progress);
        mChart = (CombinedChart) findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        mChart.setDrawOrder(new DrawOrder[]{
                DrawOrder.LINE,DrawOrder.BAR
        });

        final Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTH_SIDED);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long time = (long)value;
                SimpleDateFormat simpleDateFormat =new SimpleDateFormat("HH:mm");
                String res = simpleDateFormat.format(new Date(time));
                return res;
            }
        });
        updateValues();
    }

    private void updateValues() {
        progressBar.setVisibility(View.VISIBLE);
        final Date today=new Date();
        long dayMillis=60*60*24*1000;
        Date yesterday = new Date(today.getTime()-dayMillis);

        dataManipulator.getEntriesForPeriod(yesterday,today, FIELD_TEMP)
                .observeOn(AndroidSchedulers.mainThread())
                .zipWith(dataManipulator.getEntriesForPeriod(yesterday, today, FIELD_HUMIDITY), new Func2<List<Entry>, List<Entry>, Integer>() {
                    @Override
                    public Integer call(List<Entry> entries, List<Entry> entries2) {
                        Log.d(TAG, "call: entries="+entries.size());
                        CombinedData data = new CombinedData();

                        data.setData(generateLineData(entries, 0));
                        data.setData(generateLineData(entries2, 1));
                        data.setValueTypeface(mTfLight);
                        xAxis.setAxisMaximum(data.getXMax() + 0.25f);
                        mChart.setData(data);
                        return 0;
                    }
                }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                progressBar.setVisibility(View.GONE);

                if(integer==0) mChart.invalidate();
                else Toast.makeText(CombinedChartActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private LineData generateLineData(List<Entry> entries, int id) {

        LineData d = new LineData();
        int c ;
        String l="";
        if(id==0){
            l="Temp";
            c=Color.rgb(240, 238, 70);
        }else {
            l= "Humidity";
            c =Color.rgb(240, 50, 30);
        }

        LineDataSet set = new LineDataSet(entries, l);
        set.setColor(c);
        set.setLineWidth(2.5f);
        set.setCircleColor(c);
        set.setCircleRadius(5f);
        set.setFillColor(c);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(c);

        //if(id==)0set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return d;
    }

    private BarData generateBarData(List<Entry> entries) {
        List<BarEntry>barEntries = new ArrayList<>(entries.size());
        for (Entry e :
                entries) {

            barEntries.add( new BarEntry(e.getX(), e.getY()));
            Log.d(TAG, "generateBarData: "+e.getX()+", "+e.getY());
        }



        BarDataSet set = new BarDataSet(barEntries, "Humidity");
        set.setColor(Color.rgb(60, 220, 78));
        set.setValueTextColor(Color.rgb(60, 220, 78));
        set.setValueTextSize(10f);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setDrawValues(true);
        BarData d = new BarData(set);
        return d;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.combined, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionToggleLineValues: {
                for (IDataSet set : mChart.getData().getDataSets()) {
                    if (set instanceof LineDataSet)
                        set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleBarValues: {
                for (IDataSet set : mChart.getData().getDataSets()) {
                    if (set instanceof BarDataSet)
                        set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart.invalidate();
                break;
            }
            case R.id.actionRemoveDataSet: {

                int rnd = (int) getRandom(mChart.getData().getDataSetCount(), 0);
                mChart.getData().removeDataSet(mChart.getData().getDataSetByIndex(rnd));
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.invalidate();
                break;
            }
        }
        return true;
    }
}
