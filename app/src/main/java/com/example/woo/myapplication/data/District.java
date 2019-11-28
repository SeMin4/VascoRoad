package com.example.woo.myapplication.data;

import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;

import com.example.woo.myapplication.utils.LocationDistance;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.PolygonOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class District implements Serializable {
    private int rowIdx;
    private int colIdx;
    private ArrayList<District> children;
    private PolygonOverlay grid;
    private CircleOverlay footPrint;
    private LatLng center;
    private LatLng northWest;
    private LatLng southWest;
    private LatLng southEast;
    private LatLng northEast;
    private boolean visit;

    public District(){
        rowIdx = 0;
        colIdx = 0;
        children = new ArrayList<>();
        grid = new PolygonOverlay();
        footPrint = new CircleOverlay();
        visit = false;
    }



    public District(LatLng nw, LatLng ne, LatLng se, LatLng sw){
        rowIdx = 0;
        colIdx = 0;
        this.northWest = nw;
        this.southWest = sw;
        this.southEast = se;
        this.northEast = ne;
        visit = false;

        grid = new PolygonOverlay();
        grid.setCoords(Arrays.asList(
                new LatLng(this.northWest.latitude, this.northWest.longitude),
                new LatLng(this.northEast.latitude, this.northEast.longitude),
                new LatLng(this.southEast.latitude, this.southEast.longitude),
                new LatLng(this.southWest.latitude, this.southWest.longitude)
        ));

        footPrint = new CircleOverlay();
        footPrint.setRadius(LocationDistance.distance(nw, sw, "meter") * Math.sqrt(2) / 2);
        children = new ArrayList<>();
    }

    public void addToMap(@Nullable NaverMap naverMap, int color, int width){
        grid.setColor(ColorUtils.setAlphaComponent(color, 0));
        grid.setOutlineWidth(width);
        grid.setOutlineColor(color);
        grid.setGlobalZIndex(10);
        grid.setMap(naverMap);
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public void setRowIdx(int rowIdx) {
        this.rowIdx = rowIdx;
    }

    public int getColIdx() {
        return colIdx;
    }

    public void setColIdx(int colIdx) {
        this.colIdx = colIdx;
    }

    public ArrayList<District> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<District> child) {
        this.children = child;
    }

    public PolygonOverlay getGrid() { return grid; }

    public void setGrid(PolygonOverlay grid) { this.grid = grid; }

    public CircleOverlay getFootPrint() {
        return footPrint;
    }

    public void setFootPrint(CircleOverlay footPrint) {
        this.footPrint = footPrint;
    }

    public LatLng getCenter(){ return center; }

    public void setCenter(LatLng center){
        this.center = center;
        footPrint.setCenter(center);
    }

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

    public boolean isVisit() {
        return visit;
    }

    public void setVisit(boolean visit) {
        this.visit = visit;
    }
}
