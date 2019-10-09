package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.R;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class SignUpActivity extends AppCompatActivity {
    static int email_check_integer = 0;
    protected EditText sign_up_email;
    protected Button sign_up_email_check_btn;
    protected EditText sign_up_password;
    protected EditText sign_up_check_password;
    protected EditText sign_up_name;
    protected EditText sign_up_department;
    protected Button sign_up_btn;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;
    protected String sign_up_email_same = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sign_up_email = (EditText) findViewById(R.id.sign_up_email);
        sign_up_email_check_btn = (Button) findViewById(R.id.sign_up_email_check_btn);
        sign_up_password = (EditText) findViewById(R.id.sign_up_password);
        sign_up_check_password = (EditText) findViewById(R.id.sign_up_check_password);
        sign_up_name = (EditText) findViewById(R.id.sign_up_name);
        sign_up_department = (EditText) findViewById(R.id.sign_up_department);
        sign_up_btn = (Button) findViewById(R.id.sign_up_btn);
        if( (MyGlobals.getInstance().getRetrofit() == null) || (MyGlobals.getInstance().getRetrofitExService() ==null) ){
            retrofit = new Retrofit.Builder().baseUrl(MyGlobals.RetrofitExService.URL).addConverterFactory(GsonConverterFactory.create()).build();
            retrofitExService = retrofit.create(MyGlobals.RetrofitExService.class);
            MyGlobals.getInstance().setRetrofit(retrofit);
            MyGlobals.getInstance().setRetrofitExService(retrofitExService);
        }else{
            retrofit = MyGlobals.getInstance().getRetrofit();
            retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        }

        sign_up_email_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailValid(sign_up_email.getText().toString())){
                    HashMap<String,String> input = new HashMap<>();
                    input.put("email",sign_up_email.getText().toString());
                    //input.put("test","test");
                    retrofitExService.postData(input).enqueue(new Callback<OverlapExamineData>() {
                        @Override
                        public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                            System.out.println("onResponse 호출");
                            OverlapExamineData overlapData = response.body();
                            Log.d("overlap", overlapData.getOverlap_examine());
                            if(overlapData.getOverlap_examine().equals("access")) {
                                email_check_integer = 1;
                                sign_up_email_same = sign_up_email.getText().toString();
                                Toast.makeText(getApplicationContext(), "사용할 수 있는 이메일 입니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                                intent.putExtra("error_code", 4);
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                            System.out.println("onFailure 호출");
                            System.out.println(t);
                        }
                    });
                   
                }else {
                    email_check_integer = 0;
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code",0);
                    startActivity(intent);
                }
            }
        });
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sign_up_password_str = sign_up_password.getText().toString();
                String sign_up_check_password_str = sign_up_check_password.getText().toString();
                if (sign_up_email.getText().toString().equals("") || sign_up_password.getText().toString().equals("") || sign_up_check_password.getText().toString().equals("") || sign_up_name.getText().toString().equals("") || sign_up_department.getText().toString().equals("")) {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code", 2);
                    startActivity(intent);
                } else if (email_check_integer == 0) {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code", 3);
                    startActivity(intent);
                } else if (sign_up_password_str.equals(sign_up_check_password_str)) {
                    if(sign_up_email_same.equals(sign_up_email.getText().toString())){
                        HashMap<String, String> input = new HashMap<>();
                        input.put("email", sign_up_email.getText().toString());
                        input.put("password", sign_up_password.getText().toString());
                        input.put("department", sign_up_department.getText().toString());
                        input.put("name", sign_up_name.getText().toString());
                        retrofitExService.postAdmin(input).enqueue(new Callback<OverlapExamineData>() {
                            @Override
                            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                                System.out.println("onResponse@@@@@@@@@@@@@@");
                                OverlapExamineData overlapExamineData = response.body();
                                if (overlapExamineData.getOverlap_examine().equals("success")) {
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    } else {
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    }
                                    Toast.makeText(getApplicationContext(), "회원 가입 완료", Toast.LENGTH_LONG).show();
                                    startActivity(intent);
                                } else if (overlapExamineData.getOverlap_examine().equals("deny"))
                                    Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                                System.out.println("onFailure@@@@@@@@@@@@@@");
                            }
                        });
                    }
                    else{
                        email_check_integer = 0;
                        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                        intent.putExtra("error_code", 3);
                        startActivity(intent);
                    }


                } else {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code", 1);
                    startActivity(intent);
                }
            }
        });
    }
    static boolean emailValid(String email){
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

}
