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
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.CompleteData;
import com.example.woo.myapplication.data.DetailData;
import com.example.woo.myapplication.data.District;
import com.example.woo.myapplication.data.MapInfo;
import com.example.woo.myapplication.data.Mperson;
import com.example.woo.myapplication.data.Not_Complete_Data;
import com.example.woo.myapplication.utils.FoundMarker;
import com.example.woo.myapplication.utils.LocationDistance;
import com.example.woo.myapplication.utils.NotCompleteMarker;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ExistingMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private District outerDistrict;
    private LatLngBounds mapBounds;
    private MapInfo mapInfo;
    private NaverMap naverMapInstance;
    private int scale;
    private int COLOR_LINE_BLACK;
    private int COLOR_LINE_WHITE;
    private int COLOR_FINISH;
    public static Socket mSocket=null;
    public String lat;
    public String lng;
    public String lat2;
    public String lng2;
    public String desc;
    public String photo_name;
    public int colorFound;
    public int colorImpossible;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;
    Button mpersoninfo;
    Mperson selected;

    String prev_lat = null;
    String prev_lng = null;
    String cur_lat = null;
    String cur_lng = null;




    String[][] Run_Length;
    //row, col 전체 512, 512 배열 처럼 전체 크기
    int run_length_row;
    int run_length_col;
    //outerindex로 그곳에 db에 저장할 index_num , district_num 알아내야 하는거
    int run_length_index_num;
    int run_length_district_num;
    boolean [][] Mark ;
    ArrayList<Integer> sendOuterIndex;
    String each_Index;




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
        colorFound = getResources().getColor(R.color.finish);
        colorImpossible = getResources().getColor(R.color.impossible);
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();


        sendOuterIndex = new ArrayList<>();

        try {
            if (mSocket == null) {
                mSocket = IO.socket("http://13.125.174.158:9000");
                mSocket.connect();
                //이벤트 등록
                mSocket.on(Socket.EVENT_CONNECT, onConnect); //방 접속시;
                //mSocket.on(Socket.EVENT_DISCONNECT, disConnect);
                mSocket.on("attendRoom", attendRoom);// 방접속시 user 아이디 보내기
                mSocket.on("complete", complete);
                mSocket.on("seeroad",seeroad); // 보여주기용
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
        run_length_row = 8 * scale;
        run_length_col = 8 * scale;
        Mark = new boolean[run_length_row][run_length_col];
        // mapInfo로 mapDetail정보까지 가져와야 함.






    }

    private Emitter.Listener seeroad = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject recived_data = (JSONObject)args[0];
                String lat = recived_data.getString("lat");
                String lng = recived_data.getString("lng");
                String uid = recived_data.getString("uid");
                Log.d("오삼삼","uid : "+uid+ " lat :"+lat +" , lng:"+lng);

                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.schedule(() -> {
                    // 백그라운드 스레드
                    LatLng cur = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    int outerIndex = findDistrictCoord(outerDistrict.getChildren().get(0), cur, 8);

                    handler.post(() -> {
                        // 메인 스레드

                        /* 속한 InnerDistrict 계산 */
                        District child = outerDistrict.getChildren().get(outerIndex);
                        if(!child.getGrid().getBounds().contains(cur)){
                            Toast.makeText(ExistingMapActivity.this, "현위치가 그리드를 벗어났습니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        int innerIndex = findDistrictCoord(child.getChildren().get(0), cur, scale);

                        District grandChild = child.getChildren().get(innerIndex);

                        if(grandChild.getFootPrint().getColor() != COLOR_FINISH){
                            Log.d("오삼삼","inner : "+innerIndex+" outter : "+outerIndex);
                            Log.d("오삼삼","ㅂㅈㄷㅂㅈㄷ"+MyGlobals.getInstance().getUser().getU_id());
                            if(MyGlobals.getInstance().getUser().getU_email().equals("admin")) {
                                Log.d("오삼삼","들어왔습니다.");
                                grandChild.getFootPrint().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 250));
                                grandChild.getFootPrint().setMap(naverMapInstance);
                            }
                        }
                    });
                }, 0, TimeUnit.MILLISECONDS);




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
                lat = receivedData.getString("lat");
                lng = receivedData.getString("lng");
                System.out.println("lat : " + receivedData.getString("lat") + "@@@@@@@@@@@@@@@");
                System.out.println("lng : " + receivedData.getString("lng") + "@@@@@@@@@@@@@");
                System.out.println("실행됨@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                FoundMarker found = new FoundMarker(colorFound);
                found.setPosition(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        found.setMap(naverMapInstance);
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
                Log.d("오삼삼","not_complete");
                JSONObject receivedData = (JSONObject)args[0];

                lat2 = receivedData.getString("lat");
                lng2 = receivedData.getString("lng");
                desc = receivedData.getString("desc");
                photo_name = receivedData.getString("photo_name");

                NotCompleteMarker notComplete = new NotCompleteMarker(photo_name, desc, colorImpossible);
                notComplete.setPosition(new LatLng(Double.parseDouble(lat2), Double.parseDouble(lng2)));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notComplete.setMap(naverMapInstance);
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
            /* 현재 위치 획득 */
            LatLng cur = new LatLng(location.getLatitude(), location.getLongitude());
            cur_lat = ""+cur.latitude;
            cur_lng = ""+cur.longitude;
            /* 속한 OuterDistrict 계산 */
            int outerIndex = findDistrictCoord(outerDistrict.getChildren().get(0), cur, 8);
            Log.d("에러", "outerIndex: " + outerIndex);
            if(outerIndex < 0 || outerIndex >= 64){
                Toast.makeText(ExistingMapActivity.this, "현위치가 지도 구역을 벗어났습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            /* 속한 InnerDistrict 계산 */
            District child = outerDistrict.getChildren().get(outerIndex);
            int innerIndex = findDistrictCoord(child.getChildren().get(0), cur, scale);

            District grandChild = child.getChildren().get(innerIndex);

            if(grandChild.getFootPrint().getColor() != COLOR_FINISH){
                grandChild.getFootPrint().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 250));
                //8*8 배열
                grandChild.setVisit(true);
                grandChild.getFootPrint().setMap(naverMap);
                Mark[(outerIndex/8*scale)+ (innerIndex/scale)][(outerIndex%8*scale) + (innerIndex%scale)] = true;
                if(!sendOuterIndex.contains(outerIndex))
                    sendOuterIndex.add(outerIndex);
            }

            //자기위치 데이터보내기 데이터보내기

            if((prev_lat != cur_lat) || (prev_lng != cur_lng) )
            {
                try {
                    ArrayList<String> row_array = new ArrayList<>();
                    for(int i = 0;i<sendOuterIndex.size();i++){
                        run_length_index_num = sendOuterIndex.get(i) / 8;
                        run_length_district_num = sendOuterIndex.get(i) % 8;
                        System.out.println("OuterIndex" + sendOuterIndex.get(i));
                        char row_data = 0;
                        each_Index = "";
                        for (int j = run_length_index_num * scale; j < run_length_index_num * scale + scale; j++) {
                            char one_discriminant = 0;
                            boolean value_boolean = true;
                            //row_data 정하기
                            one_discriminant = (char)((char)(row_data*4) | one_discriminant);
                            //첫번째의 캐릭터의 value bit 정하기
                            if(Mark[j][run_length_district_num * scale] == true){
                                one_discriminant = (char)((char)1 | one_discriminant);
                                value_boolean = true;
                            }
                            else{
                                one_discriminant = (char)((char)0 | one_discriminant);
                                value_boolean = false;
                            }
                            //각각의 열에 대한 스트링
                            String each_row = "";
                            String detail_each_row = "";
                            char cnt = 0;
                            for (int k = run_length_district_num * scale; k < run_length_district_num * scale + scale; k++) {
                                if(Mark[j][k] == value_boolean){
                                    cnt+=1;
                                }
                                else{
                                    if(value_boolean == true){
                                        detail_each_row = detail_each_row.concat("t");
//                                        detail_each_row = detail_each_row.concat(Integer.toString((int)cnt));
//                                        System.out.println("cnt :" + (int)cnt);
                                        value_boolean = false;
//                                        cnt = 0;
                                    }
                                    else{
                                        detail_each_row = detail_each_row.concat("f");
//                                        detail_each_row = detail_each_row.concat(Integer.toString((int)cnt));
                                        value_boolean = true;
//                                        cnt = 0;
                                    }
                                    detail_each_row = detail_each_row.concat(Integer.toString((int)cnt));
                                    System.out.println("each_detail: " +  detail_each_row);
                                }
                            }
                            char discriminant_bit = 0;
                            if(cnt == scale){
                                discriminant_bit = 2;
                            }
                            else{
                                discriminant_bit = 0;
                            }
                            one_discriminant = (char)(one_discriminant | discriminant_bit);
                            each_row = each_row.concat(Integer.toString((int)one_discriminant));
                            each_row = each_row.concat(detail_each_row);

                            row_data++;
                            row_array.add(each_row);
                            try{
                                if((Integer.parseInt(row_array.get(row_array.size()-1)) - Integer.parseInt(row_array.get(row_array.size()-2))) %4 == 0){
                                    if((Integer.parseInt(row_array.get(row_array.size()-2)) - Integer.parseInt(row_array.get(row_array.size()-3))) %4 == 0 ){
                                        row_array.remove(row_array.size()-2);
                                    }
                                }
                            }catch(NumberFormatException numberformatException){

                            }catch(IndexOutOfBoundsException e1){

                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        for (String row:
                             row_array) {
                            each_Index = each_Index.concat(row);
                            each_Index = each_Index.concat(",");
                        }

                        System.out.println("Each_Index : " + each_Index);
                    }

                    JSONObject data = new JSONObject();
                    data.put("mid",mapInfo.getM_id());
                    data.put("uid",MyGlobals.getInstance().getUser().getU_id());
                    data.put("lat", cur_lat);
                    data.put("lng", cur_lng);
                    data.put("index",sendOuterIndex.get(0).toString());
                    data.put("run_length",each_Index);
                    prev_lat = cur_lat;
                    prev_lng = cur_lng;
                    sendOuterIndex.clear();
                    mSocket.emit("seeroad", data);
                }catch (JSONException e){
                    e.printStackTrace();
                }
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

        //디비로부터 정보받아오기(완료,불가,트래킹)



//        retrofitExService.getMapDetailData(mapInfo.getM_id()).enqueue(new Callback<ArrayList<DetailData>>() {
//            @Override
//            public void onResponse(Call<ArrayList<DetailData>> call, Response<ArrayList<DetailData>> response) {
//                System.out.println("getMapDetail onResponse@@@@@@@@@@@@@@@@@");
//                ArrayList<DetailData> data =  response.body(); //트래킹 데이터가 들어가있음
//                System.out.println("size@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+data.size());
//                for (int a = 0; a < data.size(); a++) {
//                    //for(int i =0;i<data.size();i++)
//                    //    Log.d("오세민","data : "+data.get(i).getMd_index() +" "+data.get(i).getMd_inner_scale()+" "+data.get(i).getMd_run_length());
//
//                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////Run-Length Algorithm/////////////////////////////////////////////////////////////////////////////////////////
//                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                    String runlength = data.get(a).getMd_run_length();
//                    int outer_index = Integer.parseInt(data.get(a).getMd_index());
//                    String[] temp;
//
//                    temp = runlength.split(",");
//                    for (int i = 0; i < temp.length; i++) {
//                        System.out.println(temp[i]);
//                    }
//                    int[] int_tmp = new int[temp.length];
//                    int i = 0;
//                    int prev_row = 0;
//                    Boolean prev_data_type = null;
//                    int start = 0;
//
//
//                    for (i = 0; i < temp.length; i++) {
//                        try {
//                            int_tmp[i] = Integer.parseInt(temp[i]);
//                            int row = int_tmp[i] / 4;
//                            Boolean data_type = null;
//                            if (int_tmp[i] % 2 == 0) {
//                                data_type = false;
//                            } else {
//                                data_type = true;
//                            }
//                            if (prev_data_type == null) {
//                                for (int j = (outer_index % 8 * scale); j < (outer_index % 8 * scale) + scale; j++) {
//                                    Mark[(outer_index / 8 * scale) + row][j] = data_type;
//                                }
//                                prev_data_type = data_type;
//                                prev_row = row;
//                            } else if (data_type == prev_data_type) {
//                                for (int j = prev_row + 1; j <= row; j++) {
//                                    for (int k = (outer_index % 8 * scale); k < (outer_index % 8 * scale) + scale; k++) {
//                                        Mark[(outer_index / 8 * scale) + j][k] = data_type;
//                                        prev_data_type = data_type;
//                                        prev_row = row;
//                                    }
//                                }
//                            }
//                        } catch (NumberFormatException e1) {
//                            prev_data_type = null;
//                            String tmp = temp[i].split("t")[0];
//                            tmp = tmp.split("f")[0];
//                            int row_info = Integer.parseInt(tmp);
//                            int row = row_info / 4;
//                            String detail = temp[i].substring(tmp.length(), temp[i].length());
//                            int cnt = 0;
//                            while (detail != null) {
//                                int t_index = detail.indexOf('t');
//                                int f_index = detail.indexOf('f');
//                                if (t_index == -1) {
//                                    int f_count = Integer.parseInt(detail.substring(1));
//                                    int for_cnt = cnt;
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + f_count; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = false;
//                                        cnt += 1;
//                                    }
//                                    for_cnt = cnt;
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + scale; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = true;
//                                        cnt += 1;
//                                    }
//                                    detail = null;
//                                } else if (f_index == -1) {
//                                    int t_count = Integer.parseInt(detail.substring(1));
//                                    int for_cnt = cnt;
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + t_count; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = true;
//                                        cnt += 1;
//                                    }
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + scale; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = false;
//                                        cnt += 1;
//                                    }
//                                    detail = null;
//                                } else if (t_index < f_index) {
//                                    String t_tmp_string = detail.split("f")[0];
//                                    int t_count = Integer.parseInt(t_tmp_string.substring(1));
//                                    int for_cnt = cnt;
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + t_count; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = true;
//                                        cnt += 1;
//                                    }
//                                    detail = detail.substring(t_tmp_string.length());
//                                } else if (t_index > f_index) {
//                                    String f_tmp_string = detail.split("t")[0];
//                                    int f_count = Integer.parseInt(f_tmp_string.substring(1));
//                                    int for_cnt = cnt;
//                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + f_count; j++) {
//                                        Mark[(outer_index / 8 * scale) + row][j] = false;
//                                        cnt += 1;
//                                    }
//                                    detail = detail.substring(f_tmp_string.length());
//                                }
//                            }
//                            //i += 1;
//                            continue;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<DetailData>> call, Throwable t) {
//                System.out.println("onFailure@@@@@@@@@@@@@@@@@222222222222");
//                System.out.println("t: " + t);
//            }
//        });


        /*retrofit = MyGlobals.getInstance().getRetrofit();  //admin처리 & 내가왔던길 처리
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        retrofitExService.getMapDetail(mapInfo.getM_id()).enqueue(new Callback<ArrayList<MapDetail>>() {
            @Override
            public void onResponse(Call<ArrayList<MapDetail>> call, Response<ArrayList<MapDetail>> response) {
                System.out.println("onResponse@@@@@@@@@@@@@@@");
                ArrayList<MapDetail> items = response.body();
                for(int i =0;i<items.size();i++){
                    MapDetail item = items.get(i);
                    if(item.getMd_status().equals("1"))
                        total_districts.get(Integer.parseInt(item.getMd_districtNum())).get(Integer.parseInt(item.getMd_index())).setColor(ColorUtils.setAlphaComponent(colorFound, 100));
                    else if(item.getMd_status().equals("0"))
                        total_districts.get(Integer.parseInt(item.getMd_districtNum())).get(Integer.parseInt(item.getMd_index())).setColor(ColorUtils.setAlphaComponent(colorImpossible, 100));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MapDetail>> call, Throwable t) {
                System.out.println("onFailure@@@@@@@@@@@@@@@");
            }
        });*/

        /* 쓰레드(지도 그리드 생성) 등록 */
        Projection projection = naverMap.getProjection();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.schedule(() -> {
            // 백그라운드 스레드

            ///retrofit 동기처리

            Call<ArrayList<DetailData>> call = retrofitExService.getMapDetailData(mapInfo.getM_id());
            try{
                ArrayList<DetailData> data = call.execute().body();
                System.out.println("size@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+data.size());
                for (int a = 0; a < data.size(); a++) {
                    //for(int i =0;i<data.size();i++)
                    //    Log.d("오세민","data : "+data.get(i).getMd_index() +" "+data.get(i).getMd_inner_scale()+" "+data.get(i).getMd_run_length());

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////Run-Length Algorithm/////////////////////////////////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    String runlength = data.get(a).getMd_run_length();
                    int outer_index = Integer.parseInt(data.get(a).getMd_index());
                    String[] temp;

                    temp = runlength.split(",");
                    for (int i = 0; i < temp.length; i++) {
                        System.out.println(temp[i]);
                    }
                    int[] int_tmp = new int[temp.length];
                    int i = 0;
                    int prev_row = 0;
                    Boolean prev_data_type = null;
                    int start = 0;


                    for (i = 0; i < temp.length; i++) {
                        try {
                            int_tmp[i] = Integer.parseInt(temp[i]);
                            int row = int_tmp[i] / 4;
                            Boolean data_type = null;
                            if (int_tmp[i] % 2 == 0) {
                                data_type = false;
                            } else {
                                data_type = true;
                            }
                            if (prev_data_type == null) {
                                for (int j = (outer_index % 8 * scale); j < (outer_index % 8 * scale) + scale; j++) {
                                    Mark[(outer_index / 8 * scale) + row][j] = data_type;
                                }
                                prev_data_type = data_type;
                                prev_row = row;
                            } else if (data_type == prev_data_type) {
                                for (int j = prev_row + 1; j <= row; j++) {
                                    for (int k = (outer_index % 8 * scale); k < (outer_index % 8 * scale) + scale; k++) {
                                        Mark[(outer_index / 8 * scale) + j][k] = data_type;
                                        prev_data_type = data_type;
                                        prev_row = row;
                                    }
                                }
                            }
                        } catch (NumberFormatException e1) {
                            prev_data_type = null;
                            String tmp = temp[i].split("t")[0];
                            tmp = tmp.split("f")[0];
                            int row_info = Integer.parseInt(tmp);
                            int row = row_info / 4;
                            String detail = temp[i].substring(tmp.length(), temp[i].length());
                            int cnt = 0;
                            while (detail != null) {
                                int t_index = detail.indexOf('t');
                                int f_index = detail.indexOf('f');
                                if (t_index == -1) {
                                    int f_count = Integer.parseInt(detail.substring(1));
                                    int for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + f_count; j++) {
                                        Mark[(outer_index / 8 * scale) + row][j] = false;
                                        cnt += 1;
                                    }
                                    for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + scale; j++) {

                                        Mark[(outer_index / 8 * scale) + row][j] = true;
                                        cnt += 1;
                                    }
                                    detail = null;
                                } else if (f_index == -1) {
                                    int t_count = Integer.parseInt(detail.substring(1));
                                    int for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + t_count; j++) {
                                        Mark[(outer_index / 8 * scale) + row][j] = true;
                                        cnt += 1;
                                    }
                                    for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + scale; j++) {
                                        Mark[(outer_index / 8 * scale) + row][j] = false;
                                        cnt += 1;
                                    }
                                    detail = null;
                                } else if (t_index < f_index) {
                                    String t_tmp_string = detail.split("f")[0];
                                    int t_count = Integer.parseInt(t_tmp_string.substring(1));
                                    int for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + t_count; j++) {
                                        Mark[(outer_index / 8 * scale) + row][j] = true;
                                        cnt += 1;
                                    }
                                    detail = detail.substring(t_tmp_string.length());
                                } else if (t_index > f_index) {
                                    String f_tmp_string = detail.split("t")[0];
                                    int f_count = Integer.parseInt(f_tmp_string.substring(1));
                                    int for_cnt = cnt;
                                    for (int j = (outer_index % 8 * scale) + for_cnt; j < (outer_index % 8 * scale) + for_cnt + f_count; j++) {
                                        Mark[(outer_index / 8 * scale) + row][j] = false;
                                        cnt += 1;
                                    }
                                    detail = detail.substring(f_tmp_string.length());
                                }
                            }
                            //i += 1;
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }catch(Exception e){
                e.printStackTrace();
            }



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
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < scale * scale; j++) {
                        if(Mark[(i/8*scale) + (j/scale)][(i%8*scale) + (j%scale)] == true){
                            outerDistrict.getChildren().get(i).getChildren().get(j).setVisit(true);
                            outerDistrict.getChildren().get(i).getChildren().get(j).getFootPrint().setColor(ColorUtils.setAlphaComponent(COLOR_FINISH, 250));
                            outerDistrict.getChildren().get(i).getChildren().get(j).getFootPrint().setMap(naverMap);
                        }
                    }
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

        //디비로부터 정보받아오기(완료,불가,트래킹)
        retrofitExService.getCompleteData(mapInfo.getM_id()).enqueue(new Callback<CompleteData>() {
            @Override
            public void onResponse(Call<CompleteData> call, Response<CompleteData> response) {
                Log.d("오삼삼","complete onResponse");
                CompleteData data = response.body();
                Log.d("오삼삼","data : "+data.getM_find_latitude()+"   "+data.getM_find_longitude());
                if(data.getM_find_longitude()!=null && data.getM_find_latitude()!=null) {
                    FoundMarker found = new FoundMarker(colorFound);
                    found.setPosition(new LatLng(Double.parseDouble(data.getM_find_latitude()), Double.parseDouble(data.getM_find_longitude())));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            found.setMap(naverMap);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CompleteData> call, Throwable t) {
                Log.d("오삼삼","complete onFailure");
                Log.d("오삼삼",""+t);
            }
        });



        retrofitExService.getNotCompleteData(mapInfo.getM_id()).enqueue(new Callback<ArrayList<Not_Complete_Data>>() {
            @Override
            public void onResponse(Call<ArrayList<Not_Complete_Data>> call, Response<ArrayList<Not_Complete_Data>> response) {
                System.out.println("getNotCompleteData onResponse@@@@@@@@@@@@@@@@@"); //수색불가 데이터가 들어가있음
                ArrayList<Not_Complete_Data> data = response.body();
                for(int i =0;i<data.size();i++){
                    String received_lat = data.get(i).getUl_latitude();
                    String received_lng = data.get(i).getUl_longitude();
                    Log.d("오삼삼","recieved_lat : "+received_lat+" received_lng : "+received_lng);
                    NotCompleteMarker notComplete = new NotCompleteMarker(colorImpossible);
                    notComplete.setPosition(new LatLng(Double.parseDouble(received_lat), Double.parseDouble(received_lng)));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notComplete.setMap(naverMap);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Not_Complete_Data>> call, Throwable t) {
                Log.d("오삼삼","onFailure@@@@@@@@@@@@@@@@@222222222222");
                System.out.println("t: " + t);
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



