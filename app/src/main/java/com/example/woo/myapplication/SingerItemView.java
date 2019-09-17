package com.example.woo.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SingerItemView extends LinearLayout {
//여기서 할 것: 사진, 이름, 정보가 들어있는 하나의 큰 LinearLayout을 하나의 클래스로 취급할 수 있도록 해준다.

    TextView textView1; //이름
    TextView textView2; //실종장소
    TextView textView3; //실종시간
    ImageView imageView; // 실종자 사진


    public SingerItemView(Context context) {
        //생성자 1: context 객체를 파라미터로 받는다.
        super(context);
        init(context);
    }

    public SingerItemView(Context context, AttributeSet attrs) {
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
    public void setImage(int resId)
    {
        imageView.setImageResource(resId);
    }




}



