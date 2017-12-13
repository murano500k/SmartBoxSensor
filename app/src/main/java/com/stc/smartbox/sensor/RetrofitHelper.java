package com.stc.smartbox.sensor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stc.smartbox.sensor.data.SensorData;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by artem on 12/13/17.
 */

public class RetrofitHelper {
    public static final String BASE_URL = "https://api.thingspeak.com/";
    Retrofit retrofit;
    ThingspeakEndpointInterface thingspeakEndpointInterface;
    public static final int MY_CHANNEL = 330894;
    public static final int FIELD_TEMP = 1;
    public static final int FIELD_HUMIDITY = 2;
//https://api.thingspeak.com/channels/330894/fields/1.json?results=1
    public void init(){
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxAdapter)
                .build();
        thingspeakEndpointInterface=retrofit.create(ThingspeakEndpointInterface.class);
    }

    public Observable<SensorData> getData(int channel ){
        if(thingspeakEndpointInterface==null)init();
        return thingspeakEndpointInterface.getData(channel);
    }

    public Observable<SensorData> getData(int channel , int results){
        if(thingspeakEndpointInterface==null)init();
        return thingspeakEndpointInterface.getData(channel, results);
    }

    public interface ThingspeakEndpointInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

        @GET("channels/{channel}/feeds.json?results=2")
        Observable<SensorData> getData(@Path("channel") int channel);

        @GET("channels/{channel}/feeds.json")
        Observable<SensorData> getData(@Path("channel") int channel, @Query("results") int results);

    }
}


