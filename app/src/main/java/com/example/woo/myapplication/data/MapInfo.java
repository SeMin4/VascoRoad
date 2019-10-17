package com.example.woo.myapplication.data;

import java.io.Serializable;

public class MapInfo implements Serializable {

    private String m_id;
    private String p_id;
    private String m_owner;
    private String m_status;     // 현재 수색작업 현황(시작전/수색중/수색완료...등등)
    private String m_horizontal;
    private String m_vertical;
    private String m_rotation;   // 지도 기울기
    private String m_place_string;    // 실종위치 주소
    private String m_place_latitude;
    private String m_place_longitude;
    private String m_center_point_latitude;    // 지도 중심점
    private String m_center_point_longitude;
    private String m_up;
    private String m_down;
    private String m_left;
    private String m_right;
    private String m_unit_scale;
    private String m_northWest_latitude;
    private String m_northWest_longitude;
    private String m_northEast_latitude;
    private String m_northEast_longitude;
    private String m_southWest_latitude;
    private String m_southWest_longitude;
    private String m_southEast_latitude;
    private String m_southEast_longitude;

    public String getM_northWest_latitude() {
        return m_northWest_latitude;
    }

    public void setM_northWest_latitude(String m_northWest_latitude) {
        this.m_northWest_latitude = m_northWest_latitude;
    }

    public String getM_northWest_longitude() {
        return m_northWest_longitude;
    }

    public void setM_northWest_longitude(String m_northWest_longitude) {
        this.m_northWest_longitude = m_northWest_longitude;
    }

    public String getM_northEast_latitude() {
        return m_northEast_latitude;
    }

    public void setM_northEast_latitude(String m_northEast_latitude) {
        this.m_northEast_latitude = m_northEast_latitude;
    }

    public String getM_northEast_longitude() {
        return m_northEast_longitude;
    }

    public void setM_northEast_longitude(String m_northEast_longitude) {
        this.m_northEast_longitude = m_northEast_longitude;
    }

    public String getM_southWest_latitude() {
        return m_southWest_latitude;
    }

    public void setM_southWest_latitude(String m_southWest_latitude) {
        this.m_southWest_latitude = m_southWest_latitude;
    }

    public String getM_southWest_longitude() {
        return m_southWest_longitude;
    }

    public void setM_southWest_longitude(String m_southWest_longitude) {
        this.m_southWest_longitude = m_southWest_longitude;
    }

    public String getM_southEast_latitude() {
        return m_southEast_latitude;
    }

    public void setM_southEast_latitude(String m_southEast_latitude) {
        this.m_southEast_latitude = m_southEast_latitude;
    }

    public String getM_southEast_longitude() {
        return m_southEast_longitude;
    }

    public void setM_southEast_longitude(String m_southEast_longitude) {
        this.m_southEast_longitude = m_southEast_longitude;
    }

    public String getM_id() {
        return m_id;
    }

    public void setM_id(String m_id) {
        this.m_id = m_id;
    }

    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }

    public String getM_owner() {
        return m_owner;
    }

    public void setM_owner(String m_owner) {
        this.m_owner = m_owner;
    }

    public String getM_status() {
        return m_status;
    }

    public void setM_status(String m_status) {
        this.m_status = m_status;
    }

    public String getM_horizontal() {
        return m_horizontal;
    }

    public void setM_horizontal(String m_horizontal) {
        this.m_horizontal = m_horizontal;
    }

    public String getM_vertical() {
        return m_vertical;
    }

    public void setM_vertical(String m_vertical) {
        this.m_vertical = m_vertical;
    }

    public String getM_rotation() {
        return m_rotation;
    }

    public void setM_rotation(String m_rotation) {
        this.m_rotation = m_rotation;
    }

    public String getM_place_string() {
        return m_place_string;
    }

    public void setM_place_string(String m_place_string) {
        this.m_place_string = m_place_string;
    }

    public String getM_place_latitude() {
        return m_place_latitude;
    }

    public void setM_place_latitude(String m_place_latitude) {
        this.m_place_latitude = m_place_latitude;
    }

    public String getM_place_longitude() {
        return m_place_longitude;
    }

    public void setM_place_longitude(String m_place_longitude) {
        this.m_place_longitude = m_place_longitude;
    }

    public String getM_center_point_latitude() {
        return m_center_point_latitude;
    }

    public void setM_center_point_latitude(String m_center_point_latitude) {
        this.m_center_point_latitude = m_center_point_latitude;
    }

    public String getM_center_point_longitude() {
        return m_center_point_longitude;
    }

    public void setM_center_point_longitude(String m_center_point_longitude) {
        this.m_center_point_longitude = m_center_point_longitude;
    }

    public String getM_up() {
        return m_up;
    }

    public void setM_up(String m_up) {
        this.m_up = m_up;
    }

    public String getM_down() {
        return m_down;
    }

    public void setM_down(String m_down) {
        this.m_down = m_down;
    }

    public String getM_left() {
        return m_left;
    }

    public void setM_left(String m_left) {
        this.m_left = m_left;
    }

    public String getM_right() {
        return m_right;
    }

    public void setM_right(String m_right) {
        this.m_right = m_right;
    }

    public String getM_unit_scale() {
        return m_unit_scale;
    }

    public void setM_unit_scale(String m_unit_scale) {
        this.m_unit_scale = m_unit_scale;
    }
}
