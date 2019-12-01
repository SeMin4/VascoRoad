package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.data.MapInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.District;
import com.example.woo.myapplication.data.MapInfo;

import com.example.woo.myapplication.data.Mperson;
import com.example.woo.myapplication.utils.LocationDistance;
import com.google.gson.JsonObject;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.CameraUpdateParams;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Projection;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.File;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private District outerDistrict;
    private LatLng[] vertex_list = new LatLng[4];
    private LatLngBounds mapBounds;
    private MapInfo mapInfo;
    private int scale;
    private int COLOR_LINE_BLACK;
    private int COLOR_LINE_WHITE;
    private int COLOR_FINISH;
    public static Socket mSocket = null;
    public String received_lat;
    public String received_lng;
    public String received_img;
    public String received_content; // received는 수색불가
    NaverMap naverMapInstance;

    public String complete_lat;
    public String complete_lng; //수색완료

    public String m_id;
    Mperson selected;
    Retrofit retrofit = null;
    MyGlobals.RetrofitExService retrofitExService = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        Intent intent = getIntent();
        m_id = intent.getStringExtra("m_id");
        mapInfo = (MapInfo) intent.getSerializableExtra("mapInfo");
        mapInfo.setM_id(m_id);
        selected = (Mperson)intent.getSerializableExtra("selecteditem");


       /* //디비로부터 정보설정(트래킹)
        retrofitExService.setMapDetailData(m_id, ,).enqueue(new Callback<OverlapExamineData>() {
            @Override
            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {

            }

            @Override
            public void onFailure(Call<OverlapExamineData> call, Throwable t) {

            }
        });*/


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
                mSocket.on("makeroom", makeroom);// 방접속시 user 아이디 보내기
                mSocket.on("complete", complete);
                mSocket.on("not_complete", not_complete);
            }
        }catch(URISyntaxException e){
            e.printStackTrace();
        }

        JSONObject data = new JSONObject();
        try{
            System.out.println("makeRoom@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111111111111111111");
            data.put("id", MyGlobals.getInstance().getUser().getU_id());
            data.put("mapid", mapInfo.getM_id());
            mSocket.emit("makeroom",data);
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

        double unit = Double.parseDouble(mapInfo.getM_unit_scale());
        if(unit == 20){
            scale = 4;
        } else if(unit == 30 || unit == 50){
            scale = 8;
        } else if(unit == 100){
            scale = 16;
        } else if(unit == 250){
            scale = 32;
        } else{
            scale = 64;
        }
    }

    private Emitter.Listener makeroom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject receivedData = (JSONObject)args[0];
                System.out.println("msg : @@@@@@@@@@@@@@@"+ receivedData.getString("msg") );
                System.out.println("data : @@@@@@@@@@@@@@@" + receivedData.getString("data"));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Toast.makeText(getApplicationContext(),"방에 접속했습니다.",Toast.LENGTH_SHORT).show();
            System.out.println("방에 접속했습니다");
        }
    }; //제일처음 접속


    private Emitter.Listener complete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject receivedData = (JSONObject) args[0];
                complete_lat = receivedData.getString("lat");
                complete_lng = receivedData.getString("lng");
                System.out.println("lat : + "+ complete_lat+"@@@@@@@@@@@@@@@");
                System.out.println("lng : " + complete_lng + "@@@@@@@@@@@@@");
                System.out.println("실행됨@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // total_districts.get(Integer.parseInt(received_districtNum)).get(Integer.parseInt(received_index)).setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 100));
                        Marker foundLocation = new Marker();
                        foundLocation.setPosition(new LatLng(Double.parseDouble(complete_lat), Double.parseDouble(complete_lng)));
                        foundLocation.setMap(naverMapInstance);
                    }
                });


            } catch (JSONException e) {
                System.out.println("complete JsonException");
                e.printStackTrace();
            }
        }
    }; //수색완료

    private Emitter.Listener not_complete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject receivedData = (JSONObject)args[0];
                received_lat = receivedData.getString("lat");
                received_lng = receivedData.getString("lng");
                received_img = receivedData.getString("photo_name");
                received_content = receivedData.getString("desc");
                System.out.println("lat : "+received_lat+" lng : "+received_lng+" img : "+received_img+" content: "+received_content);
                //2개의 정보는 위치정보 2개의정보는 핀을 클릭하면 내용과 사진이나와야한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // total_districts.get(Integer.parseInt(received_districtNum2)).get(Integer.parseInt(received_index2)).setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 100));
                        Marker notComplete = new Marker();
                        notComplete.setPosition(new LatLng(Double.parseDouble(received_lat), Double.parseDouble(received_lng)));
                        notComplete.setMap(naverMapInstance);
                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    };//수색불가

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
        //mSocket.off("attendRoom",attendRoom);
       // mSocket.off("complete",complete);
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
        // 지도 줌버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);
        // 현위치 버튼 활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationSource(locationSource);

        // 위치 변경 리스너 등록
        naverMap.addOnLocationChangeListener(location -> {
            /* 현재 위치 획득 */
            LatLng cur = new LatLng(location.getLatitude(), location.getLongitude());

            /* 속한 OuterDistrict 계산 */
            int outerIndex = findDistrictCoord(outerDistrict.getChildren().get(0), cur, 8);
            if(outerIndex < 0 || outerIndex >= 8){
                Toast.makeText(NewMapActivity.this, "현위치가 지도 구역을 벗어났습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            /* 속한 InnerDistrict 계산 */
            District child = outerDistrict.getChildren().get(outerIndex);
            int innerIndex = findDistrictCoord(child.getChildren().get(0), cur, scale);

            District grandChild = child.getChildren().get(innerIndex);

            if(grandChild.getFootPrint().getColor() != COLOR_FINISH){
                grandChild.getFootPrint().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 250));
                grandChild.getFootPrint().setMap(naverMap);
            }
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

        /* 쓰레드(지도 그리드 생성) 등록 */
        Projection projection = naverMap.getProjection();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.schedule(() -> {
            // 백그라운드 스레드
            outerDistrict = new District(
                    new LatLng(Double.parseDouble(mapInfo.getM_northWest_latitude()), Double.parseDouble(mapInfo.getM_northWest_longitude())),
                    new LatLng(Double.parseDouble(mapInfo.getM_northEast_latitude()), Double.parseDouble(mapInfo.getM_northEast_longitude())),
                    new LatLng(Double.parseDouble(mapInfo.getM_southEast_latitude()), Double.parseDouble(mapInfo.getM_southEast_longitude())),
                    new LatLng(Double.parseDouble(mapInfo.getM_southWest_latitude()), Double.parseDouble(mapInfo.getM_southWest_longitude()))
            );
            outerDistrict.setCenter(center_coord);
            outerDistrict = createOuterDistrict(projection, outerDistrict);

            handler.post(() -> {
                // 메인 스레드
                for(District child: outerDistrict.getChildren()){
                    child.addToMap(naverMap, COLOR_LINE_WHITE, 5);
                }

            });
        }, 200, TimeUnit.MILLISECONDS);


        /* 지도 타입 변경 스피너 등록 */
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
                    if(outerDistrict != null) {
                        switch (mapType.toString()) {
                            case "Satellite":
                                for (District child : outerDistrict.getChildren()) {
                                    child.getGrid().setOutlineColor(COLOR_LINE_WHITE);
                                }
                                break;
                            case "Basic":
                            case "Terrain":
                                for (District child : outerDistrict.getChildren()) {
                                    child.getGrid().setOutlineColor(COLOR_LINE_BLACK);
                                }
                                break;

                        }

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* 구역 롱클릭 이벤트 생성 */
        naverMap.setOnMapClickListener((pointF, latLng) -> {
            int selectedIdx = findDistrictCoord(outerDistrict.getChildren().get(0), latLng, 8);
            if(0<=selectedIdx && selectedIdx <= 63) {
                mOnPopupClick(outerDistrict.getChildren().get(selectedIdx));
            }
        });
        naverMapInstance = naverMap;
    }

    private int findDistrictCoord(District std, LatLng C, int rowNum){
        // A --- B
        // | \   |   theta = angle between A->B and A->C
        // D_ \ _|   A->B = (a1, a2)
        //     \ |   A->C = (b1, b2)
        //       C
        LatLng A = std.getNorthWest();
        LatLng B = std.getNorthEast();
        LatLng D = std.getSouthWest();
        double a1 = B.latitude - A.latitude;
        double a2 = B.longitude - A.longitude;
        double b1 = C.latitude - A.latitude;
        double b2 = C.longitude - A.longitude;

        double theta = LocationDistance.radByInnerProduct(a1, a2, b1, b2);
        Log.d("내적", "theta: " + theta);

        double cDistance = Math.sqrt(b1*b1 + b2*b2) * Math.cos(theta);
        double rDistance = Math.sqrt(b1*b1 + b2*b2) * Math.sin(theta);
        double cOffset = Math.sqrt(Math.pow(B.latitude - A.latitude, 2) + Math.pow(B.longitude - A.longitude, 2));
        double rOffset = Math.sqrt(Math.pow(A.latitude - D.latitude, 2) + Math.pow(A.longitude - D.longitude, 2));
        Log.d("내적", "cDist: " + cDistance + "cOuter: " + cOffset);

        int row = (int) Math.floor(rDistance/rOffset);
        int col = (int) Math.floor(cDistance/cOffset);
        Log.d("내적", "row: " + row + " col: " + col);

        return row * rowNum + col;
    }

    private District createOuterDistrict(Projection projection, District outer){
        District retObject = new District(
                outer.getGrid().getCoords().get(0),
                outer.getGrid().getCoords().get(1),
                outer.getGrid().getCoords().get(2),
                outer.getGrid().getCoords().get(3));
        retObject.setCenter(outer.getCenter());

        /* bearing을 고려한 화면좌표 수집 */
        // 좌측, 우측 기준 설정
        int[] starts = {0, 0, 4, 0, 2, 4, 6};
        int[] ends   = {8, 4, 8, 2, 4, 6, 8};
        PointF[] left = new PointF[9];
        PointF[] right = new PointF[9];
        left[0] = projection.toScreenLocation(outer.getNorthWest());
        left[8] = projection.toScreenLocation(outer.getSouthWest());
        right[0] = projection.toScreenLocation(outer.getNorthEast());
        right[8] = projection.toScreenLocation(outer.getSouthEast());
        for(int idx = 0; idx < 7; idx++){
            int i = (starts[idx] + ends[idx]) / 2;
            left[i] = new PointF((left[starts[idx]].x + left[ends[idx]].x) / 2, (left[starts[idx]].y + left[ends[idx]].y) / 2);
            right[i] = new PointF((right[starts[idx]].x + right[ends[idx]].x) / 2, (right[starts[idx]].y + right[ends[idx]].y) / 2);
        }
        // 중간 좌표들 수집
        PointF[][] allPoints = new PointF[9][9];
        for(int i = 0; i < 9; i++){
            allPoints[i][0] = left[i];
            allPoints[i][8] = right[i];

            for(int idx = 0; idx < 7; idx++){
                int k = (starts[idx] + ends[idx]) / 2;
                allPoints[i][k] = new PointF((allPoints[i][starts[idx]].x + allPoints[i][ends[idx]].x) / 2,(allPoints[i][starts[idx]].y + allPoints[i][ends[idx]].y) / 2);
            }

        }

        // 화면좌표에서 위경도로 변환 후 polygon 생성
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                District child = new District(
                        projection.fromScreenLocation(allPoints[i][j]),
                        projection.fromScreenLocation(allPoints[i][j+1]),
                        projection.fromScreenLocation(allPoints[i+1][j+1]),
                        projection.fromScreenLocation(allPoints[i+1][j])
                );
                child.setRowIdx(i);
                child.setColIdx(j);
                // 중심점 계산
                float row = (allPoints[i][j].x +allPoints[i][j+1].x) / 2;
                float col = (allPoints[i][j].y +allPoints[i+1][j].y) / 2;
                child.setCenter(projection.fromScreenLocation(new PointF(row, col)));
                child.setChildren(createInnerDistrict(projection, child));
                retObject.getChildren().add(child);
            }
        }

        return retObject;
    }

   private ArrayList<District> createInnerDistrict(Projection projection, District parent){
        ArrayList<District> retList = new ArrayList<>();

        // 좌측, 우측 좌표 수
       PointF[] left = new PointF[scale+1];
       PointF[] right = new PointF[scale+1];
       left[0] = projection.toScreenLocation(parent.getNorthWest());
       left[scale] = projection.toScreenLocation(parent.getSouthWest());
       right[0] = projection.toScreenLocation(parent.getNorthEast());
       right[scale] = projection.toScreenLocation(parent.getSouthEast());

       ArrayList<Point> order = createOrder(scale);
       for(Point p: order){
           int start = p.x;
           int end = p.y;
           int middle = (p.x + p.y) / 2;

           left[middle] = new PointF(
                   (left[start].x + left[end].x) / 2,
                   (left[start].y + left[end].y) / 2
           );
           right[middle] = new PointF(
                   (right[start].x + right[end].x) / 2,
                   (right[start].y + right[end].y) / 2
           );
       }

       // 중간 좌표들 수집
       PointF[][] allPoints = new PointF[scale+1][scale+1];
       for(int row = 0; row < scale+1; ++row){
           allPoints[row][0] = left[row];
           allPoints[row][scale] = right[row];

           for(Point p:order){
               int start = p.x;
               int end = p.y;
               int middle = (p.x + p.y) / 2;
               allPoints[row][middle] = new PointF(
                       (allPoints[row][start].x + allPoints[row][end].x) / 2,
                       (allPoints[row][start].y + allPoints[row][end].y) / 2
               );
           }
       }

       // 화면좌표에서 지도좌표로 변환
       for(int i = 0; i < scale; i++){
           for(int j = 0; j < scale; j++){
               District child = new District(
                       projection.fromScreenLocation(allPoints[i][j]),
                       projection.fromScreenLocation(allPoints[i][j+1]),
                       projection.fromScreenLocation(allPoints[i+1][j+1]),
                       projection.fromScreenLocation(allPoints[i+1][j])
               );
               float row = (allPoints[i][j].x +allPoints[i][j+1].x) / 2;
               float col = (allPoints[i][j].y +allPoints[i+1][j].y) / 2;
               child.setCenter(projection.fromScreenLocation(new PointF(row, col)));
               retList.add(child);
           }
       }

       return retList;
   }

    private ArrayList<Point> createOrder(int scale){
        ArrayList<Point> retList = new ArrayList<>();
        Stack<Integer> starts = new Stack<>();
        Stack<Integer> ends = new Stack<>();
        starts.add(0);
        ends.add(scale);

        while(!starts.empty()){
            int start = starts.pop();
            int end = ends.pop();

            if(end-start >= 2) {
                retList.add(new Point(start, end));
                int middle = (start + end) / 2;
                starts.add(start);
                starts.add(middle);
                ends.add(middle);
                ends.add(end);
            }
        }

        return retList;
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

    public void mOnPopupClick(District district) {
        Intent intent = new Intent(this, DistrictActivity.class);
        intent.putExtra("row", district.getRowIdx());
        intent.putExtra("col", district.getColIdx());
        System.out.println("mapinfo : "+ mapInfo.getM_id());
        intent.putExtra("mapId", Integer.parseInt(mapInfo.getM_id()));
        List<LatLng> coords = district.getGrid().getCoords();
        intent.putExtra("coords", (Serializable) coords);
        intent.putExtra("index",(district.getRowIdx())*8+district.getColIdx());
        startActivityForResult(intent, 1);
    }

    public void mOnInfoClick(View v) {
        Intent intent = new Intent(this,MissingInfoActivity.class);
        intent.putExtra("selecteditem",selected);
        startActivityForResult(intent,1);
    }

}

