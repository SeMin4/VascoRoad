package com.example.woo.myapplication.ui.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.R;


import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InsertMpersons extends AppCompatActivity {
    protected TextView Mperson_select_date;
    protected DatePickerDialog.OnDateSetListener mDateSetListener;
    protected Button getLocationBtn;
    protected TextView latitude_txt_view;
    protected EditText mpersonName;
    protected EditText searchLocation;
    protected TextView Mperson_age;
    protected LinearLayout insertMpersonLayout;

    private String searchLocate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_mpersons);


        Mperson_select_date = (TextView) findViewById(R.id.mperson_select_date);
        getLocationBtn = (Button) findViewById(R.id.getLocation_btn) ;
        latitude_txt_view = (TextView) findViewById(R.id.mperson_latitude_longitude);
        latitude_txt_view.setMovementMethod(new ScrollingMovementMethod());
        searchLocation = (EditText)findViewById(R.id.search_location);
        insertMpersonLayout = (LinearLayout)findViewById(R.id.insertMperson_layout);
        mpersonName = (EditText)findViewById(R.id.mperson_name);
        Mperson_age = (TextView) findViewById(R.id.mperson_age);








        insertMpersonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(Mperson_select_date.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(searchLocation.getWindowToken(), 0);
            }
        });
        final Geocoder geocoder = new Geocoder(this);
        Mperson_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        InsertMpersons.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        Mperson_select_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        InsertMpersons.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d("Date", "onDateSet: mm/dd/yyy: "+ year + month + dayOfMonth);
                month  = month + 1;
                String  Date = year + "년 " + month + "월 " + dayOfMonth +"일";
                Mperson_select_date.setText(Date);
            }

        };
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocate = searchLocation.getText().toString();
                List<Address> list = null;
                 try{
                     list = geocoder.getFromLocationName(
                             searchLocate,
                             10
                     );
                 }catch (IOException e){
                     e.printStackTrace();

                 }
                 if(list != null){
                     if(list.size() == 0){
                         latitude_txt_view.setText("주소 없음");
                     }else{
                         String Address = findAddress(list.get(0).getLatitude(), list.get(0).getLongitude());
                         latitude_txt_view.setText(list.get(0).toString() + Address);
                     }
                 }

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchLocation.getWindowToken(), 0);
            }
        });
    }

    protected String findAddress(double lat, double lng){
        StringBuffer buffer = new StringBuffer();
        String LocationAddress;
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> address;
        try{
            if(geocoder != null){
                address = geocoder.getFromLocation(lat,lng,1);
                if(address!= null && address.size()>0){
                    LocationAddress = address.get(0).getAddressLine(0).toString();
                    buffer.append(LocationAddress);

                }
            }
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"주소 취득 실패", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
        return buffer.toString();

    }

}
