package com.example.woo.myapplication.data;


import java.io.Serializable;

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

   
}
