package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

import java.io.Serializable;

public class RegisterNewMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static Activity registerNewMapActivity;
    private InfoWindow infoWindow;
    private LatLng missingCoord;
    private Marker missingPoint;
    private LatLng centerCoord;
    private double bearing = 0;
    String p_id;
    String m_place_string;
    Serializable selected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_coord_center);

        registerNewMapActivity = RegisterNewMapActivity.this;


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // 실종지점 획득
        Intent intent = getIntent();
        selected = intent.getSerializableExtra("selecteditem");
        double lat = intent.getDoubleExtra("missing_lat", 0);
        double lon = intent.getDoubleExtra("missing_long", 0);
        p_id = intent.getStringExtra("p_id");
        m_place_string = intent.getStringExtra("m_place_string");
        missingCoord = new LatLng(lat, lon);

        // 실종지점 마커 생성
        missingPoint = new Marker();
        missingPoint.setIcon(MarkerIcons.BLACK);
        missingPoint.setIconTintColor(Color.RED);
        missingPoint.setPosition(missingCoord);
        missingPoint.setCaptionText("실종 지점");
        missingPoint.setCaptionColor(Color.RED);
        //missingPoint.setCaptionHaloColor(Color.RED);


        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // 비동기적 NaverMap 객체 획득
    }


    public void mOnClick(View v){
        if(centerCoord != null){
            Intent intent = new Intent(this, RegisterMapDetailsActivity.class);
            intent.putExtra("missing_lat", missingCoord.latitude);
            intent.putExtra("missing_lng", missingCoord.longitude);
            intent.putExtra("center_lat", centerCoord.latitude);
            intent.putExtra("center_lng", centerCoord.longitude);
            intent.putExtra("bearing", bearing);
            intent.putExtra("p_id",p_id);
            intent.putExtra("m_place_string",m_place_string);
            intent.putExtra("selecteditem",selected);
            startActivityForResult(intent, 1);

        }
        else{
            Toast.makeText(this, "중심점이 지정되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        /* 기본 맵 세팅 */
        // 지도 줌버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);
        // 현위치 버튼 활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.getUiSettings().setRotateGesturesEnabled(false);

        // 카메라 현위치 이동
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(missingCoord);
        naverMap.moveCamera(cameraUpdate);
        missingPoint.setMap(naverMap);

        // 지도 타입 변경 스피너 등록
        final ArrayAdapter<CharSequence> mapAdapter;
        mapAdapter = ArrayAdapter.createFromResource(this, R.array.map_types,
                android.R.layout.simple_spinner_item);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mapSpinner = findViewById(R.id.spinner_map_type);
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

        Toast.makeText(this, "지도를 회전시킨 후 중심위치를 터치해주세요.", Toast.LENGTH_LONG).show();

        // 중심점 지정 안내 말풍선 등록
        infoWindow = new InfoWindow();
        infoWindow.setAlpha(0.9f);
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return "현재 중심위치";
            }
        });


        naverMap.setOnMapClickListener((point, coord) -> {
            infoWindow.setPosition(coord);
            infoWindow.open(naverMap);
            centerCoord = new LatLng(
                    infoWindow.getPosition().latitude,
                    infoWindow.getPosition().longitude
            );
            infoWindow.setOnClickListener(overlay -> {
                infoWindow.close();
                centerCoord = null;
                return true;
            });
        });
    }
}
