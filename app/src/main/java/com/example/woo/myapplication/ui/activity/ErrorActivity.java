package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.woo.myapplication.R;


public class ErrorActivity extends AppCompatActivity {
    protected Button confirm_btn;
    protected TextView error_title;
    protected TextView error_body;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //status bar remove(whole screen)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_error_actvity);
        error_title = (TextView) findViewById(R.id.error_title);
        error_body = (TextView) findViewById(R.id.error_body);
        Intent intent = getIntent();
        int error_code = intent.getIntExtra("error_code",-1);
        switch (error_code){
            case 0:
                error_title.setText("회원가입 에러");
                error_body.setText("이메일 형식이 잘못되었습니다.");
                break;
            case 1:
                error_title.setText("회원가입 에러");
                error_body.setText("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                break;
            case 2:
                error_title.setText("회원가입 에러");
                error_body.setText("빈칸이 없도록 빠짐없이 채워주시기 바랍니다.");
                break;
            case  3:
                error_title.setText("회원가입 에러");
                error_body.setText("이메일 중복체크를 진행해 주시기 바랍니다.");
                break;
            case 4:
                error_title.setText("회원가입 에러");
                error_body.setText("이미 존재하는 이메일 입니다.");
                break;
            default:
                break;
        }
        confirm_btn = (Button) findViewById(R.id.error_confirm_btn);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
