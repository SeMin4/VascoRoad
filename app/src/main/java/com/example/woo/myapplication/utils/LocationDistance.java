package com.example.woo.myapplication.utils;

import android.util.Log;

import com.naver.maps.geometry.LatLng;

public class LocationDistance {


    public static double LatitudeInDifference(double diff){
        //지구반지름
        final int earth = 6371000;    //단위m

        return (diff*360.0) / (2* Math.PI*earth);
    }


    public static double LongitudeInDifference(double _latitude, double diff){
        //반경 m이내의 경도차(degree)
        //지구반지름
        final int earth = 6371000;    //단위m

        return (diff*360.0) / (2* Math.PI*earth* Math.cos(Math.toRadians(_latitude)));
    }

    public static double distance(LatLng a, LatLng b, String unit) {

        double theta = a.longitude - b.longitude;
        double dist = Math.sin(deg2rad(a.latitude))
                * Math.sin(deg2rad(b.latitude))
                + Math.cos(deg2rad(a.latitude))
                * Math.cos(deg2rad(b.latitude))
                * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit.equals("kilometer")) {
            dist = dist * 1.609344;
        } else if(unit.equals("meter")){
            dist = dist * 1609.344;
        }

        return (dist);
    }

    public static double angleByPoint(LatLng center, LatLng point){
        double dx = Math.abs(center.longitude - point.longitude);
        double dy = Math.abs(center.latitude - point.latitude);
        return Math.atan(dy/dx);
    }

    // This function converts decimal degrees to radians
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static LatLng rotateTransformation(LatLng center, LatLng before, double bearing){
        // 현재 bearing은 degree임.
        bearing = Math.ceil(bearing);
        double theta = deg2rad(bearing);
        double cos_theta = Math.cos(theta);
        double sin_theta = Math.sin(theta);

        double longitude = cos_theta*(before.longitude - center.longitude) + sin_theta*(before.latitude - center.latitude) + center.longitude;
        double latitude = (-sin_theta)*(before.longitude - center.longitude) + cos_theta*(before.latitude - center.latitude) + center.latitude;

//        double longitude = cos_theta*(before.longitude - center.longitude) + sin_theta*(before.latitude - center.latitude);
//        double latitude = (-sin_theta)*(before.longitude - center.longitude) + cos_theta*(before.latitude - center.latitude);
        return new LatLng(latitude, longitude);
    }
}
