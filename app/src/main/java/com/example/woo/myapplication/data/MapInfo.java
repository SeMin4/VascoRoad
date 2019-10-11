package com.example.woo.myapplication.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.naver.maps.geometry.LatLng;

public class MapInfo implements Parcelable {
    private final int STATUS_BEFORE_SEARCHING = 100;
    private final int STATUS_SEARCHING = 101;
    private final int STATUS_FINISH_SEARCHING = 102;
    private int m_id;
    private int p_id;
    private String password;    // 보안작업 필요
    private int owner_id;
    private int status;     // 현재 수색작업 현황(시작전/수색중/수색완료...등등)
    private int horizontal;
    private int vertical;
    private double bearing;   // 지도 기울기
    private String missing_address;    // 실종위치 주소
    private double missing_lat;
    private double missing_lng;
    private double center_lat;    // 지도 중심점
    private double center_lng;
    private double up_height;
    private double down_height;
    private double left_width;
    private double right_width;
    private double unit_scale;


    public MapInfo(){
        this.m_id = -1;
        this.p_id = -1;
        this.password = "0000";
        this.owner_id = -1;
        this.status = STATUS_BEFORE_SEARCHING;
        this.horizontal = -1;
        this.vertical = -1;
        this.bearing = 0;
        this.missing_address = "대구광역시 북구 대학로 80 공대 9호관 521호 세미나실";
    }

    public MapInfo(Parcel parcel){
        this.m_id = parcel.readInt();
        this.p_id = parcel.readInt();
        this.password = parcel.readString();
        this.owner_id = parcel.readInt();
        this.status = parcel.readInt();
        this.horizontal = parcel.readInt();
        this.vertical = parcel.readInt();
        this.bearing = parcel.readDouble();
        this.missing_address = parcel.readString();
        this.missing_lat = parcel.readDouble();
        this.missing_lng = parcel.readDouble();
        this.center_lat = parcel.readDouble();
        this.center_lng = parcel.readDouble();
        this.up_height = parcel.readDouble();
        this.down_height = parcel.readDouble();
        this.left_width = parcel.readDouble();
        this.right_width = parcel.readDouble();
        this.unit_scale = parcel.readDouble();
    }
    public MapInfo(int _pid,
                   int owner_id,
                   int horizontal,
                   int vertical,
                   double bearing,
                   String missing_address,
                   LatLng missing_coord,
                   LatLng center_coord,
                   double up_height,
                   double down_height,
                   double left_width,
                   double right_width,
                   double unit_scale
                    ){

        this.p_id = _pid;
        this.owner_id = owner_id;
        this.status = STATUS_BEFORE_SEARCHING;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.bearing = bearing;
        this.missing_address = missing_address;
        this.missing_lat = missing_coord.latitude;
        this.missing_lng = missing_coord.longitude;
        this.center_lat = center_coord.latitude;
        this.center_lng = center_coord.longitude;
        this.up_height = up_height;
        this.down_height = down_height;
        this.left_width = left_width;
        this.right_width = right_width;
        this.unit_scale = unit_scale;
    }

    public int getM_id() {
        return m_id;
    }

    public void setM_id(int m_id) {
        this.m_id = m_id;
    }

    public int getP_id() {
        return p_id;
    }

    public void setP_id(int p_id) {
        this.p_id = p_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    public double getBearing(){ return bearing; }

    public void setBearing(double bearing){ this.bearing = bearing; }

    public String getMissing_address() {
        return missing_address;
    }

    public void setMissing_address(String missing_address) {
        this.missing_address = missing_address;
    }

    public double getMissing_lat() {
        return missing_lat;
    }

    public void setMissing_lat(double missing_lat) {
        this.missing_lat = missing_lat;
    }

    public double getMissing_lng() {
        return missing_lng;
    }

    public void setMissing_lng(double missing_lng) {
        this.missing_lng = missing_lng;
    }

    public double getCenter_lat() {
        return center_lat;
    }

    public void setCenter_lat(double center_lat) {
        this.center_lat = center_lat;
    }

    public double getCenter_lng() {
        return center_lng;
    }

    public void setCenter_lng(double center_lng) {
        this.center_lng = center_lng;
    }

    public double getUp_height() {
        return up_height;
    }

    public void setUp_height(double up_height) {
        this.up_height = up_height;
    }

    public double getDown_height() {
        return down_height;
    }

    public void setDown_height(double down_height) {
        this.down_height = down_height;
    }

    public double getLeft_width() {
        return left_width;
    }

    public void setLeft_width(double left_width) {
        this.left_width = left_width;
    }

    public double getRight_width() {
        return right_width;
    }

    public void setRight_width(double right_width) {
        this.right_width = right_width;
    }

    public double getUnit_scale() {
        return unit_scale;
    }

    public void setUnit_scale(double unit_scale) {
        this.unit_scale = unit_scale;
    }

    public static final Parcelable.Creator<MapInfo> CREATOR = new Parcelable.Creator<MapInfo>(){

        @Override
        public MapInfo createFromParcel(Parcel source) {
            return new MapInfo(source);
        }

        @Override
        public MapInfo[] newArray(int size) {
            return new MapInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.m_id);
        dest.writeInt(this.p_id);
        dest.writeString(this.password);
        dest.writeInt(this.owner_id);
        dest.writeInt(this.status);
        dest.writeInt(this.horizontal);
        dest.writeInt(this.vertical);
        dest.writeDouble(this.bearing);
        dest.writeString(this.missing_address);
        dest.writeDouble(this.missing_lat);
        dest.writeDouble(missing_lng);
        dest.writeDouble(this.center_lat);
        dest.writeDouble(this.center_lng);
        dest.writeDouble(this.up_height);
        dest.writeDouble(this.down_height);
        dest.writeDouble(this.left_width);
        dest.writeDouble(this.right_width);
        dest.writeDouble(this.unit_scale);
    }
}
