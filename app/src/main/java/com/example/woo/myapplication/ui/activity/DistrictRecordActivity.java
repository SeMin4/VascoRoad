package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.District;


public class DistrictRecordActivity extends Activity {
    private TextView prompt;
    private int markerId;
    private District district;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_location_status);

        Intent intent = getIntent();

        markerId = intent.getIntExtra("markerId", -1);


        prompt = findViewById(R.id.popup_location);
        prompt.setText("지점 등록");

    }


    public void mOnFindFinish(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Find Finish");
        setResult(RESULT_OK, intent);
        finish();
    }


    public void mOnFindImpossible(View v){
        Intent intent = new Intent(this, UnusualRecordActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                if(result.equals("Saved")){
                    String imagePath = data.getStringExtra("imagePath");
                    String content = data.getStringExtra("content");

                    Intent intent = new Intent();
                    intent.putExtra("result", "Find Impossible");
                    intent.putExtra("imagePath", imagePath);  // 이미지가 저장된 경로
                    intent.putExtra("content", content);
                    intent.putExtra("markerId", markerId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    public void mOnCancel(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        intent.putExtra("markerId", markerId);
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
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
        //안드로이드 백버튼 막기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        intent.putExtra("markerId", markerId);
        setResult(RESULT_CANCELED, intent);
        finish();

    }
}
