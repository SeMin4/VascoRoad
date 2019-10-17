package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.woo.myapplication.R;


public class DistrictRecordActivity extends Activity {
    private int districtNum;
    private int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_location_status);

        Intent intent = getIntent();
        districtNum = intent.getIntExtra("district", -1);
        index = intent.getIntExtra("index", -1);

        int row = index / 3 + 1;
        int col = index % 3 + 1;
        String location = districtNum +"구역  " + row+"행 " + col+"열";

        TextView popup_location = findViewById(R.id.popup_location);
        popup_location.setText(location);

    }


    public void mOnFindFinish(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Find Finish");
        intent.putExtra("district", districtNum);
        intent.putExtra("location", index);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
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
                    String contentPath = data.getStringExtra("imagePath");
                    String content = data.getStringExtra("content");

                    Intent intent = new Intent();
                    intent.putExtra("result", "Find Impossible");
                    intent.putExtra("imagePath", contentPath);  // 이미지가 저장된 경로
                    intent.putExtra("content", content);
                    intent.putExtra("district", districtNum);
                    intent.putExtra("location", index);
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
        setResult(RESULT_OK, intent);

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
        return;
    }
}
