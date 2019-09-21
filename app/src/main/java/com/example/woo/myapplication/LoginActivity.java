package com.example.woo.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    public static Activity _LoginActivity;
    protected LinearLayout login_activity;
    protected Button login_sign_up_btn;
    protected Button login_sign_in_btn;
    protected EditText login_email;
    protected EditText login_password;
    protected String Email;
    protected String Password;
    protected CheckBox auto_login_check_box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);
        _LoginActivity = LoginActivity.this;
        login_activity = (LinearLayout) findViewById(R.id.login_layout);
        login_sign_in_btn = (Button) findViewById(R.id.login_page_sign_in);
        login_sign_up_btn = (Button) findViewById(R.id.login_page_sign_up);
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        auto_login_check_box = (CheckBox) findViewById(R.id.auto_login);
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        //First, SharedPreferences don't have any information, and then make key to store value
        //First parameter is key, Second parameter is value(getString)
        //I don't have any value, so you make any key and null value

        Email = auto.getString("AutoEmail", null);
        Password  = auto.getString("AutoPassword", null);
        if(Email != null && Password != null){
            // Check Email and password from DB
            //if(check Email and password from DB){

            if(Email.equals("test") && Password.equals("test")){
                Toast.makeText(getApplicationContext(), Email + "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }
        login_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(login_email.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(login_password.getWindowToken(), 0);
            }
        });
        login_sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check Email and password from DB
                // if(check Email and Password from DB)
                if (login_email.getText().toString().equals("test")) {
                    if (login_password.getText().toString().equals("test")) {
                        if(auto_login_check_box.isChecked()){
                            SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor auto_editor = auto.edit();
                            auto_editor.putString("AutoEmail", login_email.getText().toString());
                            auto_editor.putString("AutoPassword", login_password.getText().toString());
                            auto_editor.commit();
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                    startActivity(intent);
                }

            }
        });
        login_sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

    }


}
