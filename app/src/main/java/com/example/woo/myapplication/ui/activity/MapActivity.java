package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.MapInfo;
import com.example.woo.myapplication.utils.LocationDistance;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.CameraUpdateParams;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private MapInfo mapInfo;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<ArrayList<PolygonOverlay>> total_districts;
    private int COLOR_LINE_BLACK;
    private int COLOR_LINE_WHITE;
    private int COLOR_FINISH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        COLOR_LINE_BLACK = ResourcesCompat.getColor(getResources(), R.color.black, getTheme());
        COLOR_LINE_WHITE= ResourcesCompat.getColor(getResources(), R.color.white, getTheme());
        COLOR_FINISH = ResourcesCompat.getColor(getResources(), R.color.finish, getTheme());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // 비동기적 NaverMap 객체 획득

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        Intent intent = getIntent();
        mapInfo = intent.getParcelableExtra("mapInfo");


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationSource = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        /* 기본 맵 세팅 */
        // 지도 줌버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);
        // 현위치 버튼 활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationSource(locationSource);
        // 위치 변경 리스너 등록
        naverMap.addOnLocationChangeListener(location ->{
            /* 현재 위치 획득하는 법 */
//            if(coord_center == null) {
//                coord_center = new LatLng(location.getLatitude(), location.getLongitude());
//                Toast.makeText(this,
//                        "현재위치: " + coord_center.latitude + ", " + coord_center.longitude,
//                        Toast.LENGTH_SHORT).show();
//            }
        });

        // 지도 중심 설정
        LatLng center_coord = new LatLng(mapInfo.getCenter_lat(), mapInfo.getCenter_lng());
        CameraUpdateParams params = new CameraUpdateParams();
        params.scrollTo(center_coord);
        params.rotateBy(mapInfo.getBearing()); // 90(왼쪽으로 90도임),
        naverMap.moveCamera(CameraUpdate.withParams(params).animate(CameraAnimation.Easing));

        // 맵이 최대로 보이도록 확대
        //naverMap.moveCamera(CameraUpdate.fitBounds())

        // 실종지점 등록
        Marker missingPoint = new Marker();
        LatLng missing_coord = new LatLng(mapInfo.getMissing_lat(), mapInfo.getMissing_lng());
        missingPoint.setPosition(missing_coord);
        missingPoint.setWidth(50);
        missingPoint.setHeight(50);
        missingPoint.setIcon(MarkerIcons.BLACK);
        missingPoint.setIconTintColor(Color.RED);
        missingPoint.setMap(naverMap);

        // 기본 지도 그리기
//        total_districts = createDistricts(center_coord, mapInfo.getUnit_scale());
//        for(ArrayList<PolygonOverlay> district: total_districts){
//            for(PolygonOverlay square: district){
//                square.setMap(naverMap);
//            }
//        }

        /* 디버깅 중 */
        ArrayList<PolygonOverlay> district = createDistrict(center_coord, mapInfo.getUnit_scale());
        for(PolygonOverlay p:district){
            for(LatLng l:p.getCoords()){
                Marker m = new Marker();
                m.setPosition(l);
                m.setMap(naverMap);
            }
            p.setMap(naverMap);
        }

        // 지도 타입 변경 스피너 등록
        final ArrayAdapter<CharSequence> mapAdapter;
        mapAdapter = ArrayAdapter.createFromResource(this, R.array.map_types,
                android.R.layout.simple_spinner_item);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mapSpinner = findViewById(R.id.map_type);
        mapSpinner.setAdapter(mapAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence mapType = mapAdapter.getItem(position);
                if (mapType != null) {
                    naverMap.setMapType(NaverMap.MapType.valueOf(mapType.toString()));
                    /* 지도 type에 따른 선 색상 지정 */
//                    switch(mapType.toString()){
//                        case "Satellite":
//                            for(ArrayList<PolygonOverlay> district:total_districts){
//                                for(PolygonOverlay square:district){
//                                    square.setOutlineColor(COLOR_LINE_WHITE);
//                                }
//                            }
//                            break;
//                        case "Basic":
//                        case "Terrain":
//                            for(ArrayList<PolygonOverlay> district:total_districts){
//                                for(PolygonOverlay square:district){
//                                    square.setOutlineColor(COLOR_LINE_BLACK);
//                                }
//                            }
//                            break;
//
//                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        naverMap.setOnMapLongClickListener((pointF, latLng) -> {
            if(total_districts != null) {
                for (int i = 0; i < total_districts.size(); i++) {
                    for(int j = 0; j < total_districts.get(0).size(); j++){
                        LatLngBounds bounds = total_districts.get(i).get(j).getBounds();
                        // 특정 구역 long-tap 시 팝업창 실행.
                        if (bounds.contains(latLng)) {
                            mOnPopupClick(i, j);
                        }

                    }
                }
            }
        });

    }


    private ArrayList<ArrayList<PolygonOverlay>> createDistricts(LatLng center, double unit){
        ArrayList<ArrayList<PolygonOverlay>> grids = new ArrayList<>();

        // 중앙점 찾기
        double offset = unit*3;
        double up_dist = (mapInfo.getUp_height() - offset/2) / offset;
        double start_lat = center.latitude
                + LocationDistance.LatitudeInDifference(offset*(up_dist+1));
        double left_dist = (mapInfo.getLeft_width() - offset/2) / offset;
        double start_lng = center.longitude
                - LocationDistance.LongitudeInDifference(start_lat, offset*left_dist);

        double row = (mapInfo.getLeft_width() + mapInfo.getRight_width()) / offset;
        double col = (mapInfo.getUp_height() + mapInfo.getDown_height()) / offset;

        ArrayList<Marker> markers = new ArrayList<>();
        for(int i = 1; i <= (int) row; i++){
            LatLng row_temp = new LatLng(start_lat - LocationDistance.LatitudeInDifference(unit*3) * i, start_lng);
            for(int j = 0; j < col; j++){
                LatLng col_temp = new LatLng(
                        row_temp.latitude,
                        row_temp.longitude + LocationDistance.LongitudeInDifference(row_temp.latitude, offset)*j
                );
                grids.add(createDistrict(col_temp, unit));
            }
        }
        return grids;
    }


    private ArrayList<PolygonOverlay> createDistrict(LatLng center, /* 축척 */double unit){
        double offset_x = LocationDistance.LatitudeInDifference(unit);
        double offset_y = LocationDistance.LongitudeInDifference(center.latitude, unit);
        double[] offsets_x = { 1.5*offset_x, 0.5*offset_x, -0.5*offset_x, -1.5*offset_x};
        double[] offsets_y = { -1.5*offset_y, -0.5*offset_y, 0.5*offset_y, 1.5*offset_y};

        ArrayList<LatLng> coords = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                double temp_lat = center.latitude + offsets_x[i];
                double temp_lng = center.longitude + offsets_y[j];
                LatLng temp = new LatLng(temp_lat, temp_lng);
                // 실험 중
                double angle_rad = LocationDistance.angleByPoint(center, temp) + LocationDistance.deg2rad(mapInfo.getBearing());
                // 실험 끝
                double k = LocationDistance.distance(center, temp, "meter");
                double offset_lat = LocationDistance.LatitudeInDifference(k* Math.cos(angle_rad));
                double offset_lng = LocationDistance.LongitudeInDifference(center.latitude, k* Math.sin(angle_rad));
                LatLng point = new LatLng(
                  temp_lat + offset_lat,
                        temp_lng - offset_lng
                );
                Log.d("MapActivity::point",  point.latitude + " / " + point.longitude);
                coords.add(point);

            }
        }

        ArrayList<PolygonOverlay> polygons = new ArrayList<>();

        for(double i = 2.5; i < 13; i++){
            if(i == 5.5 || i == 9.5) continue;

            List<LatLng> temp = Arrays.asList(
                    coords.get((int) (i + 2.5)),
                    coords.get((int) (i + 1.5)),
                    coords.get((int) (i - 2.5)),
                    coords.get((int) (i - 1.5))
            );

            PolygonOverlay polygon = new PolygonOverlay();
            polygon.setCoords(temp);
            polygon.setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 0));
            polygon.setOutlineColor(COLOR_LINE_BLACK);
            polygon.setOutlineWidth(getResources().getDimensionPixelSize(R.dimen.overlay_line_width));

            polygons.add(polygon);
        }

        return polygons;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void mOnPopupClick(int districtNum, int index){
        Intent intent = new Intent(this, PopUpActivity.class);
        intent.putExtra("district", districtNum);
        intent.putExtra("index", index);
        startActivityForResult(intent, 1);
    }

    public void mOnInfoClick(View v){
        Toast.makeText(this, "여기 준희가 만든 실종자 정보 팝업화면 비스므리하게 연결하면 됨.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                int districtNum;
                int index;
                switch(result){
                    case "Find Finish":
                        districtNum = data.getIntExtra("district", -1);
                        index = data.getIntExtra("location", -1);
                        int color_finish = getResources().getColor(R.color.finish);
                        total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_finish, 100));
                        break;
                    case "Find Impossible":
                        String content = data.getStringExtra("content");
                        Toast.makeText(this, "특이사항: " + content, Toast.LENGTH_SHORT).show();
                        districtNum = data.getIntExtra("district", -1);
                        index = data.getIntExtra("location", -1);
                        int color_impossible = getResources().getColor(R.color.impossible);
                        total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_impossible, 100));
                        break;
                    case "Close Popup":
                        break;


                }
            }
        }
    }
}
