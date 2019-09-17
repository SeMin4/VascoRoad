package com.example.woo.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ListVieww extends Activity {

    class SingerAdapter extends BaseAdapter {
        ArrayList<SingerItem> items = new ArrayList<SingerItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(SingerItem item)
        {
            items.add(item);
        }
        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            SingerItemView view = new SingerItemView(getApplicationContext());
            SingerItem item = items.get(position);
            view.setName(item.getName());
            view.setPlace(item.getPlace());
            view.setTimee(item.getTimee());
            view.setImage(item.getResId());

            return view;

        }
    }




    ListView listView;
    SingerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        listView = (ListView) findViewById(R.id.listView);

        adapter = new SingerAdapter();
        listView.setAdapter(adapter);

        //버튼을 누르면 등록이 된다.
        Button EnrollButton = (Button) findViewById(R.id.Button_Add);
        EnrollButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addItem(new SingerItem("김철수","대구광역시 달서구 진천동","2019년 4월 19일 13시경",R.drawable.boy));
            }
        });




    }

}






