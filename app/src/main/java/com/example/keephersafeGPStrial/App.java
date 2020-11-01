package com.example.keephersafeGPStrial;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Handler;

public class App extends Application {
    public static final String CHANNEL_ID = "gpsService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,"GPS Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    Handler.Callback realCallback = null;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (realCallback != null) {
                realCallback.handleMessage(msg);
            }
        };
    };
    public Handler getHandler() {
        return handler;
    }
    public void setCallBack(Handler.Callback callback) {
        this.realCallback = callback;
    }
}
