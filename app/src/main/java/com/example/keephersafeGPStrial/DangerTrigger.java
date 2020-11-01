package com.example.keephersafeGPStrial;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DangerTrigger extends AppCompatActivity {
    Button cooldown;
    TextView forewarning;
    Handler h;
    boolean flag = false;
    ArrayList<String> phoneNumbers;
    SmsManager smsManager;
    SharedPreferences myPref;
    DBManager dbManager;
    EntityModel model;
    FirebaseDatabase firebase;
    DatabaseReference dref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangertrigger_layout);

        Bundle data = getIntent().getExtras();
        model = (EntityModel) data.getParcelable("DataPoint");
        Log.d("DangerTrigger:OnCreate",model.pulse + " pulse received from intent");
        cooldown = findViewById(R.id.cooldown_btn);
        forewarning = findViewById(R.id.forewarning);
        dbManager = DBManager.getInstance(this);
        firebase = FirebaseDatabase.getInstance();
        dref = firebase.getReference("datapoints");
        cooldown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTheAlert();
            }
        });



        startNewService();

    }

    public void startNewService(){

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                sendSOS();
            }
        };
        Handler h = new Handler();

        if(flag == false){
            try {
                h.postDelayed(r, 15000);
            }catch(Exception e){
                e.printStackTrace();
            }
            //sendSOS();

        }else{
            //h.removeCallbacksAndMessages(null);
        }


    }
    public void stopTheAlert() {
        h.removeCallbacksAndMessages(null);
        model.decision = 0;
        dbManager.addDataPoint(model);
        Log.d("INSERTING NEW DATAPOINT_STOP:", model.pulse + "||" + model.latitude + "||" + model.longitude + "||" + model.decision);
        String id = dref.push().getKey();
        dref.child(id).setValue(model);
        flag = true;
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);

    }
    public void sendSOS() {
        if(flag == true){
            Log.d("DangerTriggerClass","SOS stopped");
            return;
        }
        myPref = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        model.decision = 1;
        dbManager.addDataPoint(model);
        Log.d("INSERTING NEW DATAPOINT_SOS:", model.pulse + "||" + model.latitude + "||" + model.longitude+"||" + model.decision);
        String id = dref.push().getKey();
        dref.child(id).setValue(model);
        myPref = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        smsManager = SmsManager.getDefault();
        phoneNumbers = new ArrayList<>();
//        phoneNumbers.add("");
//        phoneNumbers.add("");
//        phoneNumbers.add("");
        phoneNumbers.add("9491212790");
        for(int i = 0;i < phoneNumbers.size();i++){
            smsManager.sendTextMessage(phoneNumbers.get(i),null,"HELP NEEDED! http://www.google.com/maps/place/" + myPref.getString("Latitude","0.00") + "," + myPref.getString("Longitude","0.00"),null,null);
        }

    }

}
