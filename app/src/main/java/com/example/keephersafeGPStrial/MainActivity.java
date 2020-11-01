package com.example.keephersafeGPStrial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private BluetoothAdapter mBluetoothAdapter;
    ArrayList<String> phoneNumbers;
    SmsManager smsManager;
    SharedPreferences myPref;
    private Button sendSOS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.edit_text_input);
        sendSOS = findViewById(R.id.SOSbutton);
        sendSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendManualSOS();
            }
        });
//        if(mBluetoothAdapter == null) {
//            Toast.makeText(this,"Device doesnt support bluetooth!",Toast.LENGTH_LONG).show();
//            //stopSelf();
//            return;
//        }
//        if(!mBluetoothAdapter.isEnabled()){
//
//        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent,1);



    }
    public void startService(View v){
        String input = editText.getText().toString();


        Intent serviceIntent  = new Intent(this,LocationService.class);
        serviceIntent.putExtra("inputExtra",input);
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
        phoneNumbers.add("9491212790");
        phoneNumbers.add("8374012004");
        phoneNumbers.add("9985302688");
        for(int i = 0;i < phoneNumbers.size();i++){
            smsManager.sendTextMessage(phoneNumbers.get(i),null,"HELP NEEDED!Location at LAT:" + myPref.getString("Latitude","0.00") + " Longitude:" + myPref.getString("Longitude","0.00"),null,null);
        }

    }
}