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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MissingInfoActivity extends Activity {
    Mperson selected;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.missinginfo_popup);


        selected = (Mperson)getIntent().getSerializableExtra("selecteditem");



        ImageView profile = (ImageView)findViewById(R.id.ImageView_popuptitle);
        TextView name = (TextView)findViewById(R.id.TextView_Name);
        TextView time = (TextView)findViewById(R.id.TextView_Time);
        TextView place = (TextView)findViewById(R.id.TextView_Place);
        TextView desc = (TextView)findViewById(R.id.TextView_Characteristic);
        TextView age = (TextView)findViewById(R.id.TextView_Age);

        //이름 장소 시간 사진 특징
        name.setText((CharSequence)selected.getP_name());
        time.setText((CharSequence)selected.getP_time());
        place.setText((CharSequence)selected.getP_place_string());
        desc.setText((CharSequence)selected.getP_place_description());
        age.setText((CharSequence)selected.birthToAge());
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
    }
}

