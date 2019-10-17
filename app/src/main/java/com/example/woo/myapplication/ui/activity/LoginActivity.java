package com.example.woo.myapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;

import com.example.woo.myapplication.data.User;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST_MULTI = 100;
    public static Activity _LoginActivity;
    protected LinearLayout login_activity;
    protected Button login_sign_up_btn;
    protected Button login_sign_in_btn;
    protected EditText login_email;
    protected EditText login_password;
    protected String Email;
    protected String Password;
    protected CheckBox auto_login_check_box;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;

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

        // 하나라도 권한 거부 시, 허용할 때까지 무한으로 물을거임.
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkPermission(permissions);

        if( (MyGlobals.getInstance().getRetrofit() == null) || (MyGlobals.getInstance().getRetrofitExService() ==null) ){
            retrofit = new Retrofit.Builder().baseUrl(MyGlobals.RetrofitExService.URL).addConverterFactory(GsonConverterFactory.create()).build();
            retrofitExService = retrofit.create(MyGlobals.RetrofitExService.class);
            MyGlobals.getInstance().setRetrofit(retrofit);
            MyGlobals.getInstance().setRetrofitExService(retrofitExService);
        }else{
            retrofit = MyGlobals.getInstance().getRetrofit();
            retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        }

        Email = auto.getString("AutoEmail", null);
        Password  = auto.getString("AutoPassword", null);


        if(Email != null && Password != null){
            // Check Email and password from DB
            //if(check Email and password from DB){
            HashMap<String, String> input = new HashMap<>();
            input.put("email", Email);
            input.put("password", Password);
            retrofitExService.postLogin(input).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    System.out.println("onResponse 호출@@@!!!!!!!!!!!!!");
                    User user = response.body();
                    String data = user.getCheck();
                    System.out.println("data : " + data + "@@@@@@@@@@@@@@@@@@@@@@@");
                    Log.d("server key", data);
                    if (data.equals("yes")) {
                        Toast.makeText(getApplicationContext(), Email + "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        MyGlobals.getInstance().setUser(user);
                        startActivity(intent);
                    } else if (data.equals("no")) {
                        Intent intent = new Intent(getApplicationContext(), LoginErrorActivity.class);
                        startActivity(intent);
                    } else if (data.equals("wrong")) {
                        Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                        startActivity(intent);
                    } else if (data.equals("error")) {
                        Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    System.out.println("onFailure 호출@@@!!!!!!!!!!!!!");
                }
            });



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
                String email = login_email.getText().toString();
                String password = login_password.getText().toString();
                if(  (email.equals("")) || (password.equals("")) )
                {
                    Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                    startActivity(intent);

                }
                else {
                    HashMap<String, String> input = new HashMap<>();
                    input.put("email", email);
                    input.put("password", password);

                    retrofitExService.postLogin(input).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            System.out.println("onResponse 호출@@@!!!!!!!!!!!!!");
                            User user = response.body();
                            String data = user.getCheck();
                            System.out.println("data : " + data + "@@@@@@@@@@@@@@@@@@@@@@@");
                            Log.d("server key", data);
                            if (data.equals("yes")) {
                                 if(auto_login_check_box.isChecked()){
                                    SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor auto_editor = auto.edit();
                                    auto_editor.putString("AutoEmail", login_email.getText().toString());
                                    auto_editor.putString("AutoPassword", login_password.getText().toString());
                                    auto_editor.commit();
                                }
                                System.out.println("로그인 성공");
                                System.out.println("user check : " + user.getCheck());
                                System.out.println("user email : " + user.getU_email());
                                System.out.println("user name : " + user.getU_name());
                                MyGlobals.getInstance().setUser(user); //로그인시 유저정보 저장
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else if (data.equals("no")) {

                                Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                                startActivity(intent);
                            } else if (data.equals("wrong")) {
                                Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                                startActivity(intent);
                            } else if (data.equals("error")) {
                                Intent intent = new Intent(getApplicationContext(),LoginErrorActivity.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            System.out.println("onFailure 호출@@@!!!!!!!!!!!!!");
                        }
                    });
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

    private void checkPermission(ArrayList<String> permissions){
        ArrayList<String> request_list = new ArrayList<>();
        for(String permission:permissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                request_list.add(permission);
            }
        }
        if (request_list.size() > 0) {
            String[] reqPermissionArray = new String[permissions.size()];
            reqPermissionArray = permissions.toArray(reqPermissionArray);
            ActivityCompat.requestPermissions(this, reqPermissionArray, MY_PERMISSIONS_REQUEST_MULTI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        ArrayList<String> request_list = new ArrayList<>();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)){
                request_list.add(Manifest.permission.CAMERA);
            }
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
                request_list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
                request_list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (request_list.size() > 0){
                checkPermission(request_list);
            }

        }

    }

}
