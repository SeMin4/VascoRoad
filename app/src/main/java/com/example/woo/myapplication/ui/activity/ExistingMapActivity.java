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

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.MapDetail;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ExistingMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private MapInfo mapInfo;
    LatLng[] vertex_list = new LatLng[4];
    private LatLngBounds mapBounds;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<ArrayList<PolygonOverlay>> total_districts;
    private PolygonOverlay big_polygon;
    private int COLOR_LINE_BLACK;
    private int COLOR_LINE_WHITE;
    private int COLOR_FINISH;
    private Socket mSocket=null;
    public String received_districtNum;
    public String received_index;
    public String received_districtNum2;
    public String received_index2;
    public String received_content2;
    public int color_finish;
    public int color_impossible;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mapInfo = (MapInfo) intent.getSerializableExtra("mapInfo");

        COLOR_LINE_BLACK = ResourcesCompat.getColor(getResources(), R.color.black, getTheme());
        COLOR_LINE_WHITE = ResourcesCompat.getColor(getResources(), R.color.white, getTheme());
        COLOR_FINISH = ResourcesCompat.getColor(getResources(), R.color.finish, getTheme());
        color_finish = getResources().getColor(R.color.finish);
        color_impossible = getResources().getColor(R.color.impossible);


        try {
            if (mSocket == null) {
                mSocket = IO.socket("http://13.125.95.139:9000");
                mSocket.connect();
                //이벤트 등록
                mSocket.on(Socket.EVENT_CONNECT, onConnect); //방 접속시;
                //mSocket.on(Socket.EVENT_DISCONNECT, disConnect);
                mSocket.on("attendRoom", attendRoom);// 방접속시 user 아이디 보내기
                mSocket.on("complete", complete);
                //mSocket.on("disconnect",disConnect);
                mSocket.on("not_complete", not_complete);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        JSONObject data = new JSONObject();
        try {
            System.out.println("attendRoom@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111111111111111111");
            data.put("id", MyGlobals.getInstance().getUser().getU_id());
            mSocket.emit("attendRoom", data);
        } catch (JSONException e) {
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



        // mapInfo로 mapDetail정보까지 가져와야 함.

    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Toast.makeText(getApplicationContext(),"방에 접속했습니다.",Toast.LENGTH_SHORT).show();
            System.out.println("방에 접속했습니다");
        }
    }; //제일처음 접속

   /* private Emitter.Listener disConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mSocket.disconnect();
            mSocket = null;
        }
    };*/

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
                        total_districts.get(Integer.parseInt(received_districtNum)).get(Integer.parseInt(received_index)).setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 100));
                    }
                });


            } catch (JSONException e) {
                System.out.println("complete JsonException");
                e.printStackTrace();
            }

        }
    };

    private Emitter.Listener not_complete = new Emitter.Listener() { //수색불가
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

        mapBounds = new LatLngBounds(
                new LatLng(Double.parseDouble(mapInfo.getM_southWest_latitude()), Double.parseDouble(mapInfo.getM_southWest_longitude())),
                new LatLng(Double.parseDouble(mapInfo.getM_northEast_latitude()), Double.parseDouble(mapInfo.getM_northEast_longitude()))
        );

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

        vertex_list[0] = new LatLng(Double.parseDouble(mapInfo.getM_northWest_latitude()), Double.parseDouble(mapInfo.getM_northWest_longitude()));
        vertex_list[1] = new LatLng(Double.parseDouble(mapInfo.getM_northEast_latitude()), Double.parseDouble(mapInfo.getM_northEast_latitude()));
        vertex_list[2] = new LatLng(Double.parseDouble(mapInfo.getM_southEast_latitude()), Double.parseDouble(mapInfo.getM_southEast_longitude()));
        vertex_list[3] = new LatLng(Double.parseDouble(mapInfo.getM_southWest_latitude()), Double.parseDouble(mapInfo.getM_southWest_longitude()));

        // 중앙점 등록
//        Marker centerPoint = new Marker();
//        centerPoint.setPosition(center_coord);
//        centerPoint.setWidth(50);
//        centerPoint.setHeight(50);
//        centerPoint.setIcon(MarkerIcons.BLACK);
//        centerPoint.setIconTintColor(Color.BLUE);
//        centerPoint.setMap(naverMap);

        // 지도 그리드 생성


        /* Projection을 이용헤 지도좌표로 변환해보려 했지만 실패 */
//        Projection p = naverMap.getProjection();
//        //왼쪽시작점들
//        Marker nw = new Marker();
//        nw.setPosition(vertex_list[0]);
//        nw.setCaptionText("북서점");
//        nw.setMap(naverMap);
//        Marker sw = new Marker();
//        sw.setPosition(vertex_list[3]);
//        sw.setCaptionText("남서점");
//        sw.setMap(naverMap);
//
//        PointF northWest = p.toScreenLocation(vertex_list[0]);
//        PointF southWest = p.toScreenLocation(vertex_list[1]);
//        int count = (int) (mapInfo.getVertical() / (3*mapInfo.getUnit_scale()));
//        double unit = (northWest.y - southWest.y) / count;
//
//        Marker center = new Marker();
//        LatLng l = p.fromScreenLocation(new PointF(northWest.x, (float) ((northWest.y+southWest.y)/2)));
//        center.setPosition(l);
//        center.setCaptionText("가운데");
//        center.setMap(naverMap);


        /* 회전변환 적용한 각 district의 중심점 마커 표시 */
//        ArrayList<Marker> mm = getRotateCenters(center_coord, mapInfo.getUnit_scale());
//        Log.d("MapActivity", "size: " + mm.size());
//        for(Marker m : mm){
//            m.setMap(naverMap);
//        }


//        for(Marker m: createDistrictMarker(center_coord, mapInfo.getUnit_scale())){
//            m.setMap(naverMap);
//        }

        /* 원래방법대로 그리드 그릴 때 */
        /* 원래 중심 좌표 디버깅 중 - 제대로 전달 됨.*/
//        for(Marker m : checkDistrictCenters(center_coord, mapInfo.getUnit_scale())){
//            m.setMap(naverMap);
//        }


//        for(District d:total_districts.getChildren()){
//            d.getGrid().setOutlineColor(COLOR_LINE_WHITE);
//            d.getGrid().setMap(naverMap);
//        }

        /* 전체 영역 확인용 PolygonOverlay */
//        big_polygon = new PolygonOverlay();
//        big_polygon.setCoords(Arrays.asList(
//                vertex_list[0],
//                vertex_list[3],
//                vertex_list[2],
//                vertex_list[1]
//        ));
//        int color = ResourcesCompat.getColor(getResources(), R.color.light_gold, getTheme());
//        big_polygon.setColor(ColorUtils.setAlphaComponent(color, 0));
//        big_polygon.setOutlineWidth(getResources().getDimensionPixelSize(R.dimen.overlay_line_bold_width));
//        big_polygon.setOutlineColor(COLOR_LINE_WHITE);
//        big_polygon.setGlobalZIndex(10);
//        big_polygon.setMap(naverMap);

        // 지도 그리드

        total_districts = createDistricts(center_coord, Double.parseDouble(mapInfo.getM_unit_scale()));
        for (ArrayList<PolygonOverlay> district : total_districts) {
            for (PolygonOverlay p : district) {
                p.setMap(naverMap);
            }
        }

        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        retrofitExService.getMapDetail(mapInfo.getM_id()).enqueue(new Callback<ArrayList<MapDetail>>() {
            @Override
            public void onResponse(Call<ArrayList<MapDetail>> call, Response<ArrayList<MapDetail>> response) {
                System.out.println("onResponse@@@@@@@@@@@@@@@");
                ArrayList<MapDetail> items = response.body();
                for(int i =0;i<items.size();i++){
                    MapDetail item = items.get(i);
                    if(item.getMd_status().equals("1"))
                        total_districts.get(Integer.parseInt(item.getMd_districtNum())).get(Integer.parseInt(item.getMd_index())).setColor(ColorUtils.setAlphaComponent(color_finish, 100));
                    else if(item.getMd_status().equals("0"))
                        total_districts.get(Integer.parseInt(item.getMd_districtNum())).get(Integer.parseInt(item.getMd_index())).setColor(ColorUtils.setAlphaComponent(color_impossible, 100));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MapDetail>> call, Throwable t) {
                System.out.println("onFailure@@@@@@@@@@@@@@@");
            }
        });

        /* 디버깅 중 */
//        ArrayList<PolygonOverlay> district = createDistrict(center_coord, Double.parseDouble(mapInfo.getM_unit_scale()));
//        for (PolygonOverlay p : district) {
//            for (LatLng l : p.getCoords()) {
//                Marker m = new Marker();
//                m.setPosition(l);
//                m.setMap(naverMap);
//            }
//        }


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
                    switch (mapType.toString()) {
                        case "Satellite":
                            if(big_polygon != null)
                                big_polygon.setOutlineColor(COLOR_LINE_WHITE);
                            for (ArrayList<PolygonOverlay> district : total_districts) {
                                for (PolygonOverlay square : district) {
                                    square.setOutlineColor(COLOR_LINE_WHITE);
                                }
                            }
                            break;
                        case "Basic":
                        case "Terrain":
                            if(big_polygon != null)
                                big_polygon.setOutlineColor(COLOR_LINE_BLACK);
                            for (ArrayList<PolygonOverlay> district : total_districts) {
                                for (PolygonOverlay square : district) {
                                    square.setOutlineColor(COLOR_LINE_BLACK);
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

    private ArrayList<ArrayList<PolygonOverlay>> createDistricts(LatLng center, double unit) {
        ArrayList<ArrayList<PolygonOverlay>> grids = new ArrayList<>();

        // 중앙점 찾기
        double offset = unit * 3;
        double up_dist = (Double.parseDouble(mapInfo.getM_up()) - offset / 2) / offset;
        double start_lat = center.latitude
                + LocationDistance.LatitudeInDifference(offset * (up_dist + 1));
        double left_dist = (Double.parseDouble(mapInfo.getM_left()) - offset / 2) / offset;
        double start_lng = center.longitude
                - LocationDistance.LongitudeInDifference(start_lat, offset * left_dist);


        double row = (Double.parseDouble(mapInfo.getM_left()) + Double.parseDouble(mapInfo.getM_right())) / offset;
        double col = (Double.parseDouble(mapInfo.getM_up()) + Double.parseDouble(mapInfo.getM_down())) / offset;


        ArrayList<Marker> markers = new ArrayList<>();
        for (int i = 1; i <= (int) row; i++) {
            LatLng row_temp = new LatLng(start_lat - LocationDistance.LatitudeInDifference(unit * 3) * i, start_lng);
            for (int j = 0; j < col; j++) {
                LatLng col_temp = new LatLng(
                        row_temp.latitude,
                        row_temp.longitude + LocationDistance.LongitudeInDifference(row_temp.latitude, offset) * j
                );
                grids.add(createDistrict(col_temp, unit));
            }
        }
        return grids;
    }

    private ArrayList<Marker> checkDistrictCenters(LatLng center, double unit) {
        ArrayList<ArrayList<PolygonOverlay>> grids = new ArrayList<>();

        // 중앙점 찾기
        double offset = unit * 3;
        double up_dist = (Double.parseDouble(mapInfo.getM_up()) - offset / 2) / offset;
        double start_lat = center.latitude
                + LocationDistance.LatitudeInDifference(offset * (up_dist + 1));
        double left_dist = (Double.parseDouble(mapInfo.getM_left()) - offset / 2) / offset;
        double start_lng = center.longitude
                - LocationDistance.LongitudeInDifference(start_lat, offset * left_dist);

        double row = (Double.parseDouble(mapInfo.getM_unit_scale()) + Double.parseDouble(mapInfo.getM_down())) / offset;
        double col = (Double.parseDouble(mapInfo.getM_left()) + Double.parseDouble(mapInfo.getM_right())) / offset;

        ArrayList<Marker> markers = new ArrayList<>();
        for (int i = 1; i <= (int) row; i++) {
            LatLng row_temp = new LatLng(start_lat - LocationDistance.LatitudeInDifference(unit * 3) * i, start_lng);
            for (int j = 0; j < col; j++) {
                LatLng col_temp = new LatLng(
                        row_temp.latitude,
                        row_temp.longitude + LocationDistance.LongitudeInDifference(row_temp.latitude, offset) * j
                );
                //grids.add(createDistrict(col_temp, unit));
                Marker m = new Marker();
                m.setPosition(col_temp);
                m.setWidth(50);
                m.setHeight(50);
                m.setIcon(MarkerIcons.BLACK);
                m.setIconTintColor(Color.DKGRAY);
                markers.add(m);
            }
        }
        return markers;
    }

    private ArrayList<Marker> getRotateCenters(LatLng center, double unit) {
        ArrayList<ArrayList<PolygonOverlay>> grids = new ArrayList<>();

        // 중앙점 찾기
        double offset = unit * 3;
        double up_dist = (Double.parseDouble(mapInfo.getM_up()) - offset / 2) / offset;
        double start_lat = center.latitude
                + LocationDistance.LatitudeInDifference(offset * (up_dist + 1));
        double left_dist = (Double.parseDouble(mapInfo.getM_left()) - offset / 2) / offset;
        double start_lng = center.longitude
                - LocationDistance.LongitudeInDifference(start_lat, offset * left_dist);

        double row = (Double.parseDouble(mapInfo.getM_unit_scale()) + Double.parseDouble(mapInfo.getM_down())) / offset;
        double col = (Double.parseDouble(mapInfo.getM_left()) + Double.parseDouble(mapInfo.getM_right())) / offset;

        ArrayList<Marker> markers = new ArrayList<>();
        for (int i = 1; i <= (int) row; i++) {
            LatLng row_temp = new LatLng(start_lat - LocationDistance.LatitudeInDifference(unit * 3) * i, start_lng);
            for (int j = 0; j < col; j++) {
                LatLng col_temp = new LatLng(
                        row_temp.latitude,
                        row_temp.longitude + LocationDistance.LongitudeInDifference(row_temp.latitude, offset) * j
                );
                Marker m = new Marker();
                m.setIconTintColor(Color.RED);
                m.setPosition(LocationDistance.rotateTransformation(center, col_temp, Double.parseDouble(mapInfo.getM_rotation())));
                markers.add(m);
            }
        }
        return markers;
    }

    private ArrayList<Marker> createDistrictMarker(LatLng center, /* 축척 */double unit) {
        double offset_x = LocationDistance.LatitudeInDifference(unit);
        double offset_y = LocationDistance.LongitudeInDifference(center.latitude, unit);
        double[] offsets_x = {1.5 * offset_x, 0.5 * offset_x, -0.5 * offset_x, -1.5 * offset_x};
        double[] offsets_y = {-1.5 * offset_y, -0.5 * offset_y, 0.5 * offset_y, 1.5 * offset_y};

        ArrayList<LatLng> coords = new ArrayList<>();
        ArrayList<Marker> markers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double temp_lat = center.latitude + offsets_x[i];
                double temp_lng = center.longitude + offsets_y[j];
                LatLng temp = new LatLng(temp_lat, temp_lng);
                double k = LocationDistance.distance(center, temp, "meter");
                //double offset_lat = LocationDistance.LatitudeInDifference(k* Math.cos(angle_rad));
                //double offset_lng = LocationDistance.LongitudeInDifference(center.latitude, k* Math.sin(angle_rad));
                double offset_lat = LocationDistance.LatitudeInDifference(k);
                double offset_lng = LocationDistance.LongitudeInDifference(center.latitude, k);
                LatLng point = new LatLng(
                        temp_lat + offset_lat,
                        temp_lng - offset_lng
                );
                //LatLng rotate_point = LocationDistance.rotateTransformation(center, point, mapInfo.getBearing());
                Log.d("MapActivity::point", point.latitude + " / " + point.longitude);
                coords.add(point);
                Marker m = new Marker();
                m.setPosition(point);
                m.setWidth(50);
                m.setHeight(50);
                m.setIcon(MarkerIcons.BLACK);
                m.setIconTintColor(Color.GREEN);
                markers.add(m);

            }
        }

        return markers;
    }

    private ArrayList<PolygonOverlay> createDistrict(LatLng center, /* 축척 */double unit) {
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
//                double k = LocationDistance.distance(center, temp, "meter");
//                double offset_lat = LocationDistance.LatitudeInDifference(k);
//                double offset_lng = LocationDistance.LongitudeInDifference(center.latitude, k);
//
//                LatLng point = new LatLng(
//                  temp_lat + offset_lat,
//                        temp_lng - offset_lng
//                );
//                coords.add(point);

            }
        }

        ArrayList<PolygonOverlay> polygons = new ArrayList<>();
        for (double i = 2.5; i < 13; i++) {
            if (i == 5.5 || i == 9.5) continue;

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

    public void mOnPopupClick(int districtNum, int index) {
        Intent intent = new Intent(this, DistrictRecordActivity.class);
        intent.putExtra("district", districtNum);
        intent.putExtra("index", index);
        startActivityForResult(intent, 1);
    }

    public void mOnInfoClick(View v) {
        Toast.makeText(this, "여기 준희가 만든 실종자 정보 팝업화면 비스므리하게 연결하면 됨.", Toast.LENGTH_SHORT).show();
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
                        //color_finish = getResources().getColor(R.color.finish);
                        //total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_finish, 100));
                        try {
                            JSONObject complete_data = new JSONObject();
                            System.out.println("complete@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111111111111111111");
                            String area_districtNum = ""+districtNum;
                            String area_index = ""+index;
                            complete_data.put("mid",mapInfo.getM_id());
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
                        //color_impossible = getResources().getColor(R.color.impossible);
                        //total_districts.get(districtNum).get(index).setColor(ColorUtils.setAlphaComponent(color_impossible, 100));
                        try{
                            JSONObject non_complete_data = new JSONObject();
                            String area_districtNum = ""+districtNum;
                            String area_index = ""+index;
                            non_complete_data.put("mid", mapInfo.getM_id());
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



