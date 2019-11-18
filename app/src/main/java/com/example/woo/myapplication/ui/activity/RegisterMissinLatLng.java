package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.woo.myapplication.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;

public class RegisterMissinLatLng extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng temp_center;
    private LatLng missing_center;
    private InfoWindow infoWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_missin_latlng);

        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("latitude", -1);
        double lng = intent.getDoubleExtra("longitude", -1);
        temp_center = new LatLng(lat, lng);

        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.mperson_latitude_longitude);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.mperson_latitude_longitude, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // 비동기적 NaverMap 객체 획득

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(temp_center);
        naverMap.moveCamera(cameraUpdate);

        // 중심점 지정 안내 말풍선 등록
        infoWindow = new InfoWindow();
        infoWindow.setAlpha(0.9f);
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return "선택된 실종지점";
            }
        });


        naverMap.setOnMapClickListener((point, coord) -> {
            infoWindow.setPosition(coord);
            infoWindow.open(naverMap);
            missing_center = new LatLng(
                    infoWindow.getPosition().latitude,
                    infoWindow.getPosition().longitude
            );
            infoWindow.setOnClickListener(overlay -> {
                infoWindow.close();
                missing_center = null;
                return true;
            });
        });

        // 지도 타입 변경 스피너 등록
        final ArrayAdapter<CharSequence> mapAdapter;
        mapAdapter = ArrayAdapter.createFromResource(this, R.array.map_types,
                android.R.layout.simple_spinner_item);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mapSpinner = findViewById(R.id.map_type_in_missing_latlng);
        mapSpinner.setAdapter(mapAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence mapType = mapAdapter.getItem(position);
                if (mapType != null) {
                    naverMap.setMapType(NaverMap.MapType.valueOf(mapType.toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public void mOnCancel(View v){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void mOnSave(View v){
        if(missing_center != null){
            Intent intent = new Intent();
            intent.putExtra("latitude", missing_center.latitude);
            intent.putExtra("longitude", missing_center.longitude);
            setResult(RESULT_OK, intent);
            finish();
        }
        else{
            Toast.makeText(RegisterMissinLatLng.this, "실종지점을 선택하세요.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){ // 바깥레이어 클릭시 안닫히게
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
