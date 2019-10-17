package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.MapInfo;
import com.example.woo.myapplication.data.Mperson;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class listVieww_popup extends Activity implements View.OnClickListener{
    ListView listView;
    ListAdapter adapter;
    private LatLng missingPoint;
    private ArrayList<MapInfo> maplist;
    Retrofit retrofit = null;
    MyGlobals.RetrofitExService retrofitExService =null;

    public class ListAdapter extends BaseAdapter
        {
            ArrayList<MapInfo> listViewItemList = new ArrayList<MapInfo>();

            public ListAdapter()
            {}
            @Override
            public int getCount() {
                return listViewItemList.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final int pos = position;
                final Context context = parent.getContext();

                if(convertView==null)
                {
                    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.custom_listview_popup,parent,false);
                }

                // 화면에 표시될 View(Layout이 IT터 위젯에 대한 참조 획득

                TextView place = (TextView) convertView.findViewById(R.id.TextView_Searchingplace) ;
                MapInfo listViewItem = listViewItemList.get(position);
                // 아이템 내 각 위젯에 데이터 반영;
                // place.setText("실종지점 위도 : "+listViewItem.getM_center_point_latitude() + " 실종지점 경도 : "+listViewItem.getM_center_point_longitude());
                place.setText(listViewItem.getM_place_string());
                return convertView;
        }
        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position) ;
        }

        // 아이템 데이터 추가

        public void addItem(MapInfo item)
        {
            listViewItemList.add(item);
        }

    }


    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        listView = (ListView)findViewById(R.id.listView_popup);
        adapter = new ListAdapter();
        listView.setAdapter(adapter);

        Mperson selected = (Mperson)getIntent().getSerializableExtra("selecteditem");

        ImageView profile = (ImageView)findViewById(R.id.ImageView_person);
        TextView name = (TextView)findViewById(R.id.TextView_Name);
        TextView time = (TextView)findViewById(R.id.TextView_Time);
        TextView place = (TextView)findViewById(R.id.TextView_Place);
        TextView desc = (TextView)findViewById(R.id.TextView_Characteristic);

        //이름 장소 시간 사진 특징
        name.setText((CharSequence)selected.getP_name());
        time.setText((CharSequence)selected.getP_time());
        place.setText((CharSequence)selected.getP_place_string());
        desc.setText((CharSequence)selected.getP_place_description());
        missingPoint = new LatLng(
                Double.parseDouble(selected.getP_place_latitude()),
                Double.parseDouble(selected.getP_place_longitude())
        );
        //***************************해당실종자***********
        retrofitExService.getPersonMapData( selected.getP_id()).enqueue(new Callback<ArrayList<MapInfo>>() {
            @Override
            public void onResponse(Call<ArrayList<MapInfo>> call, Response<ArrayList<MapInfo>> response) {
                System.out.println("onResponse@@@@@@@@@@@@");
                maplist = response.body();
                System.out.println("maplist _size : "+maplist.size());
                System.out.println("place : "+maplist.get(0).getM_place_string());
                for(int i =0;i<maplist.size();i++){
                    adapter.addItem(maplist.get(i));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<MapInfo>> call, Throwable t) {
                System.out.println("onFailure@@@@@@@@@@@@@@");
                Toast.makeText(getApplicationContext(),"지도 띄우기 실패",Toast.LENGTH_SHORT).show();
            }
        });


        //리스트뷰를 누르면 해당 지역의 수색 상황을 보여준다.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), ExistingMapActivity.class);
                intent.putExtra("mapInfo", maplist.get(position));
                startActivity(intent);
            }
        });

    }

    public void mOnClick(View v){
        Intent intent = new Intent(this, RegisterNewMapActivity.class);
        intent.putExtra("missing_lat", missingPoint.latitude);
        intent.putExtra("missing_long", missingPoint.longitude);
        startActivityForResult(intent, 1);
    }
}
