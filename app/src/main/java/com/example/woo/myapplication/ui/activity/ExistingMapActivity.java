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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.District;
import com.example.woo.myapplication.data.MapDetail;
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
import com.naver.maps.map.Projection;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

public class ExistingMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    protected TextView zoomLevel;
    private ArrayList<ArrayList<PolygonOverlay>> total_districts;
    private District outerDistrict;
    private LatLng[] vertex_list = new LatLng[4];
    private LatLngBounds mapBounds;
    private MapInfo mapInfo;
    private int scale;
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
    Button mpersoninfo;

    Mperson selected;

   /*private void uploadImage(String filePath){
        File file = new File(filePath);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload",file.getName(),fileReqBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),"image-type");

        String name,age,time,p_string,latitude,longitude,desc,photo;

        HashMap<String,String> input = new HashMap<>();
        input.put("p_name",name);
        input.put("p_age",age);
        input.put("p_time",time);
        input.put("p_place_string",p_string);
        input.put("p_place_latitude",latitude);
        input.put("p_place_longitude",longitude);
        input.put("p_place_description",desc);

        retrofitExService.postInsertMperson(input,part,description).enqueue(new Callback<OverlapExamineData>() {
            @Override
            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                System.out.println("onResponse 호출됨@@@@@@@@@@@@@@@");
                OverlapExamineData data = response.body();
                if(data.getOverlap_examine().equals("yes")){
                    System.out.println("yes");
                    Toast.makeText(getApplicationContext(),"insert 성공",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"insert 실패",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                System.out.println("onFailure 호출됨@@@@@@@@@@@@@@@");
                Toast.makeText(getApplicationContext(),"insert 실패",Toast.LENGTH_SHORT).show()
            }
        });
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mapInfo = (MapInfo) intent.getSerializableExtra("mapInfo");
        selected = (Mperson) intent.getSerializableExtra("selecteditem");

        COLOR_LINE_BLACK = ResourcesCompat.getColor(getResources(), R.color.black, getTheme());
        COLOR_LINE_WHITE = ResourcesCompat.getColor(getResources(), R.color.white, getTheme());
        COLOR_FINISH = ResourcesCompat.getColor(getResources(), R.color.finish, getTheme());
        color_finish = getResources().getColor(R.color.finish);
        color_impossible = getResources().getColor(R.color.impossible);


        try {
            if (mSocket == null) {
                mSocket = IO.socket("http://13.125.174.158:9000");
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
            data.put("mapid", mapInfo.getM_id());
            mSocket.emit("attendRoom", data);
        } catch (JSONException e) {
            System.out.println("attendRoom 에러");
            e.printStackTrace();
        }



        mpersoninfo = findViewById(R.id.button_mPerson_info);

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
            /* 현재 위치 획득 */
            LatLng cur = new LatLng(location.getLatitude(), location.getLongitude());

            /* 속한 OuterDistrict 계산 */
            int outerIndex = findDistrictCoord(outerDistrict.getChildren().get(0), cur, 8);

            /* 속한 InnerDistrict 계산 */
            District child = outerDistrict.getChildren().get(outerIndex);
            if(!child.getGrid().getBounds().contains(cur)){
                Toast.makeText(ExistingMapActivity.this, "현위치가 그리드를 벗어났습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            int innerIndex = findDistrictCoord(child.getChildren().get(0), cur, scale);

            District grandChild = child.getChildren().get(innerIndex);
            // grandChild.setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 150));
            // grandChild.setMap(naverMap);

            if(grandChild.getFootPrint().getColor() != COLOR_FINISH){
                grandChild.getFootPrint().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 250));
                grandChild.getFootPrint().setMap(naverMap);
            }
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
        intent.putExtra("mapId", Double.parseDouble(mapInfo.getM_id()));
        List<LatLng> coords = district.getGrid().getCoords();
        intent.putExtra("coords", (Serializable) coords);
        startActivityForResult(intent, 1);
    }

    public void mOnInfoClick(View v) {
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
                        System.out.println("content : "+content);
                        districtNum = data.getIntExtra("district", -1);
                        index = data.getIntExtra("location", -1);
                        String imagePath = data.getStringExtra("imagePath");
                        System.out.println("imagepath : "+imagePath);
                        if(imagePath!=null)
                            //uploadImage(imagePath);
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

     /*private void uploadImage(String filePath){
        File file = new File(filePath);
        System.out.println("upload 이미지@@@@@@@@@@@@");

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),mapInfo.getM_id());
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload",file.getName(),fileReqBody);


       /* Call<OverlapExamineData> c = retrofitExService.postNotComplete(part,description);
        try{
            OverlapExamineData data = c.execute().body();
            System.out.println("overlapdata : "+data.getOverlap_examine());
        }catch (IOException e){
            e.printStackTrace();
        }*/


        /*retrofitExService.postNotComplete(description,part).enqueue(new Callback<OverlapExamineData>() {
            @Override
            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                System.out.println("onResponse 호출됨@@@@@@@@@@@@@@@");
                OverlapExamineData data = response.body();
                if(data.getOverlap_examine().equals("yes")){
                    System.out.println("yes");
                   // Toast.makeText(getApplicationContext(),"insert 성공",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),data.getOverlap_examine(),Toast.LENGTH_SHORT).show();
                }else{
                   // Toast.makeText(getApplicationContext(),"insert 실패",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                System.out.println("onFailure 호출됨@@@@@@@@@@@@@@@");
                Toast.makeText(getApplicationContext(),"insert 실패",Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}



