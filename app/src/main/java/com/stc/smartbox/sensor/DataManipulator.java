package com.stc.smartbox.sensor;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.stc.smartbox.sensor.data.Feed;
import com.stc.smartbox.sensor.data.SensorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_HUMIDITY;
import static com.stc.smartbox.sensor.RetrofitHelper.FIELD_TEMP;

/**
 * Created by artem on 12/13/17.
 */

public class DataManipulator {
    private static final String TAG = "DataManipulator";

    public enum VALUE_TYPE{
        MIN,
        MAX,
    }
    private static RetrofitHelper retrofitHelper;
    public List<Feed> feeds;
    public DataManipulator(){
        retrofitHelper =new RetrofitHelper();
    }


    public RetrofitHelper getRetrofit() {
        return retrofitHelper;
    }

    public Observable<List<Feed>> getDataForPeriod(final Date from, final Date to){
        if(feeds==null) {
            return retrofitHelper.getData(RetrofitHelper.MY_CHANNEL, 500
            )
                    .flatMap(new Func1<SensorData, Observable<List<Feed>>>() {


                        @Override
                        public Observable<List<Feed>> call(SensorData sensorData) {
                            feeds = sensorData.getFeeds();
                            Log.d(TAG, "geDataForPeriod size: "+feeds.size());
                            List<Feed> result = new ArrayList<>();

                            for (Feed feed : sensorData.getFeeds()) {
                                if (feed.getCreatedAt().after(from) &&
                                        feed.getCreatedAt().before(to)) {
                                    result.add(feed);
                                }
                            }
                            return Observable.just(result);
                        }
                    });
        }else {
            return Observable.just(feeds);
        }
    }
    Comparator<Feed> getComparator(final int field){
        return new Comparator<Feed>() {
            @Override
            public int compare(Feed o1, Feed o2) {
                float v1,v2;
                if(field==FIELD_TEMP){
                    v1=o1.getField1();
                    v2=o2.getField1();
                }else {
                    v1=o1.getField2();
                    v2=o2.getField2();
                }
                if(v1>v2)return 1;
                else if(v1==v2) return 0;
                else return -1;
            }
        };
    }

    public Feed getValueForPeriod(List<Feed> feeds, final VALUE_TYPE valueType, final int field){

        Feed result=null;
        switch (valueType){
            case MAX:
                result=Collections.max(feeds, getComparator(field));
                break;
            case MIN:
                result=Collections.min(feeds, getComparator(field));

                break;
            default:
                Log.e(TAG, "call: INCORRECT VAL");
        }
        return result;
    }

    public Float getAvg(List<Feed>feeds,int field){
        Float sum=0f;
        for(Feed f: feeds){
            if (field==1) sum+=f.getField1();
            else sum+=f.getField2();
        }
        return sum/feeds.size();
    }


    public Observable<List<Entry>> getEntriesForPeriod(final Date from, final Date to, final int field){
        return getDataForPeriod(from, to).flatMap(new Func1<List<Feed>, Observable<List<Entry>>>() {
            @Override
            public Observable<List<Entry>> call(List<Feed> feeds) {
                List<Entry> entries = new ArrayList<>();
                for (Feed f :
                        feeds) {
                    if(f.getEntryId()%10!=0)continue;
                    float val = -1;
                    if (field == FIELD_TEMP) val = f.getField1();
                    if (field == FIELD_HUMIDITY) val = f.getField2();
                    entries.add(new Entry(f.getCreatedAt().getTime(), val));

                }
                return Observable.just(entries);
            }
        });
    }
}
