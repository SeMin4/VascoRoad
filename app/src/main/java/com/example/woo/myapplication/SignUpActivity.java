package com.example.woo.myapplication;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {
    static int email_check_integer = 0;
    protected EditText sign_up_email;
    protected Button sign_up_email_check_btn;
    protected EditText sign_up_password;
    protected EditText sign_up_check_password;
    protected EditText sign_up_name;
    protected EditText sign_up_department;
    protected Button sign_up_btn;
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
        sign_up_email_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailValid(sign_up_email.getText().toString())){
                    email_check_integer = 1;
                    Toast.makeText(getApplicationContext(), "사용할 수 있는 이메일 입니다.", Toast.LENGTH_SHORT).show();
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
                if(sign_up_email.getText().toString().equals("") || sign_up_password.getText().toString().equals("") || sign_up_check_password.getText().toString().equals("") || sign_up_name.getText().toString().equals("") || sign_up_department.getText().toString().equals("")){
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code", 2);
                    startActivity(intent);
                }
                else if(email_check_integer == 0){
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code",3);
                    startActivity(intent);
                }
                else if(sign_up_password_str.equals(sign_up_check_password_str)){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                    Toast.makeText(getApplicationContext(), "회원 가입 완료", Toast.LENGTH_LONG).show();
                    startActivity(intent);

                }else{
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("error_code",1);
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
