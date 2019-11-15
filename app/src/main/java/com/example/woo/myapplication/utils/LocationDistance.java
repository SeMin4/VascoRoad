package com.example.woo.myapplication.utils;

import android.graphics.PointF;
import android.util.Log;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.Projection;

import java.util.ArrayList;

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

    public static double radByInnerProduct(double a1, double a2, double b1, double b2){
        double numerator = a1*b1 + a2*b2;
        double denominator = Math.sqrt(a1*a1 + a2*a2)*Math.sqrt(b1*b1 + b2*b2);
        return Math.acos(numerator/denominator);
    }

    // This function converts decimal degrees to radians
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static LatLng rotateTransformation(LatLng origin, LatLng point, double bearing){
        // 현재 bearing은 degree임.
        double theta = deg2rad(bearing);
        double cos_theta = Math.cos(theta);
        double sin_theta = Math.sin(theta);

        double x = cos_theta*(point.longitude - origin.longitude) + sin_theta*(point.latitude - origin.latitude) + origin.longitude;
        double y = (-sin_theta)*(point.longitude - origin.longitude) + cos_theta*(point.latitude - origin.latitude) + origin.latitude;

        return new LatLng(y, x);
    }

    public static ArrayList<LatLng> findVertexByCenter(Projection projection, LatLng[] centers){
        PointF up = projection.toScreenLocation(centers[0]);
        PointF down = projection.toScreenLocation(centers[1]);
        PointF left = projection.toScreenLocation(centers[2]);
        PointF right = projection.toScreenLocation(centers[3]);

        ArrayList<LatLng> vertex = new ArrayList<>();
        vertex.add(projection.fromScreenLocation(new PointF(left.x, up.y)));    // LU
        vertex.add(projection.fromScreenLocation(new PointF(right.x, up.y)));   // RU
        vertex.add(projection.fromScreenLocation(new PointF(right.x, down.y))); // RD
        vertex.add(projection.fromScreenLocation(new PointF(left.x, down.y)));  // LD

        return vertex;
    }
}
