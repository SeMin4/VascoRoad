package com.example.woo.myapplication.ui.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.woo.myapplication.R;
import com.example.woo.myapplication.ui.activity.MainActivity;
import com.squareup.picasso.Picasso;

import java.net.URL;

import static android.media.ExifInterface.ORIENTATION_NORMAL;
import static android.media.ExifInterface.TAG_ORIENTATION;

public class MpersonItemView extends LinearLayout {
//여기서 할 것: 사진, 이름, 정보가 들어있는 하나의 큰 LinearLayout을 하나의 클래스로 취급할 수 있도록 해준다.

    TextView textView1; //이름
    TextView textView2; //실종장소
    TextView textView3; //실종시간
    ImageView imageView; // 실종자 사진
    TextView textView4; //실종자 나이


    public MpersonItemView(Context context) {
        //생성자 1: context 객체를 파라미터로 받는다.
        super(context);
        init(context);
    }

    public MpersonItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //XML 레이아웃을 인플레이션하여 설정

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //객체를 참조한 후
        inflater.inflate(R.layout.custom_listview, this, true);
        //inflate메소드를 호출 - 레이아웃 xml..

        textView1 = (TextView) findViewById(R.id.TextView_Name);
        //이름
        textView2 = (TextView) findViewById(R.id.TextView_Place);
        //실종장소
        textView3 = (TextView) findViewById(R.id.TextView_Time);
        //실종시간
        imageView = (ImageView) findViewById(R.id.ImageView_person);
        //실종자사진
        textView4 = findViewById(R.id.TextView_Age);
        //실종자 나이

    }

    public void setName(String name)
    {
      textView1.setText(name);
    }
    public void setPlace(String place)
    {
        textView2.setText(place);
    }

    public void setTimee(String timee)
    {
        textView3.setText(timee);
    }
    public void setImage(String imageBaseDirectory, String imageName)
    {
        if(imageName == null){
            imageView.setImageResource(R.drawable.boy);
        }
        else{
            float rotation = 0;
          
            Picasso.with(getContext())
                    .load(imageBaseDirectory+imageName)
                    .rotate(0f)
                    .into(imageView);
        }
    }
    public void setAge(String age)
    {
        textView4.setText(age);
    }

}



