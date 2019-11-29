package com.example.woo.myapplication.utils;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

public class FoundMarker {
    Marker marker;

    public FoundMarker(int color){
        createMarker(color);
    }

    public void createMarker(int color){
        marker = new Marker();
        marker.setCaptionText("발견 지점");
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

    public LatLng getPosition(){ return this.marker.getPosition(); }

    public void setPosition(LatLng latLng){ this.marker.setPosition(latLng); }

    public NaverMap getMap(){ return this.marker.getMap(); }

    public void setMap(NaverMap naverMap){ this.marker.setMap(naverMap); }
}
