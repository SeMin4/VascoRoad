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

import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.Mperson;
import com.naver.maps.geometry.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class listVieww_popup extends Activity implements View.OnClickListener{
    ListView listView;
    ListAdapter adapter;
    private final LatLng missingPoint = new LatLng(35.886880, 128.608543); // 임시


    public class ListAdapter extends BaseAdapter
    {
        ArrayList<ListViewItem_popup> listViewItemList = new ArrayList<ListViewItem_popup>();

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

            TextView name = (TextView) convertView.findViewById(R.id.TextView_Searchingname) ;
            TextView place = (TextView) convertView.findViewById(R.id.TextView_Searchingplace) ;



            ListViewItem_popup listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            //iconImageView.setImageDrawable(listViewItem.getIcon());
            name.setText(listViewItem.getName());

            place.setText(listViewItem.getPlace());



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

        public void addItem(ListViewItem_popup item)
        {
            listViewItemList.add(item);
        }

    }
    @Override
    public void onClick(View view)
    {}




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //맞나 모르겠는데 눈치껏 일단 짜봄.....
        setContentView(R.layout.activity_popup);
        listView = (ListView)findViewById(R.id.listView_popup);
        adapter = new ListAdapter();

        Mperson selected = (Mperson)getIntent().getSerializableExtra("selecteditem");

        ImageView profile = (ImageView)findViewById(R.id.ImageView_popuptitle);
        TextView name = (TextView)findViewById(R.id.TextView_Name);
        TextView time = (TextView)findViewById(R.id.TextView_Time);
        TextView place = (TextView)findViewById(R.id.TextView_Place);
        TextView desc = (TextView)findViewById(R.id.TextView_Characteristic);

        //이름 장소 시간 사진 특징
        name.setText((CharSequence)selected.getP_name());
        time.setText((CharSequence)selected.getP_time());
        place.setText((CharSequence)selected.getP_place_string());
        desc.setText((CharSequence)selected.getP_place_description());
        if(selected.getP_photo() == null){
            profile.setImageResource(R.drawable.boy);
        }
        else{
            float rotation = 0;

            Picasso.with(getApplicationContext())
                    .load("http://13.125.95.139:9000/mperson_picture/"+selected.getP_photo())
                    .fit()
                    .rotate(90f)
                    .into(profile);



        }
        //***************************해당실종자***********


        //리스트뷰********************************
        adapter.addItem(new ListViewItem_popup("이규진","대구시 북구 대현동"));
        listView.setAdapter(adapter);
        adapter.addItem(new ListViewItem_popup("이규진","대구시 동구 신암동"));
        listView.setAdapter(adapter);
        adapter.addItem(new ListViewItem_popup("이규진","대구시 북구 침산동"));
        listView.setAdapter(adapter);
        adapter.addItem(new ListViewItem_popup("이규진","대구 어딘가"));
        listView.setAdapter(adapter);

        //리스트뷰를 누르면 해당 지역의 수색 상황을 보여준다.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), EmptyRoomActivity.class);
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
