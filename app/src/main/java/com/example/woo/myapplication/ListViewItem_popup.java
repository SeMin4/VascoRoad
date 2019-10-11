package com.example.woo.myapplication;

public class ListViewItem_popup {
//여기서 할 것: 사진, 이름, 정보가 들어있는 하나의 큰 LinearLayout을 하나의 클래스로 취급할 수 있도록 해준다.


    String place; //수색장소
    String name;// 실종자 이름


    public ListViewItem_popup(String name, String place) {
        //생성자 1: context 객체를 파라미터로 받는다.
        this.name = name;
        this.place = place;
    }

    public ListViewItem_popup() {

    }


    public String getPlace()
    {
        return place;
    }
    public void setPlace(String place)
    {
        this.place= place;
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



