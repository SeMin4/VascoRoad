package com.example.woo.myapplication.data;


import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Mperson implements Serializable {

    String p_id;
    String p_name;
    String p_age;
    String p_time;
    String p_place_string;
    String p_place_latitude;
    String p_place_longitude;
    String p_place_description;
    String p_photo;

    public Mperson() {

    }

    public Mperson(String p_name,String p_age, String p_place_string, String p_time, String p_photo, String p_place_description) {
        this.p_name = p_name;
        this.p_age = p_age;
        this.p_time = p_time;
        this.p_place_string = p_place_string;
        this.p_place_description = p_place_description;
        this.p_photo = p_photo;
    }

    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getP_age() {
        return p_age;
    }

    public void setP_age(String p_age) {
        this.p_age = p_age;
    }

    public String getP_time() {
        return p_time;
    }

    public void setP_time(String p_time) {
        this.p_time = p_time;
    }

    public String getP_place_string() {
        return p_place_string;
    }

    public void setP_place_string(String p_place_string) {
        this.p_place_string = p_place_string;
    }

    public String getP_place_latitude() {
        return p_place_latitude;
    }

    public void setP_place_latitude(String p_place_latitude) {
        this.p_place_latitude = p_place_latitude;
    }

    public String getP_place_longitude() {
        return p_place_longitude;
    }

    public void setP_place_longitude(String p_place_longitude) {
        this.p_place_longitude = p_place_longitude;
    }

    public String getP_place_description() {
        return p_place_description;
    }

    public void setP_place_description(String p_place_description) {
        this.p_place_description = p_place_description;
    }

    public String getP_photo() {
        return p_photo;
    }

    public void setP_photo(String p_photo) {
        this.p_photo = p_photo;
    }

   public String birthToAge()
   {
       //string 4개만 자르고 그걸 정수형으로 바꾼 다음에
       String Born_year = p_age.substring(0,4);

       //현재 year
       //(1) long 으로 졸라 큰 값 받아옴
       long Nowtime = System.currentTimeMillis();
       //(2) 그 졸라 큰 값(현재시간)을 date 형식에 맞춰서 넣음
       Date Nowdate = new Date(Nowtime);
       //(3) 날짜 format 중 년도만 구하는 것: YearOnly
       SimpleDateFormat YearOnly = new SimpleDateFormat("yyyy");
       //(4)현재 년도: date 형식에 맞춰서 넣은 현재 시간을 년도만 구하는 format에 맞춤
       String Now_year = YearOnly.format(Nowdate);
       Log.d("Nowyear",Now_year);
       Log.d("Bornyear",Born_year);
       //현재 년도 - 태어난거 +1 = 나이
       int age = Integer.parseInt(Now_year) - Integer.parseInt(Born_year) +1;

       // - 정수형으로 바꾼거  -> string 으로 바꿔서 반환
       return Integer.toString(age);
   }
}
