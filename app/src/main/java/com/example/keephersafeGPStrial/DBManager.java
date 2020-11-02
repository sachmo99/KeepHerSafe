package com.example.keephersafeGPStrial;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class DBManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "KeepHerSafe";
    private static  final String TABLE_NAME =  "location_pulse_tracker";
    private static final String KEY_TRACKER_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_PULSE = "pulse";
    private static final String KEY_DECISION = "decision";
    private static final String KEY_PREDICTION = "prediction";
    private static DBManager sInstance;
    private Context context;


    public static synchronized DBManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public DBManager(Context c){
        super(c,DATABASE_NAME,null,1);
        context = c;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Toast.makeText(context,"DB TABLE CREATED!",Toast.LENGTH_SHORT).show();
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + KEY_TRACKER_ID + " INTEGER PRIMARY KEY,"+ KEY_LATITUDE + " REAL," + KEY_LONGITUDE + " REAL," + KEY_PULSE + " REAL," + KEY_PREDICTION + " INTEGER," + KEY_DECISION + " INTEGER);";
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            //db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }

    }

    public long addDataPoint(EntityModel model) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_LATITUDE,model.latitude);
            values.put(KEY_LONGITUDE,model.longitude);
            values.put(KEY_PULSE,model.pulse);
            values.put(KEY_PREDICTION,model.prediction);
            values.put(KEY_DECISION,model.decision);
            id = db.insertOrThrow(TABLE_NAME,null,values);
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d("DBManager class","Error while entering Data");
        }finally {
            db.endTransaction();
        }
        return id;
    }

    public int getAllPointsCount(){
        List<EntityModel> datapoints  = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";",null);
        try {
            if(cursor.moveToFirst()){
                do {
                    EntityModel model = new EntityModel();
                    model.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
                    model.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
                    model.pulse = cursor.getInt(cursor.getColumnIndex(KEY_PULSE));
                    model.prediction = cursor.getInt(cursor.getColumnIndex(KEY_PREDICTION));
                    model.decision = cursor.getInt(cursor.getColumnIndex(KEY_LATITUDE));
                    model.id = cursor.getInt(cursor.getColumnIndex(KEY_TRACKER_ID));
                    datapoints.add(model);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d("FETCH FROM DB","Error while retrieving the data points");
        }finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return datapoints.size();
    }
    /*
    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        super(c,);
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(int id, String latitude, String longitude) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.ID, id);
        contentValue.put(DatabaseHelper.LAT, latitude);
        contentValue.put(DatabaseHelper.LON, longitude);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        contentValue.put(DatabaseHelper.TIME, currentDateandTime);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public int fetch() {
        String[] columns = new String[] { DatabaseHelper.ID, DatabaseHelper.TIME, DatabaseHelper.LAT, DatabaseHelper.LON };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor.getCount();
    }

    public void clear() {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.ID + "=" + DatabaseHelper.ID, null);
    }
    */

}
