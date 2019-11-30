package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.CompleteData;
import com.example.woo.myapplication.data.DetailData;
import com.example.woo.myapplication.data.Not_Complete_Data;
import com.example.woo.myapplication.utils.FoundMarker;
import com.example.woo.myapplication.utils.NotCompleteMarker;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class DistrictActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int RECORD_REGISTER = 0;
    private HashMap<Integer, Marker> markerHashMap = new HashMap<Integer, Marker>();
    private HashMap<Integer, InfoWindow> windowHashMap = new HashMap<Integer, InfoWindow>();
    private Marker findLocation;
    private PolygonOverlay district = new PolygonOverlay();
    private FusedLocationSource locationSource;
    private NaverMap naverMapInstance;
    private int mapId;
    private int row;
    private int col;
    private int colorOutline;
    private int colorFound;
    private int colorImpossible;
    String lat2,lng2,desc,photo_name;
    private int index;

    private Socket mSocket=null;
    String lat,lng;

    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_details);

        /* 색상 resource 획득 */
        colorOutline = getResources().getColor(R.color.white);
        colorFound = getResources().getColor(R.color.finish);
        colorImpossible = getResources().getColor(R.color.impossible);


        /* 이전 Activity로부터 정보 획득 */
        Intent intent = getIntent();

        row = intent.getIntExtra("row",-1);
        col = intent.getIntExtra("col", -1);
        mapId = intent.getIntExtra("mapId", -1);
        index = intent.getIntExtra("index",-1);
        System.out.println("district : row : "+row + " col : "+col +" mapid : "+mapId + " index : "+index);
        TextView district_title = findViewById(R.id.textView_district_details);
        String str = (row+1) + "행" + (col+1) + "열 ";
        district_title.setText(str);

        List<LatLng> coords = (List<LatLng>) intent.getSerializableExtra("coords");
        district.setCoords(coords);
        district.setColor(ColorUtils.setAlphaComponent(colorOutline, 0));
        district.setOutlineWidth(getResources().getDimensionPixelSize(R.dimen.overlay_line_bold_width));
        district.setOutlineColor(colorOutline);

        /* 지도 fragment 등록 */
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment_district_details, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // LocationSource 획득
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        /* 서버로부터 수색불가 및 발견지점 위경도 획득(홍성기) */

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ExistingMapActivity.mSocket != null)
            mSocket = ExistingMapActivity.mSocket; //기존 지도에서 들어옴
        else
            mSocket = NewMapActivity.mSocket; // 새로만든 지도에서 들어옴
        mSocket.on("complete", complete);
        mSocket.on("not_complete", not_complete);

        // mSocket.on("not_complete", not_complete);
    }

    private Emitter.Listener complete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {
                JSONObject receivedData = (JSONObject) args[0];
                lat = receivedData.getString("lat");
                lng = receivedData.getString("lng");
                System.out.println("lat : @@@@@@@@@@@@@@" + receivedData.getString("lat") + "@@@@@@@@@@@@@@@");
                System.out.println("index : @@@@@@@@@@@@@@@@@@@@@@" + receivedData.getString("lng") + "@@@@@@@@@@@@@");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FoundMarker found = new FoundMarker(colorFound);
                        found.setPosition(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
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
                JSONObject receivedData = (JSONObject)args[0];

                lat2 = receivedData.getString("lat");
                lng2 = receivedData.getString("lng");
                desc = receivedData.getString("desc");
                photo_name = receivedData.getString("photo_name");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NotCompleteMarker notComplete = new NotCompleteMarker(colorImpossible);
                        notComplete.setPosition(new LatLng(Double.parseDouble(lat2), Double.parseDouble(lng2)));
                        notComplete.getMarker().setOnClickListener(new Overlay.OnClickListener() {
                            @Override
                            public boolean onClick(@NonNull Overlay overlay) {
                                int option = 1;
                                Intent intent = new Intent(DistrictActivity.this, ImpossibleDetails.class);
                                intent.putExtra("desc", desc);
                                if (photo_name != null) {
                                    intent.putExtra("image", photo_name);
                                    intent.putExtra("mapId", mapId);
                                    Log.w("item_ul_file", photo_name);
                                    option = 2;
                                }
                                intent.putExtra("option", option);
                                startActivity(intent);
                                return true;
                            }
                        });
                        notComplete.setMap(naverMapInstance);
                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
    };


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        /* 기본 맵 세팅 */
        naverMap.getUiSettings().setZoomControlEnabled(false);      // 지도 줌버튼 비활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);    // 현위치 버튼 활성화
        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setScrollGesturesEnabled(true);   // 스크롤 제스쳐 비활성화
        naverMap.getUiSettings().setTiltGesturesEnabled(false);     // 기울임 제스쳐 비활성화
        naverMap.getUiSettings().setStopGesturesEnabled(false);     // 애니메이션 비활성화
        naverMap.getUiSettings().setRotateGesturesEnabled(true);   // 회전 제스쳐 비활성화
        naverMap.getUiSettings().setZoomGesturesEnabled(true);
        naverMap.setMapType(NaverMap.MapType.valueOf("Satellite"));

        /* 구역 등록 및 확대 */
        district.setMap(naverMap);
        // 중심점 획득 시, 중심도 설정하면 좋을 듯.
        naverMap.moveCamera(CameraUpdate.fitBounds(district.getBounds(), 100));
        naverMap.setMinZoom(naverMap.getCameraPosition().zoom);     // 최소 줌레벨 제한.


        //디비로부터 정보받아오기(완료,불가,트래킹)
        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        retrofitExService.getCompleteData(""+mapId).enqueue(new Callback<CompleteData>() {
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

        retrofitExService.getTrackingList(""+mapId,""+index).enqueue(new Callback<ArrayList<DetailData>>() {
            @Override
            public void onResponse(Call<ArrayList<DetailData>> call, Response<ArrayList<DetailData>> response) {
                Log.d("오삼삼","onResponse");
                ArrayList<DetailData> items = response.body();
                for(int i =0;i<items.size();i++){
                    DetailData data = items.get(i);
                    Log.d("오삼삼",data.getMd_run_length());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DetailData>> call, Throwable t) {
                Log.d("오삼삼","onFailure");
                Log.d("오삼삼",""+t);
            }
        });

        retrofitExService.getNotCompleteList(""+mapId,""+index).enqueue(new Callback<ArrayList<Not_Complete_Data>>() { //수색불가 띄우기
            @Override
            public void onResponse(Call<ArrayList<Not_Complete_Data>> call, Response<ArrayList<Not_Complete_Data>> response) {
                Log.d("오삼삼", "retrofit success");
                Log.d("오삼삼", "notComplete onResponse");
                ArrayList<Not_Complete_Data> items = response.body();
                Log.d("오삼삼", "notComplete size: " + items.size());
                for(int i =0;i<items.size();i++){
                    Not_Complete_Data item = items.get(i);

                    NotCompleteMarker notComplete = new NotCompleteMarker(colorImpossible);
                    notComplete.setPosition(new LatLng(Double.parseDouble(items.get(i).getUl_latitude()), Double.parseDouble(items.get(i).getUl_longitude())));
                    if(item.getUl_file()!=null){
                        notComplete.setImagePath(item.getUl_file());
                    }
                    notComplete.setDesc(item.getUl_desc());
                    notComplete.getMarker().setOnClickListener(overlay -> {
                        int option = 1;
                        Intent intent = new Intent(DistrictActivity.this, ImpossibleDetails.class);
                        intent.putExtra("desc", item.getUl_desc());
                        if (item.getUl_file() != null) {
                            intent.putExtra("image", item.getUl_file());
                            intent.putExtra("mapId", mapId);
                            Log.w("item_ul_file", item.getUl_file());
                            option = 2;
                        }
                        intent.putExtra("option", option);
                        startActivity(intent);
                        return true;
                    });
                        runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notComplete.setMap(naverMap); //여기있는 정보로 마크 클릭 시  사진보여주고 내용 보여주기 (민정)
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Not_Complete_Data>> call, Throwable t) {
                Log.d("오삼삼", "retrofit Failure");
            }
        });



        /* LongClick 이벤트 등 */
        naverMap.setOnMapLongClickListener((pointF, latLng) -> {
            InfoWindow infoWindow = new InfoWindow();
            infoWindow.setAlpha(0.9f);
            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                    return "이곳을 등록하려면 말풍선을 클릭하세요";
                }
            });

            infoWindow.setPosition(latLng);
            infoWindow.open(naverMap);
            windowHashMap.put(infoWindow.hashCode(), infoWindow);
            infoWindow.setOnClickListener(overlay -> {
                infoWindow.close();
                Intent intent = new Intent(DistrictActivity.this, DistrictRecordActivity.class);
                intent.putExtra("markerId", infoWindow.hashCode());
                intent.putExtra("latitude", latLng.latitude);
                intent.putExtra("longitude", latLng.longitude);
                intent.putExtra("mapId", mapId);
                intent.putExtra("index",index);
                startActivityForResult(intent, RECORD_REGISTER);
                return true;
            });

        });

        naverMapInstance = naverMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RECORD_REGISTER){
            int markerId = data.getIntExtra("markerId", -1);
            Log.d("마커", "after code: " + markerId);

            switch(resultCode){
                case RESULT_OK:
                    String opt = data.getStringExtra("result");
                    if(opt.toLowerCase().contains("finish")){
                        if(findLocation == null){
                            findLocation = new Marker();
                            findLocation.setPosition(windowHashMap.get(markerId).getPosition());
                            JSONObject s_data = new JSONObject();
                            try{
                                s_data.put("mid",mapId);
                                s_data.put("lat",findLocation.getPosition().latitude);
                                s_data.put("lng",findLocation.getPosition().longitude);
                                mSocket.emit("complete",s_data);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(DistrictActivity.this, "이미 등록된 발견지점이 있습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }

    }

    public void mOnClick(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);
        finish();

    }
}
