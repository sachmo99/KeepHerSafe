package com.example.keephersafeGPStrial;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText mTextUsername;
    EditText mTextPassword;
    EditText mTextHeight;
    EditText mTextWeight;
    EditText mTextAge;
    EditText mTextEmg1;
    EditText mTextEmg2;

    Button mButtonRegister;
    TextView mTextViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        mTextUsername = (EditText)findViewById(R.id.edittext_username);
        mTextPassword = (EditText)findViewById(R.id.edittext_password);
        mTextHeight = (EditText)findViewById(R.id.edit_text_height);
        mTextWeight = (EditText)findViewById(R.id.edit_text_weight);
        mTextAge = (EditText)findViewById(R.id.edit_text_age);
        mTextEmg1 = (EditText)findViewById(R.id.edit_text_emg1);
        mTextEmg2 = (EditText)findViewById(R.id.edit_text_emg2);
        mButtonRegister = (Button)findViewById(R.id.buttonRegister);
        mTextViewLogin = (TextView)findViewById(R.id.textviewLogin);
        mTextViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(LoginIntent);
            }
        });

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = mTextUsername.getText().toString().trim();
                String pwd = mTextPassword.getText().toString().trim();
                String height = mTextHeight.getText().toString().trim();
                String weight = mTextHeight.getText().toString().trim();
                String age = mTextAge.getText().toString().trim();
                String emg1 = mTextEmg1.getText().toString().trim();
                String emg2 = mTextEmg2.getText().toString().trim();

                long val = db.addUser(user,pwd,height,weight,emg1,emg2,age);
                if(val > 0){
                    Toast.makeText(RegisterActivity.this,"You have registered",Toast.LENGTH_SHORT).show();
                    Intent moveToLogin = new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(moveToLogin);
                }
                else{
                    Toast.makeText(RegisterActivity.this,"Registeration Error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
