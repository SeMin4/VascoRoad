package com.example.woo.myapplication.data;

import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.ColorUtils;

import com.example.woo.myapplication.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.PolygonOverlay;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class District {
    private ArrayList<District> children;
    private PolygonOverlay grid;
    private LatLng center;
    private LatLng northWest;
    private LatLng southWest;
    private LatLng southEast;
    private LatLng northEast;

    public District(){
        children = new ArrayList<>();
    }

    public District(LatLng sw, LatLng ne){
        // 회전각도 0일때만 가능
        children = new ArrayList<>();
        this.southWest = sw;
        this.northEast = ne;
        this.northWest = new LatLng(northEast.latitude, southWest.longitude);
        this.southEast = new LatLng(southWest.latitude, northEast.longitude);
        grid = new PolygonOverlay();
        grid.setCoords(Arrays.asList(
                this.northEast,
                this.northWest,
                this.southEast,
                this.southWest
        ));
    }

    public District(LatLng nw, LatLng sw, LatLng se, LatLng ne){
        this.northWest = nw;
        this.southWest = sw;
        this.southEast = se;
        this.northEast = ne;

        grid = new PolygonOverlay();
        grid.setCoords(Arrays.asList(
                this.northEast,
                this.northWest,
                this.southEast,
                this.southWest
        ));
        children = new ArrayList<>();
    }

    public void addToMap(@Nullable NaverMap naverMap, int color, int width){
        grid.setColor(ColorUtils.setAlphaComponent(color, 0));
        grid.setOutlineWidth(width);
        grid.setOutlineColor(color);
        grid.setGlobalZIndex(10);
        grid.setMap(naverMap);
    }

    public ArrayList<District> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<District> child) {
        this.children = child;
    }

    public PolygonOverlay getGrid() {
        return grid;
    }

    public void setGrid(PolygonOverlay grid) {
        this.grid = grid;
    }

    public LatLng getCenter(){ return center; }

    public void setCenter(LatLng center){ this.center = center; }

    public LatLng getNorthWest() {
        return northWest;
    }

    public void setNorthWest(LatLng northWest) {
        this.northWest = northWest;
    }

    public LatLng getSouthWest() {
        return southWest;
    }

    public void setSouthWest(LatLng southWest) {
        this.southWest = southWest;
    }

    public LatLng getSouthEast() {
        return southEast;
    }

    public void setSouthEast(LatLng southEast) {
        this.southEast = southEast;
    }

    public LatLng getNorthEast() {
        return northEast;
    }

    public void setNorthEast(LatLng northEast) {
        this.northEast = northEast;
    }

}
