package com.example.keephersafeGPStrial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    TextView hearrateTV,avgHRV;
    private BluetoothAdapter mBluetoothAdapter;
    ArrayList<String> phoneNumbers;
    SmsManager smsManager;
    SharedPreferences myPref;
    private Button sendSOS;
    Switch simpleSwitch;
    ArrayList<Double> hrvs;
    double hrv;
    int hrIndex = 0;
    double tot = 0, mean, stdDev, scaleOfElimination = 1.5;


    public void toast(String mess) {
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sendSOS = findViewById(R.id.SOSbutton);
        hearrateTV = findViewById(R.id.hearrateTV);
        avgHRV = findViewById(R.id.avgHRV);
        simpleSwitch = findViewById(R.id.simpleSwitch);

        hrvs = new ArrayList (Collections.nCopies(100, 0.0));



        sendSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendManualSOS();
            }
        });

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent i=new Intent(this,  MyBluetoothService.class);
        startService(i);


        ServiceToActivity serviceReceiver = new ServiceToActivity();
        IntentFilter intentSFilter = new IntentFilter("ServiceToActivityAction");
        registerReceiver(serviceReceiver, intentSFilter);
    }


    public void startBtService(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        Intent i=new Intent(this,  MyBluetoothService.class);
        startService(i);
    }

    public boolean isEmergency(){

        tot = 0;
        for(double d : hrvs){
            tot += d;
        }

        mean = tot / hrvs.size();


        double temp = 0;

        for (double a : hrvs) {
            temp += (a - mean) * (a - mean);
        }

        stdDev = Math.sqrt(temp / (hrvs.size() - 1));


        final List<Integer> newList = new ArrayList<>();


        boolean isLessThanLowerBound = hrv < (mean - stdDev * scaleOfElimination);
        boolean isGreaterThanUpperBound = hrv > (mean + stdDev * scaleOfElimination);
        boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

//        return !(hrv >=63 && hrv <= 87);
        return isOutOfBounds;
    }

    public class ServiceToActivity extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle notificationData = intent.getExtras();
            String newData  = notificationData.getString("ServiceToActivityKey");

            // newData is from the service

            String[]  data = newData.split(";");

            if(data[0].equals("datapoint")){
                hrv = Double.parseDouble(data[1]);
                hearrateTV.setText("Heart Rate Detected " + hrv);

                if(isEmergency() && !simpleSwitch.isChecked()){
                    sendManualSOS();
                }

                if(simpleSwitch.isChecked())
                {
                    hrvs.set(hrIndex , Double.parseDouble(data[1]));
                    hrIndex = (hrIndex + 1) % hrvs.size();

                    avgHRV.setText(
                             hrvs.toString() + "\n\n" +
                            "Average Heart Rate = " + mean  + " \nStdDev =" + stdDev + "\n"
                            + "LowerBound = " + (mean - stdDev * scaleOfElimination) + "\n"
                            + "UpperBound = " + (mean + stdDev * scaleOfElimination) + "\n"
                    );

                }
            }



        }
    }


    public void startService(View v){

        Intent serviceIntent  = new Intent(this,LocationService.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }
    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return super.checkPermission(permission, pid, uid);
    }
    public void sendManualSOS() {
        myPref = getSharedPreferences("MySharedPref",MODE_PRIVATE);

        smsManager = SmsManager.getDefault();
        phoneNumbers = new ArrayList<>();
        phoneNumbers.add("9246465080");
        for(int i = 0;i < phoneNumbers.size();i++){
            String tosend = "HELP NEEDED!  "+ hrv+ "\n http://www.google.com/maps/place/" +  myPref.getString("Latitude","0.00") + "," + myPref.getString("Longitude","0.00");
            toast(tosend);

            smsManager.sendTextMessage(phoneNumbers.get(i),null,"HELP NEEDED!  "+ hrv+ "\n http://www.google.com/maps/place/" + myPref.getString("Latitude","0.00") + "," + myPref.getString("Longitude","0.00"),null,null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Intent i=new Intent(this,  MyBluetoothService.class);
//        stopService(i);

    }
}