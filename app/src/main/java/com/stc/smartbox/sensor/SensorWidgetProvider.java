package com.stc.smartbox.sensor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.stc.smartbox.sensor.data.SensorData;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.stc.smartbox.sensor.RetrofitHelper.MY_CHANNEL;

public class SensorWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "SensorWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "onUpdate() called with: context = [" + context + "], appWidgetManager = [" + appWidgetManager + "], appWidgetIds = [" + appWidgetIds + "]");

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                SensorWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            // Register an onClickListener
            Intent intent = new Intent(context, SensorWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
            update(remoteViews, widgetId, appWidgetManager);
        }
    }

    private String getData() {
        return null;
    }
    private void update(final RemoteViews remoteViews, final  int widgetId, final AppWidgetManager appWidgetManager) {
        remoteViews.setTextViewText(R.id.update, "updating...");
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
        RetrofitHelper.getInstance().getData(MY_CHANNEL, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SensorData>() {
                    @Override
                    public void call(SensorData sensorData) {
                        String text = sensorData.getFeeds().get(0).getField1()+"°C ";
                        text+="\n"+sensorData.getFeeds().get(0).getField2()+"%";
                        Log.w(TAG, "call: " + text);
                        remoteViews.setTextViewText(R.id.update, text);
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call: ",throwable );

                        remoteViews.setTextViewText(R.id.update, "ERROR \n no DATA");
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);

                    }
                });
    }
}