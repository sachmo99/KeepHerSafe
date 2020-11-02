package com.example.keephersafeGPStrial;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MyBluetoothService extends Service {


    private final String DEVICE_ADDRESS="00:19:09:11:18:22"; // Replace this by your hc-05/6 's mac address.
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;


    boolean deviceConnected=false;
    boolean stopThread;
    byte buffer[];




    public void toast(String mess) {
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this,"onCreate",
//                Toast.LENGTH_SHORT).show();


    }

    private void sendMessageToActivity(String newData){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityAction");
        broadcastIntent.putExtra("ServiceToActivityKey", newData);
        sendBroadcast(broadcastIntent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags,
                              int startId) {
//        Toast.makeText(this,"onStartCommand",
//                Toast.LENGTH_SHORT).show();

        if(BTinit())
        {
            if(BTconnect())
            {
                Toast.makeText(getApplicationContext(),"My Device Connected",Toast.LENGTH_SHORT).show();

                deviceConnected=true;
                beginListenForData();
//                textView.append("\nConnection Opened!\n");
            }
        }



        return START_STICKY;
       /*
       START_STICKY : Using this return value, if the OS kills our Service it will recreate it
                   but the Intent that was sent to the Service isn’t redelivered.
                   In this way the Service is always running
       START_NOT_STICKY: If the OS kills the Service it won’t recreate it until the client calls
                          explicitly onStart command
        START_REDELIVER_INTENT: It is similar to the START_STICKY and in this case,
                       the Intent will be redelivered to the service.
        */
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
//                                     toast(string);
                                    sendMessageToActivity(string);
//                                 textView.append(string);
//                                 scrollToBottom();
                                }
                            });
                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this,"OnDestroy",
//                Toast.LENGTH_SHORT).show();


//        stopThread = true;
//
//        try {
//            outputStream.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        deviceConnected=false;
//        toast("\nConnection Closed!\n");

    }


    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }



        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
//                    Toast.makeText(getApplicationContext(),iterator.getAddress(),Toast.LENGTH_SHORT).show();
                    device=iterator;

                    found=true;
                    break;
                }
            }
        }
        return found;
    }


    public boolean BTconnect()
    {
        boolean connected=true;

        try {

//            Toast.makeText(getApplicationContext(),"Device UUIDS are \n " + device.getUuids()[0],Toast.LENGTH_LONG).show();

            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();

        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {

            try {
                outputStream=socket.getOutputStream();
                outputStream.write(200);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        return connected;
    }






}