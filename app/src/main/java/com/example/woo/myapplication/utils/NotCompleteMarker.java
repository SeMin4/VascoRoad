package com.example.woo.myapplication.utils;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

public class NotCompleteMarker {
    Marker marker;
    String imagePath;
    String desc;

    public NotCompleteMarker(int color){
        createMarker(color);
    }

    public NotCompleteMarker(String imagePath, String desc, int color){
        createMarker(color);
        this.imagePath = imagePath;
        this.desc = desc;
    }

    public void createMarker(int color){
        marker = new Marker();
        marker.setCaptionText("수색 불가");
        marker.setIcon(MarkerIcons.BLACK);
        marker.setWidth(50);
        marker.setHeight(70);
        marker.setCaptionColor(color);
        marker.setIconTintColor(color);
    }


    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LatLng getPosition(){ return this.marker.getPosition(); }

    public void setPosition(LatLng latLng){ this.marker.setPosition(latLng); }

    public NaverMap getMap(){ return this.marker.getMap(); }

    public void setMap(NaverMap naverMap){ this.marker.setMap(naverMap); }
}
