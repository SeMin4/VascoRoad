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
import com.example.woo.myapplication.data.MapDetail;
import com.example.woo.myapplication.data.NotCompleteList;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
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
import java.util.HashMap;
import java.util.List;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class DistrictActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private HashMap<Integer, Marker> markerHashMap = new HashMap<Integer, Marker>();
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
        colorFound = getResources().getColor(R.color.colorPrimary);
        colorImpossible = getResources().getColor(R.color.primary);

        if(ExistingMapActivity.mSocket != null)
            mSocket = ExistingMapActivity.mSocket;
        else
            mSocket = NewMapActivity.mSocket;
        mSocket.on("complete", complete);

           // mSocket.on("not_complete", not_complete);



        /* 이전 Activity로부터 정보 획득 */
        Intent intent = getIntent();

        row = intent.getIntExtra("row",-1);
        col = intent.getIntExtra("col", -1);
        mapId = intent.getIntExtra("mapId", -1);

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
                        Marker foundMarker = new Marker();
                        foundMarker.setPosition(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                        foundMarker.setIcon(MarkerIcons.BLACK);
                        foundMarker.setIconTintColor(Color.MAGENTA);
                        foundMarker.setCaptionText("발견 지점");
                        foundMarker.setMap(naverMapInstance);
                    }
                });
            } catch (JSONException e) {
                System.out.println("complete JsonException");
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


        /* 서버로부터 받은 수색불가 및 발견지점을 마커로 등록(홍성기) */
        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        retrofitExService.getNotCompleteList(mapId+"").enqueue(new Callback<ArrayList<NotCompleteList>>() {
            @Override
            public void onResponse(Call<ArrayList<NotCompleteList>> call, Response<ArrayList<NotCompleteList>> response) {
                Log.w("Success", "retrofit success");
                ArrayList<NotCompleteList> items = response.body();
                for(int i =0;i<items.size();i++){
                    NotCompleteList item = items.get(i);
                    Log.w("item_ul_id", item.getUl_id());
                    Log.w("item_m_id", item.getM_id());
                    Log.w("item_ul_latitude", item.getUl_latitude());
                    Log.w("item_ul_longitude", item.getUl_longitude());
                    Log.w("item_ul_desc", item.getUl_desc());
                    if(!item.getUl_file().equals("null")){
                        Log.w("item_ul_file", item.getUl_file());
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<NotCompleteList>> call, Throwable t) {
                Log.w("Fail", "retrofit Failure");
            }
        });

        /* LongClick 이벤트 등 */
        naverMap.setOnMapLongClickListener((pointF, latLng) -> {
            Marker marker = new Marker();
            markerHashMap.put(marker.hashCode(), marker);
            marker.setPosition(latLng);
            marker.setOnClickListener(overlay -> {
                Intent intent = new Intent(DistrictActivity.this, DistrictRecordActivity.class);
                intent.putExtra("markerId", marker.hashCode());
                intent.putExtra("latitude", latLng.latitude);
                intent.putExtra("longitude", latLng.longitude);
                intent.putExtra("mapId", mapId);
                startActivityForResult(intent, 0);
                return true;
            });
            marker.setMap(naverMap);
        });
        naverMapInstance = naverMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 0){
            int markerId = data.getIntExtra("markerId", -1);
            Marker point = markerHashMap.get(markerId);
            point.setIcon(MarkerIcons.BLACK);
            point.setWidth(50);
            point.setHeight(70);

            switch(resultCode){
                case RESULT_OK:
                    String opt = data.getStringExtra("result");
                    if(opt.toLowerCase().contains("finish")){
                        if(findLocation == null){
                            point.setCaptionText("발견 지점");
                            point.setCaptionColor(colorFound);
                            point.setIconTintColor(colorFound);
                            findLocation = point;
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
                    else if(opt.toLowerCase().contains("impossible")){
                        point.setCaptionText("수색 불가");
                        point.setCaptionColor(colorImpossible);
                        point.setIconTintColor(colorImpossible);
                    }
                    break;
                case RESULT_CANCELED:
                    point.setMap(null);
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
