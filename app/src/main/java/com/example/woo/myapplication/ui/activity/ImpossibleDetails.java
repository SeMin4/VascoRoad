package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.woo.myapplication.R;
import com.squareup.picasso.Picasso;

public class ImpossibleDetails extends AppCompatActivity {
    private final String SERVER_HOST_PATH = "http://13.125.174.158:9000/not_complete_picture";
    protected ImageView imageView;
    protected TextView desc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impossible_details);

        imageView = findViewById(R.id.impossible_image);
        desc = findViewById(R.id.textView_impossible_desc);

        Intent intent = getIntent();
        desc.setText(intent.getStringExtra("desc"));

        int opt = intent.getIntExtra("option", -1);
        Log.i("image", "option: " + opt);
        switch (opt){
            case 1: // 설명만 존재
                imageView.setVisibility(View.INVISIBLE);
                break;
            case 2: // 이미지와 설명 모두 존재
                String image = intent.getStringExtra("image");
                String mapId = Integer.toString(intent.getIntExtra("mapId", -1));
                String path = SERVER_HOST_PATH + "/" + mapId + "/" + image;
                Log.i("image", "image url: " + path);
                Picasso.with(getApplicationContext())
                        .load(path)
                        .resize(800, 800)
                        .rotate(90f)
                        .into(imageView);

        }


    }

    public void mOnClick(View v){
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
