package com.example.keephersafeGPStrial;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import static com.example.keephersafeGPStrial.App.CHANNEL_ID;

public class LocationService extends Service {
    private FusedLocationProviderClient locationProviderClient;
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private LocationListener mLocationListener;
    private LocationManager mlocationManager;
    private NotificationManager notificationManager;
    private DBManager dbManager;
    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 10;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    public static final String B_DEVICE = "MY DEVICE";
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    //private ConnectThread mConnectThread;
    //private static ConnectedThread mConnectedThread;
    private static Handler mHandler = null;
    public static int mState = STATE_NONE;
    public static String deviceName;
    public static BluetoothDevice sDevice = null;
    public Vector<Byte> packData = new Vector<>(2048);
    //private final IBinder mBinder = new LocationServiceBinder();
    private String macAddress;
    FirebaseDatabase database;
    DatabaseReference myRef;


    SharedPreferences latlongsaves;

    public LocationService() {
    }

    public void toast(String mess) {
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initializeLocationManager();
        Log.d("LocationService", "Service started");
        dbManager = DBManager.getInstance(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(mBluetoothAdapter == null) {
//            Toast.makeText(this,"Device doesnt support bluetooth!",Toast.LENGTH_LONG).show();
//            //stopSelf();
//            return;
//        }
//        if(!mBluetoothAdapter.isEnabled()){
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Keeping You Safe.. ")

                    .setSmallIcon(R.drawable.ic_android)
                    .build();

            startForeground(1, notification);
        }
        startTracking();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("datapoints");


        /*Date curr = Calendar.getInstance().getTime();
        String input;
        if (intent == null) {
            input = "new intent called onStartCommand";
        } else {
            input = intent.getStringExtra("inputExtra");
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Keeping You Safe.. ")
                .setContentText("Latitude:" + Double.valueOf(mLocationListener.mLastLocation.getLatitude()))
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);*/
        Log.d("LocationService", "onStartCommand: called");


            getLocation();
            String deviceg = intent.getStringExtra("bluetooth_device");

            return START_NOT_STICKY;
        }
        private synchronized void connectToDevice (String macAddress){
            BluetoothDevice myPC = mBluetoothAdapter.getRemoteDevice(macAddress);
            Log.d("ConnectToDevice:", myPC.getName() + ":" + myPC.getBluetoothClass());
            BluetoothSocket btSocket = null;
            int counter = 0;
            do {
                try {
                    btSocket = myPC.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    Log.d("ConnectToDevice:", "||" + btSocket);
                    btSocket.connect();
                    Log.d("ConnectedToDevice:", "||" + btSocket.isConnected());


                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter++;
            } while (!btSocket.isConnected() && counter < 3);
            if (counter >= 3) {
                //toast("Unable to connect to BL");
                Log.d("BluetoothFailure:", "cannot connect to the device");
            }


        }
    /*private synchronized void connectToDevice(String macAddress){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if(mState == STATE_CONNECTING){
            if(mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new PrinterService.ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }
    private void setState(int state){
        LocationService.mState = state;
        if(mHandler!=null) {
            mHandler.obtainMessage(AbstractActivity.MESSAGE_STATE_CHANGE,state,-1).sendToTarget();
        }
    }
    public synchronized void stop() {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }
    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }
    private void connectionFailed() {
        LocationService.this.stop();
        Message msg = mHandler.obtainMessage(AbstractActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AbstractActivity.TOAST, getString(R.string.error_connect_failed));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        LocationService.this.stop();
        Message msg = mHandler.obtainMessage(AbstractActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AbstractActivity.TOAST, getString(R.string.error_connect_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    private static Object obj = new Object();

    public static void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (obj) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new PrinterService.ConnectedThread(mmSocket);
        mConnectedThread.start();

        // Message msg =
        // mHandler.obtainMessage(AbstractActivity.MESSAGE_DEVICE_NAME);
        // Bundle bundle = new Bundle();
        // bundle.putString(AbstractActivity.DEVICE_NAME, "p25");
        // msg.setData(bundle);
        // mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }*/

        private void getLocation () {
            LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
            mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequestHighAccuracy.setInterval(4000);
            mLocationRequestHighAccuracy.setFastestInterval(2000);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Location Service", "getLocation: stopping the location service");
                stopSelf();
                return;
            }
            Log.d("LocationService", "getLocation: getting Location information.");
            locationProviderClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.d("LocationService", "onLocationResult: got location result");
                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                        EntityModel model = new EntityModel();
                        model.prediction = 1;
                        model.decision = 1;
                        model.id = new Random().nextInt(1000);
                        model.latitude = location.getLatitude();
                        model.longitude = location.getLongitude();
                        latlongsaves = getApplicationContext().getSharedPreferences("MySharedPref",MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = latlongsaves.edit();
                        myEdit.putString("Latitude",String.valueOf(model.latitude));
                        myEdit.putString("Longitude",String.valueOf(model.longitude));
                        myEdit.commit();
                        model.pulse = new Random().nextInt(60) + 60;
                        if(model.pulse < 65 || model.pulse>100){
                            startCoolDown(model);
                        }else {

                            String id = myRef.push().getKey();
                            myRef.child(id).setValue(model);
                            dbManager.addDataPoint(model);
                            Log.d("INSERTING NEW DATAPOINT:", model.pulse + "||" + model.latitude + "||" + model.longitude);

                            Toast.makeText(getApplicationContext(), String.valueOf(dbManager.getAllPointsCount()), Toast.LENGTH_LONG).show();
                        }

                        //User user = ((UserClient)(getApplicationContext())).getUser();
                        //GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        // UserLocation userLocation = new UserLocation(user, geoPoint, null);
                        //saveUserLocation(userLocation);
                    }
                    //super.onLocationResult(locationResult);
                }
            }, Looper.myLooper());
        }
//    private void saveUserLocation(final UserLocation userLocation){
//
//        try{
//            DocumentReference locationRef = FirebaseFirestore.getInstance()
//                    .collection(getString(R.string.collection_user_locations))
//                    .document(FirebaseAuth.getInstance().getUid());
//
//            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        Log.d(TAG, "onComplete: \ninserted user location into database." +
//                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
//                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
//                    }
//                }
//            });
//        }catch (NullPointerException e){
//            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
//            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
//            stopSelf();
//        }
//
//    }

        public synchronized void  startCoolDown(EntityModel model) {
            Log.d("LocationService","CoolDown Activated");
            Intent intent = new Intent(this,DangerTrigger.class);
            intent.putExtra("DataPoint",model);
            PendingIntent inte = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 26) {
                String CHANNEL_ID = "my_channel_02";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "My Channel_2",
                        NotificationManager.IMPORTANCE_HIGH);

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentIntent(inte)
                        .setContentTitle("CoolDown Mode Activated ")
                        .setContentText("Abnormal HeartRate detected.. Please click here to terminate SOS")
                        .setSmallIcon(R.drawable.ic_baseline_warning_24)
                        .build();

                startForeground(2, notification);

            }

        }


        @Override
        public void onDestroy () {
            super.onDestroy();
            if (mlocationManager != null) {
                try {
                    mlocationManager.removeUpdates(mLocationListener);
                } catch (Exception e) {
                    Log.i(TAG, "failed to remove location listener, ignore ", e);
                }
            }
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }

        private void initializeLocationManager () {
            if (mlocationManager == null) {
                mlocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }
        }

        public void startTracking () {
            initializeLocationManager();
            mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

            try {

                mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
            } catch (SecurityException e) {

            } catch (IllegalArgumentException e) {

            }
        }
        public void stopTracking () {
            onDestroy();
        }

        @Override
        public IBinder onBind (Intent intent){
            // TODO: Return the communication channel to the service.
            //throw new UnsupportedOperationException("Not yet implemented");
            mHandler = ((App) getApplication()).getHandler();
            return binder;
        }

        private class LocationListener implements android.location.LocationListener {
            private Location lastLocation = null;
            private final String TAG = "LocationListener";
            private Location mLastLocation;

            public LocationListener(String provider) {
                mLastLocation = new Location(provider);
            }

            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
                Log.i(TAG, "LocationChanged:" + location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.e(TAG, "onStatusChanged: " + s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.e(TAG, "onProviderEnabled: " + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.e(TAG, "onProviderDisabled: " + s);

            }
        }
        public class LocationServiceBinder extends Binder {
            public LocationService getService() {
                return LocationService.this;
            }
        }

   /* private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;

            }
            synchronized (LocationService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("LocationService", "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Printer Service", "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (!encodeData(mmInStream)) {
                        mState = STATE_NONE;
                        connectionLost();
                        break;
                    } else {
                    }
                    // mHandler.obtainMessage(AbstractActivity.MESSAGE_READ,
                    // bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
                    LocationService.this.stop();
                    break;
                }

            }
        }

        private byte[] btBuff;


        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(AbstractActivity.MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("LocationService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
                Log.e("LocationService", "close() of connect socket failed", e);
            }
        }

    }
    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {//
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 3:

                        break;

                    case 4:

                        break;
                    case 5:
                        break;

                    case -1:
                        break;
                }
            }
            super.handleMessage(msg);
        }

    };*/


    }

