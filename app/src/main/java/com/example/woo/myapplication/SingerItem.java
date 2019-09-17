package com.example.woo.myapplication;

public class SingerItem {
//여기서 할 것: 사진, 이름, 정보가 들어있는 하나의 큰 LinearLayout을 하나의 클래스로 취급할 수 있도록 해준다.

    String name; //이름
    String place; //실종장소
    String timee; //실종시간
    int resId; // 실종자 사진


    public SingerItem(String name,String place, String timee) {
        //생성자 1: context 객체를 파라미터로 받는다.
        this.name = name;
        this.place = place;
        this.timee = timee;
    }

    public SingerItem(String name, String place, String timee, int resId) {
        this.name = name;
        this.place = place;
        this.timee = timee;
        this.resId = resId;
    }

    public int getResId()
    {
        return resId;
    }

    public void setResId(int resId)
    {
        this.resId = resId;
    }

    public String getPlace()
    {
        return place;
    }
    public void setPlace(String place)
    {
        this.place= place;
    }
    public String getTimee()
    {
        return timee;
    }
    public void setTimee(String timee)
    {
        this.timee = timee;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }





}



