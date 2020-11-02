package com.example.keephersafeGPStrial;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText mTextUsername;
    EditText mTextPassword;
    Button mButtonLogin, mButtonRegister;
    TextView mTextViewRegister;
    DatabaseHelper db;
    ViewGroup progressView;
    protected boolean isProgressShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        mTextUsername = (EditText)findViewById(R.id.edittext_username);
        mTextPassword = (EditText)findViewById(R.id.edittext_password);
        mButtonLogin = (Button)findViewById(R.id.button_login);
        mButtonRegister = (Button)findViewById(R.id.buttonRegister);
//        mTextViewRegister = (TextView)findViewById(R.id.textview_register);
    }

    public void registerIndent(View view) {
        Intent registerIntent = new Intent(this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    public void loginIntent(View view) {
        String user = mTextUsername.getText().toString().trim();
        String pwd = mTextPassword.getText().toString().trim();
//        Boolean res = db.checkUser(user, pwd);
        Boolean res = true;
        if(res == true)
        {
            Intent HomePage = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(HomePage);
        }
        else
        {
            Toast.makeText(LoginActivity.this,"Login Error",Toast.LENGTH_SHORT).show();
        }
    }
}
