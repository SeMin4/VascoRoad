package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.woo.myapplication.R;

public class EnterMapPWActivity extends Activity {
    private EditText password;
    private int mapInfo_index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_enter_pw);

        Intent intent = getIntent();
        mapInfo_index = intent.getIntExtra("mapInfoIndex", -1);

        password = (EditText) findViewById(R.id.EditText_password);


    }

    public void mOnCancel(View v){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void mOnAccept(View v){
        String strPassword1 = password.getText().toString();

        //if(/*비밀번호 일치*/){
            Intent intent = new Intent();
            intent.putExtra("mapInfoIndex", mapInfo_index);
            setResult(RESULT_OK, intent);
            finish();
        //6+}
        //else{   // 비밀번호 불일치 일치할때까지 못넘어감 ㅎㅎ
        //    Toast.makeText(this, "비밀번호를 다시 확인하세요.", Toast.LENGTH_LONG).show();
        //}


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }

}
