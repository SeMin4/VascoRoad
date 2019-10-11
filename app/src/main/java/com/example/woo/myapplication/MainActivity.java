package com.example.woo.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {
    LoginActivity _LoginActivity = (LoginActivity)LoginActivity._LoginActivity;
    protected Button logout_btn;
    protected Button myPage_btn;
    ListView listView;
    MpersonAdapter adapter;
    Retrofit retrofit;
    MyGlobals.RetrofitExService retroService;
    String districtName;
    EditText search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _LoginActivity.finish();
        logout_btn = (Button) findViewById(R.id.logout_btn);
        myPage_btn = (Button) findViewById(R.id.my_page_btn);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        listView = (ListView) findViewById(R.id.listView);
        search = (EditText)findViewById(R.id.search);
        listView.setAdapter(adapter);
        adapter = new MpersonAdapter();

        if( (MyGlobals.getInstance().getRetrofit() == null) || (MyGlobals.getInstance().getRetrofitExService() ==null) ) {
            retrofit = new Retrofit.Builder().baseUrl(MyGlobals.RetrofitExService.URL).addConverterFactory(GsonConverterFactory.create()).build();
            retroService = retrofit.create(MyGlobals.RetrofitExService.class);
            MyGlobals.getInstance().setRetrofit(retrofit);
            MyGlobals.getInstance().setRetrofitExService(retroService);
        }else{
            retrofit = MyGlobals.getInstance().getRetrofit();
            retroService = MyGlobals.getInstance().getRetrofitExService();
        }

        retroService.getData().enqueue(new Callback<ArrayList<Mperson>>() {
            @Override
            public void onResponse(Call<ArrayList<Mperson>> call, Response<ArrayList<Mperson>> response) {
                System.out.println("onResponse 호출됨@@@@@@@@@");
                ArrayList<Mperson> persons = response.body();
                for(int i=0;i<persons.size();i++){
                    adapter.addItem(persons.get(i));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Mperson>> call, Throwable t) {
                System.out.println("onFailure 호출됨@@@@@@@@@");
                Toast.makeText(getApplicationContext(),"리스트 오류",Toast.LENGTH_SHORT).show();
            }
        });
       // adapter.addItem(new Mperson("Minjeong","대구광역시 달서구 진천동","2019년 4월 19일 13시경",R.drawable.boy,"하비스트 먹다가 사라짐"));
        //adapter.addItem(new Mperson("Joonhee","대구광역시 달서구 진천동","2019년 4월 19일 13시경",R.drawable.boy,"연어초밥 먹다가 사라짐"));
        //adapter.addItem(new Mperson("Semin","대구광역시 달서구 진천동","2019년 4월 19일 13시경",R.drawable.boy,"인도네시아 스쿠터 타고 사라짐"));
        //adapter.addItem(new Mperson("Seongki","대구광역시 달서구 진천동","2019년 4월 19일 13시경",R.drawable.boy,"엘렌에게 등짝맞아서 사라짐"));
        listView.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                districtName = parent.getItemAtPosition(position).toString();
                //입력값을 변수에 저장한다.

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        myPage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //지도정보 받아오기
                if(MyGlobals.getInstance().getMaplist() == null){
                    retroService.getMapData(MyGlobals.getInstance().getUser().getU_id()).enqueue(new Callback<ArrayList<MyRoomItem>>() {
                        @Override
                        public void onResponse(Call<ArrayList<MyRoomItem>> call, Response<ArrayList<MyRoomItem>> response) {
                            System.out.println("onResponse 호출됨@@@@@@@@@@@@@@@@");
                            ArrayList<MyRoomItem> maplist = response.body();
                            System.out.println("size :" +maplist.size());
                            //MyRoomItem maplist = response.body()
                            for(int i =0;i<maplist.size();i++)
                                System.out.println(maplist.get(i).m_id);
                            MyGlobals.getInstance().setMaplist(maplist);
                            Intent intent1 = new Intent(getApplicationContext(),MyPageActivity.class);
                            startActivity(intent1);
                        }

                        @Override
                        public void onFailure(Call<ArrayList<MyRoomItem>> call, Throwable t) {
                            System.out.println("onFailure 호출됨@@@@@@@@@@@@@@@@@");
                            Toast.makeText(getApplicationContext(),"맵호출 실패",Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(getApplicationContext(),MyPageActivity.class);
                            startActivity(intent1);
                        }
                    });
                }
                else{
                    Intent intent1 = new Intent(getApplicationContext(),MyPageActivity.class);
                    startActivity(intent1);
                }

            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                MyGlobals.getInstance().setUser(null);
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor auto_editor = auto.edit();
                auto_editor.clear();
                auto_editor.commit();
                startActivity(intent);
                finish();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String filterText = editable.toString();
                if(filterText.length() > 0){
                    listView.setFilterText(filterText);
                }else{
                    listView.clearTextFilter();
                }
                ((MpersonAdapter)listView.getAdapter()).getFilter().filter(filterText) ;
            }
        });

    }


    class MpersonAdapter extends BaseAdapter implements Filterable {  //adapter 정의

       ArrayList<Mperson> items = new ArrayList<Mperson>();
       // ArrayList<Mperson> items = recv_list;
        ArrayList<Mperson> filteredItemList = items;
        Filter listFilter;

        @Override
        public int getCount() {
            return filteredItemList.size();
        }

        public void addItem(Mperson item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return filteredItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            MpersonItemView view = new MpersonItemView(getApplicationContext());
            Mperson item = filteredItemList.get(position);
            view.setName(item.getP_name());
            view.setPlace(item.getP_place_string());
            view.setTimee(item.getP_time());
            //view.setImage(item.getP_photo());
            return view;
        }

        @Override
        public Filter getFilter() {
            if(listFilter == null){
                listFilter = new ListFilter();
            }
            return listFilter;
        }

        private class ListFilter extends Filter{
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
               FilterResults results = new FilterResults();

               if(charSequence == null || charSequence.length() == 0){
                   results.values = items;
                   results.count = items.size();
               }else{
                   ArrayList<Mperson> itemList = new ArrayList<Mperson>();

                   for(Mperson item : items){
                       if(item.getP_name().toUpperCase().contains(charSequence.toString().toUpperCase()))
                           itemList.add(item);
                   }
                   results.values = itemList;
                   results.count = itemList.size();
               }
               return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItemList = (ArrayList<Mperson>)filterResults.values;

                if(filterResults.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        }

    }
}



