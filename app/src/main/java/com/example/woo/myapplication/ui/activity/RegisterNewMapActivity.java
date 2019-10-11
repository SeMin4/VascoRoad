package com.example.woo.myapplication.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

public class RegisterNewMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private InfoWindow infoWindow;
    private LatLng missingCoord;
    private Marker missingPoint;
    private LatLng centerCoord;
    private TextView textView_bearing;
    private double bearing;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_coord_center);
        textView_bearing = findViewById(R.id.textView_bearing_value);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // 실종지점 획득
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("missing_lat", 0);
        double lon = intent.getDoubleExtra("missing_long", 0);
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

        // 중심점 지정 안내 말풍선 등록
        infoWindow = new InfoWindow();
        infoWindow.setAnchor(new PointF(0, 1));
        infoWindow.setOffsetX(getResources().getDimensionPixelSize(R.dimen.custom_info_window_offset_x));
        infoWindow.setOffsetY(getResources().getDimensionPixelSize(R.dimen.custom_info_window_offset_y));
        infoWindow.setAdapter(new InfoWindowAdapter(this));
        infoWindow.setOnClickListener(overlay -> {
            infoWindow.close();
            centerCoord = null;
            return true;
        });

        naverMap.setOnMapClickListener((point, coord) -> {
            infoWindow.setPosition(coord);
            infoWindow.open(naverMap);
            centerCoord = new LatLng(
                    infoWindow.getPosition().latitude,
                    infoWindow.getPosition().longitude
            );
        });

        naverMap.addOnCameraChangeListener((reason, animated) -> {
            CameraPosition position = naverMap.getCameraPosition();
            bearing = position.bearing;
            int value = (int) Math.floor(bearing);
            Log.d("RegisterNewActivity", "current bearing = " + value);
            textView_bearing.setText(Integer.toString(value));

        });
    }

    private static class InfoWindowAdapter extends InfoWindow.ViewAdapter {
        @NonNull
        private final Context context;
        private View rootView;
        private ImageView icon;
        private TextView text;

        private InfoWindowAdapter(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(@NonNull InfoWindow infoWindow) {
            if (rootView == null) {
                rootView = View.inflate(context, R.layout.view_center_info_window, null);
                icon = rootView.findViewById(R.id.icon);
                text = rootView.findViewById(R.id.text);
            }

            if (infoWindow.getMarker() != null) {
                icon.setImageResource(R.drawable.ic_place_black_24dp);
                text.setText((String)infoWindow.getMarker().getTag());
            } else {
                icon.setImageResource(R.drawable.ic_center_location_black_24dp);
                text.setText("현재 중심점");
            }

            return rootView;
        }

    }
}
