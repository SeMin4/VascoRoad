package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.District;
import com.naver.maps.geometry.LatLng;


public class DistrictRecordActivity extends Activity {
    private TextView prompt;
    private int markerId;
    private int mapId;
    private double latitude;
    private double longitude;
    private int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_location_status);

        Intent intent = getIntent();

        markerId = intent.getIntExtra("markerId", -1);
        mapId = intent.getIntExtra("mapId", -1);
        latitude = intent.getDoubleExtra("latitude", -1);
        longitude = intent.getDoubleExtra("longitude", -1);
        index = intent.getIntExtra("index",-1);

        System.out.println("district_recrd_activity : "+index);

        prompt = findViewById(R.id.popup_location);
        prompt.setText("지점 등록");

    }


    public void mOnFindFinish(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Find Finish");
        intent.putExtra("markerId", markerId);
        setResult(RESULT_OK, intent);

        finish();
    }


    public void mOnFindImpossible(View v){
        Intent intent = new Intent(this, UnusualRecordActivity.class);
        intent.putExtra("mapId", mapId);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("index",index);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                if(result.equals("Saved")){
                    Toast.makeText(this, "사진 및 내용이 CP로 전달되었습니다.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.putExtra("result", "Find Impossible");
                    intent.putExtra("desc", data.getStringExtra("desc"));
                    intent.putExtra("image", data.getStringExtra("image"));
                    intent.putExtra("markerId", markerId);
                    setResult(RESULT_OK, intent);

                    finish();
                }
            }
        }
    }

    public void mOnCancel(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        intent.putExtra("markerId", markerId);
        setResult(RESULT_CANCELED, intent);

        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){ //바깥레이어 클릭시 안닫히게
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        intent.putExtra("markerId", markerId);
        setResult(RESULT_CANCELED, intent);

        finish();
    }
}
