package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
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

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.District;
import com.example.woo.myapplication.data.MapInfo;
import com.example.woo.myapplication.data.Mperson;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class NewMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ArrayList<ArrayList<PolygonOverlay>> total_districts;
    private FusedLocationSource locationSource;
    private LatLng[] vertex_list = new LatLng[4];
    private LatLngBounds mapBounds;
    private MapInfo mapInfo;
    private int COLOR_LINE_BLACK;
    private int COLOR_LINE_WHITE;
    private int COLOR_FINISH;
    private Socket mSocket = null;
    public int color_finish;
    public String received_districtNum;
    public String received_index;
    public String received_districtNum2;
    public String received_index2;
    public String received_content2;
    public int color_impossible;
    public String m_id;
    Mperson selected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        RegisterNewMapActivity registerNewMapActivity =
                (RegisterNewMapActivity) RegisterNewMapActivity.registerNewMapActivity;
        registerNewMapActivity.finish();
        RegisterMapDetailsActivity registerMapDetailsActivity =
                (RegisterMapDetailsActivity) RegisterMapDetailsActivity.registerMapDetailsActivity;
        registerMapDetailsActivity.finish();

        COLOR_LINE_BLACK = ResourcesCompat.getColor(getResources(), R.color.black, getTheme());
        COLOR_LINE_WHITE = ResourcesCompat.getColor(getResources(), R.color.white, getTheme());
        COLOR_FINISH = ResourcesCompat.getColor(getResources(), R.color.finish, getTheme());

        try{
            if(mSocket == null) {
                mSocket = IO.socket("http://13.125.174.158:9000");
                mSocket.connect();
                //이벤트 등록
                mSocket.on(Socket.EVENT_CONNECT, onConnect); //방 접속시;
                mSocket.on("attendRoom", attendRoom);// 방접속시 user 아이디 보내기
                mSocket.on("complete", complete);
                mSocket.on("not_complete", not_complete);
            }
        }catch(URISyntaxException e){
            e.printStackTrace();
        }

        JSONObject data = new JSONObject();
        try{
            System.out.println("attendRoom@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111111111111111111");
            data.put("id", MyGlobals.getInstance().getUser().getU_id());
            mSocket.emit("attendRoom",data);
        }catch(JSONException e){
            System.out.println("attendRoom 에러");
            e.printStackTrace();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // 비동기적 NaverMap 객체 획득

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        Intent intent = getIntent();
        m_id = intent.getStringExtra("m_id");
        mapInfo = (MapInfo) intent.getSerializableExtra("mapInfo");
        mapInfo.setM_id(m_id);
        selected = (Mperson)intent.getSerializableExtra("selecteditem");

        double[] vertex_double = intent.getDoubleArrayExtra("vertex");
        for (int i = 0; i < vertex_double.length; i += 2) {
            int index = i / 2;
            Log.d("Map", "vertex index" + index);
            vertex_list[index] = new LatLng(vertex_double[i], vertex_double[i + 1]);
        }
        mapBounds = new LatLngBounds(
                new LatLng(vertex_list[3].latitude, vertex_list[3].longitude),
                new LatLng(vertex_list[1].latitude, vertex_list[1].longitude)
        );


    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Toast.makeText(getApplicationContext(),"방에 접속했습니다.",Toast.LENGTH_SHORT).show();
            System.out.println("방에 접속했습니다");
        }
    }; //제일처음 접속

    private Emitter.Listener attendRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                System.out.println("attendRoom@@@@@@@@@@@@@@@@@@@22222222222222222222");
                JSONObject receivedData = (JSONObject)args[0];
                System.out.println("msg: " +receivedData.getString("msg") +"@@@@@@@@@@");
                System.out.println("data : "+receivedData.getString("data")+"@@@@@@@@@@@");

            }catch(JSONException e){
                System.out.println("JSONException 발생");
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener complete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject receivedData = (JSONObject) args[0];
                received_districtNum = receivedData.getString("districtNum");
                received_index = receivedData.getString("index");
                System.out.println("district : " + receivedData.getString("districtNum") + "@@@@@@@@@@@@@@@");
                System.out.println("index : " + receivedData.getString("index") + "@@@@@@@@@@@@@");
                System.out.println("실행됨@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        total_districts.get(Integer.parseInt(received_districtNum)).get(Integer.parseInt(received_index)).setColor(ColorUtils.setAlphaComponent(color_finish, 100));
                    }
                });


            } catch (JSONException e) {
                System.out.println("complete JsonException");
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener not_complete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject receivedData = (JSONObject)args[0];
                received_districtNum2 = receivedData.getString("districtNum");
                received_index2 = receivedData.getString("index");
                received_content2 = receivedData.getString("content");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        total_districts.get(Integer.parseInt(received_districtNum2)).get(Integer.parseInt(received_index2)).setColor(ColorUtils.setAlphaComponent(color_impossible, 100));
                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationSource = null;
        System.out.println("onDestroy@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
       /* try{
            JSONObject data = new JSONObject();
            data.put("u_id",MyGlobals.getInstance().getUser().getU_id());
            mSocket.emit("disconnect",data);
        }catch(JSONException e){
            e.printStackTrace();
        }*/
        mSocket.disconnect();
        mSocket.off("attendRoom",attendRoom);
        mSocket.off("complete",complete);
        mSocket.off("not_complete",not_complete);
        mSocket.close();
        mSocket = null;

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
    public void onMapReady(@NonNull NaverMap naverMap) {
        /* 기본 맵 세팅 */
        // 지도 줌버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);
        // 현위치 버튼 활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationSource(locationSource);
        // 위치 변경 리스너 등록
        naverMap.addOnLocationChangeListener(location -> {
            /* 현재 위치 획득하는 법 */
//            if(coord_center == null) {
//                coord_center = new LatLng(location.getLatitude(), location.getLongitude());
//                Toast.makeText(this,
//                        "현재위치: " + coord_center.latitude + ", " + coord_center.longitude,
//                        Toast.LENGTH_SHORT).show();
//            }
        });

        // 지도 중심 설정
        LatLng center_coord = new LatLng(Double.parseDouble(mapInfo.getM_center_point_latitude()), Double.parseDouble(mapInfo.getM_center_point_longitude()));
        naverMap.moveCamera(CameraUpdate.fitBounds(mapBounds, 10));
        naverMap.moveCamera(CameraUpdate
                .withParams(new CameraUpdateParams()
                        .rotateBy(Double.parseDouble(mapInfo.getM_rotation()))
                )
                .animate(CameraAnimation.Easing));


        // 실종지점 등록
        Marker missingPoint = new Marker();
        LatLng missing_coord = new LatLng(Double.parseDouble(mapInfo.getM_place_latitude()), Double.parseDouble(mapInfo.getM_place_longitude()));
        missingPoint.setPosition(missing_coord);
        missingPoint.setWidth(50);
        missingPoint.setHeight(50);
        missingPoint.setIcon(MarkerIcons.BLACK);
        missingPoint.setIconTintColor(Color.RED);
        missingPoint.setCaptionText("실종 지점");
        missingPoint.setCaptionColor(Color.RED);
        missingPoint.setMap(naverMap);

        // 지도 그리드
        District total = createDistricts(center_coord, Double.parseDouble(mapInfo.getM_unit_scale()));
        for(District child: total.getChildren()){
            for(District grandChild: child.getChildren()){
                grandChild.getGrid().setMap(naverMap);
            }
        }

        // 지도 타입 변경 스피너 등록!
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
                    switch (mapType.toString()) {
                        case "Satellite":
                            for(District child: total.getChildren()){
                                for(District grandChild: child.getChildren()){
                                    grandChild.getGrid().setOutlineColor(COLOR_LINE_WHITE);
                                }
                            }
                            break;
                        case "Basic":
                        case "Terrain":
                            for(District child: total.getChildren()){
                                for(District grandChild: child.getChildren()){
                                    grandChild.getGrid().setOutlineColor(COLOR_LINE_BLACK);
                                }
                            }
                            break;

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        naverMap.setOnMapLongClickListener((pointF, latLng) -> {
            if (total_districts != null) {
                for (int i = 0; i < total_districts.size(); i++) {
                    for (int j = 0; j < total_districts.get(0).size(); j++) {
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

    private District createDistricts(LatLng center, double unit) {
        District district = new District();
        district.setCenter(center);

        // 중앙점 찾기
        double offset = unit * 3;
        double up_dist = (Double.parseDouble(mapInfo.getM_up()) - offset / 2) / offset;
        double start_lat = center.latitude
                + LocationDistance.LatitudeInDifference(offset * (up_dist + 1));
        double left_dist = (Double.parseDouble(mapInfo.getM_left()) - offset / 2) / offset;
        double start_lng = center.longitude
                - LocationDistance.LongitudeInDifference(start_lat, offset * left_dist);


        double col = (Double.parseDouble(mapInfo.getM_left()) + Double.parseDouble(mapInfo.getM_right())) / offset;
        double row = (Double.parseDouble(mapInfo.getM_up()) + Double.parseDouble(mapInfo.getM_down())) / offset;


        ArrayList<Marker> markers = new ArrayList<>();
        for (int i = 1; i <= (int) row; i++) {
            LatLng row_temp = new LatLng(start_lat - LocationDistance.LatitudeInDifference(unit * 3) * i, start_lng);
            for (int j = 0; j < col; j++) {
                LatLng col_temp = new LatLng(
                        row_temp.latitude,
                        row_temp.longitude + LocationDistance.LongitudeInDifference(row_temp.latitude, offset) * j
                );
                district.getChildren().add(createDistrict(col_temp, unit));
            }
        }
        return district;
    }

    private District createDistrict(LatLng center, /* 축척 */double unit) {
        double offset_x = LocationDistance.LatitudeInDifference(unit);
        double offset_y = LocationDistance.LongitudeInDifference(center.latitude, unit);
        double[] offsets_x = {1.5 * offset_x, 0.5 * offset_x, -0.5 * offset_x, -1.5 * offset_x};
        double[] offsets_y = {-1.5 * offset_y, -0.5 * offset_y, 0.5 * offset_y, 1.5 * offset_y};

        ArrayList<LatLng> coords = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double temp_lat = center.latitude + offsets_x[i];
                double temp_lng = center.longitude + offsets_y[j];
                LatLng temp = new LatLng(temp_lat, temp_lng);
                coords.add(temp);
            }
        }

        District district = new District(
                coords.get(0),
                coords.get(12),
                coords.get(15),
                coords.get(3)
        );
        // 중심설정
        district.setCenter(center);
        // 자식그리드 생성
        for (double i = 2.5; i < 13; i++) {
            if (i == 5.5 || i == 9.5) continue;

            District childDistrict = new District(
                    coords.get((int) (i - 2.5)),    // northWest
                    coords.get((int) (i + 1.5)),    // southWest
                    coords.get((int) (i + 2.5)),    // southEast
                    coords.get((int) (i - 1.5))     // northEast
            );
            childDistrict.getGrid().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 0));
            childDistrict.getGrid().setOutlineColor(COLOR_LINE_WHITE);
            childDistrict.getGrid().setOutlineWidth(getResources().getDimensionPixelSize(R.dimen.overlay_line_width));

            district.getChildren().add(childDistrict);
        }
        return district;
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

    public void mOnPopupClick(int districtNum, int index) {
        Intent intent = new Intent(this, DistrictRecordActivity.class);
        intent.putExtra("district", districtNum);
        intent.putExtra("index", index);
        startActivityForResult(intent, 1);
    }

    public void mOnInfoClick(View v) {
        //그대로 전달 -> 새로운 액티비ㅣㅌ 종나 만들어야함
        Intent intent = new Intent(this,MissingInfoActivity.class);
        intent.putExtra("selecteditem",selected);
        startActivityForResult(intent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("result");
                int districtNum;
                int index;
                switch (result) {
                    case "Find Finish":
                        districtNum = data.getIntExtra("district", -1);
                        index = data.getIntExtra("location", -1);
                        System.out.println("districtNum : "+districtNum+"@@@@@@@@@@@@@@@@@@@" );
                        System.out.println("index : "+index+"@@@@@@@@@@@@@@@@@@@" );
                        color_finish = getResources().getColor(R.color.finish);
                        //total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_finish, 100));

                        try {
                            JSONObject complete_data = new JSONObject();
                            System.out.println("complete@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111111111111111111");
                            String area_districtNum = ""+districtNum;
                            String area_index = ""+index;
                            complete_data.put("mid",m_id);
                            complete_data.put("districtNum",area_districtNum);
                            complete_data.put("index",area_index);
                            mSocket.emit("complete", complete_data);
                        }catch(JSONException e){
                            System.out.println("complete 에러");
                            e.printStackTrace();
                        }
                        break;
                    case "Find Impossible":
                        String content = data.getStringExtra("content");
                        Toast.makeText(this, "특이사항: " + content, Toast.LENGTH_SHORT).show();
                        districtNum = data.getIntExtra("district", -1);
                        index = data.getIntExtra("location", -1);
                        String imagePath = data.getStringExtra("imagePath");
                         color_impossible = getResources().getColor(R.color.impossible);
                        //total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_impossible, 100));
                        try{
                            JSONObject non_complete_data = new JSONObject();
                            String area_districtNum = ""+districtNum;
                            String area_index = ""+index;
                            non_complete_data.put("mid", m_id);
                            non_complete_data.put("districtNum",area_districtNum);
                            non_complete_data.put("index",area_index);
                            non_complete_data.put("content",content);
                            mSocket.emit("not_complete",non_complete_data);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        break;
                    case "Close Popup":
                        break;


                }
            }
        }
    }
}

