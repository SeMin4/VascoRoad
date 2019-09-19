package com.example.woo.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ListVieww extends Activity {

    class SingerAdapter extends BaseAdapter {
        ArrayList<Mperson> items = new ArrayList<Mperson>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Mperson item)
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
            Mperson item = items.get(position);
            view.setName(item.getP_name());
            view.setPlace(item.getP_place_string());
            view.setTimee(item.getP_time());
            //view.setImage(item.getP_photo());
            return view;
        }
    }

    ListView listView;
    SingerAdapter adapter;
    Retrofit retrofit;
    RetrofitExService retroService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new SingerAdapter();
        listView.setAdapter(adapter);



        retrofit = new Retrofit.Builder().baseUrl(RetrofitExService.URL).addConverterFactory(GsonConverterFactory.create()).build();
        retroService = retrofit.create(RetrofitExService.class);

        retroService.getData().enqueue(new Callback<ArrayList<Mperson>>() {
            @Override
            public void onResponse(Call<ArrayList<Mperson>> call, Response<ArrayList<Mperson>> response) {
                System.out.println("onResponse 호출!!!!!!@@@@");
                ArrayList<Mperson> persons = response.body();

                for(int i =0;i<persons.size();i++){
                    adapter.addItem(persons.get(i));
                    System.out.println(persons.get(i).getP_id());
                    System.out.println(persons.get(i).getP_name());
                    System.out.println(persons.get(i).getP_age());
                }
                listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onFailure(Call<ArrayList<Mperson>> call, Throwable t){
                System.out.println("onFailure 호출!!!!!@@@@@@");
                System.out.println(t);
            }

        });
    }

    public interface RetrofitExService{ //interface 선언
        public static final String URL = "http://13.125.95.139:9000/";
        @GET("mperson")
        Call<ArrayList<Mperson>> getData();
    }

}






