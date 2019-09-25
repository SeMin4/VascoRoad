package com.example.woo.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyPageActivity extends AppCompatActivity {
    protected LinearLayout MyPageLayout;
    protected EditText change_password;
    protected EditText change_check_password;
    protected EditText change_department;
    protected Button change_confirm_btn;
    protected TextView user_id;
    protected TextView user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        MyPageLayout = (LinearLayout) findViewById(R.id.my_page_layout);
        change_password = (EditText) findViewById(R.id.change_password);
        change_check_password = (EditText) findViewById(R.id.change_check_password);
        change_department = (EditText) findViewById(R.id.change_department);
        change_confirm_btn = (Button) findViewById(R.id.change_confirm_btn);
        user_id = (TextView)findViewById(R.id.user_id);
        user_name = (TextView)findViewById(R.id.user_name);

        user_id.setText(MyGlobals.getInstance().getUser().getU_email());
        user_name.setText(MyGlobals.getInstance().getUser().getU_name());
        change_department.setHint(MyGlobals.getInstance().getUser().getU_department());


        MyPageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(change_password.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(change_check_password.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(change_department.getWindowToken(), 0);

            }
        });
        change_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                else {
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                startActivity(intent);
            }
        });

    }
}
