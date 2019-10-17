package com.example.woo.myapplication;

import com.example.woo.myapplication.data.MapInfo;
import com.example.woo.myapplication.data.Mperson;
import com.example.woo.myapplication.data.User;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class MyGlobals {
    private User user = null; //user 정보 저장
    private ArrayList<MyRoomItem> maplist = null;
    private Retrofit retrofit=null;
    private RetrofitExService retrofitExService=null;
    private static MyGlobals instance = null;

    public ArrayList<MyRoomItem> getMaplist() {
        return maplist;
    }

    public void setMaplist(ArrayList<MyRoomItem> maplist) {
        this.maplist = maplist;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public RetrofitExService getRetrofitExService() {
        return retrofitExService;
    }

    public void setRetrofitExService(RetrofitExService retrofitExService) {
        this.retrofitExService = retrofitExService;
    }

    public static synchronized MyGlobals getInstance() {
        if(instance == null)
            instance = new MyGlobals();

        return instance;
    }

    public static void setInstance(MyGlobals instance) {
        MyGlobals.instance = instance;
    }

    public interface RetrofitExService{ //interface 선언
        public static final String URL = "http://13.125.95.139:9000/"; //서버 주소와 포트번호

        @GET("/mperson")
        Call<ArrayList<Mperson>> getData();

        @FormUrlEncoded
        @POST("/examine")
        Call<OverlapExamineData> postData(@FieldMap HashMap<String,String> param);

        @FormUrlEncoded
        @POST("/login/process")
        Call<User> postLogin(@FieldMap HashMap<String,String> param);

        @FormUrlEncoded
        @POST("/admin/process")
        Call<OverlapExamineData> postAdmin(@FieldMap HashMap<String,String> param);

        @FormUrlEncoded
        @POST("/change/password")
        Call<User> postChangePassword(@FieldMap HashMap<String,String> param);

        @GET("/change/department?") //URL
        Call<User> getChangeDepartment(@Query("u_id") String u_id, @Query("u_department") String u_department);

        @FormUrlEncoded
        @POST("/delete/room")
        Call<User> postDeleteRoom(@FieldMap HashMap<String,String> param);

        @GET("mypage/maplist?")
        Call<ArrayList<MyRoomItem>> getMypageMapData(@Query("u_id") String m_id);

        @GET("person/maplist?")
        Call<ArrayList<MapInfo>> getPersonMapData(@Query("p_id") String p_id);

        @FormUrlEncoded
        @POST("/map/make")
        Call<OverlapExamineData> postMapMake(@FieldMap HashMap<String,String> param);
    }

}
