package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.R;
import com.naver.maps.geometry.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsertMpersons extends AppCompatActivity {
    protected TextView Mperson_select_date;
    protected DatePickerDialog.OnDateSetListener mDateSetListener;
    protected DatePickerDialog.OnDateSetListener mAgeSetListener;
    protected Button getLocationBtn;
    protected EditText mpersonName;
    protected EditText searchLocation;
    protected TextView missingLocation;
    protected TextView Mperson_age;
    protected LinearLayout insertMpersonLayout;
    protected ImageView imageView;
    private final int PICK_IMAGE = 0;
    private final int PICK_LOCATION = 1;
    private String currentPhotoPath;
    private LatLng missingLatLng;
    private Button insert;
    protected EditText description;
    public static Activity _InsertMpersons;

    private String searchLocate = "";
    private MyGlobals.RetrofitExService retrofitExService;

    private void choosePhotoFromGallary() {
        // 갤러리 실행
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                Uri contentURI = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(contentURI, proj, null, null, null);
                cursor.moveToNext();
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                currentPhotoPath = path;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(requestCode == PICK_LOCATION){
            if(resultCode == RESULT_OK){
                double lat = data.getDoubleExtra("latitude", -1);
                double lng = data.getDoubleExtra("longitude", -1);
                missingLatLng = new LatLng(lat, lng);
                String address = "선택된 주소: " + findAddress(lat, lng);
                missingLocation.setVisibility(View.VISIBLE);
                missingLocation.setText(address);
            }
        }
    }

    public void updateMperson(String filepath,String name1,String age1,String date1,String place1,String lat,String lon,String des){
        File file = new File(filepath);
        System.out.println(filepath+" "+name1+ " "+ age1+" ");
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload",file.getName(),fileReqBody);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"),name1);
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"),age1);
        RequestBody date = RequestBody.create(MediaType.parse("text/plain"),date1);
        RequestBody place = RequestBody.create(MediaType.parse("text/plain"),place1);
        RequestBody latitude = RequestBody.create(MediaType.parse("text/plain"),lat);
        RequestBody longitude = RequestBody.create(MediaType.parse("text/plain"),lon);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),des);

        retrofitExService.postInsertMperson(part,name,age,date,place,latitude,longitude,description).enqueue(new Callback<OverlapExamineData>() {
            @Override
            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                System.out.println("onResponse 호출@@@@@@@@@@@@@@@@@@@@@@@");
                OverlapExamineData data = response.body();
                if(data.getOverlap_examine().equals("yes")){
                    Toast.makeText(getApplicationContext(),"삽입 성공",Toast.LENGTH_SHORT).show();
                    MainActivity._MainActivity.finish();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"삽입 실패",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                System.out.println("onFailure 호출@@@@@@@@@@@@@@@@@@@@@@@@");
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_mpersons);
        _InsertMpersons = InsertMpersons.this;
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        Mperson_select_date = (TextView) findViewById(R.id.mperson_select_date);
        getLocationBtn = (Button) findViewById(R.id.getLocation_btn) ;
        searchLocation = (EditText)findViewById(R.id.search_location);
        missingLocation = findViewById(R.id.mperson_missing_address);
        insertMpersonLayout = (LinearLayout)findViewById(R.id.insertMperson_layout);
        mpersonName = (EditText)findViewById(R.id.mperson_name);
        Mperson_age = (TextView) findViewById(R.id.mperson_age);
        imageView = (ImageView)findViewById(R.id.imageView);
        insert = (Button)findViewById(R.id.insert);
        description = (EditText)findViewById(R.id.mperson_description);

        missingLocation.setVisibility(View.INVISIBLE);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhotoFromGallary();
            }
        });

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("insert버튼 클릭");
                String name = mpersonName.getText().toString();
                String age = Mperson_age.getText().toString();
                String a1 = age.split(" ")[0];
                String a2 = age.split(" ")[1];
                String a3 = age.split(" ")[2];
                a2 = String.format("%02d",Integer.parseInt(a2.substring(0,a2.length()-1)));
                a3 = String.format("%02d",Integer.parseInt(a3.substring(0,a3.length()-1)));
                age = a1.substring(0,a1.length()-1)+a2+a3;
                String date = Mperson_select_date.getText().toString();
                String place = missingLocation.getText().toString().substring(8);
                String lat = Double.toString(missingLatLng.latitude);
                String lon = Double.toString(missingLatLng.longitude);
                String desc = description.getText().toString();

                updateMperson(currentPhotoPath,name,age,date,place,lat,lon,desc);
            }
        });




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
                        mAgeSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mAgeSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d("Date", "onDateSet: mm/dd/yyy: "+ year + month + dayOfMonth);
                month  = month + 1;
                String  Date = year + "년 " + month + "월 " + dayOfMonth +"일";
                Mperson_age.setText(Date);
            }

        };

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
                         Toast.makeText(InsertMpersons.this, "구글지도에 제공되지 않는 위치입니다.", Toast.LENGTH_LONG).show();
                     }else{
                         Intent intent = new Intent(InsertMpersons.this, RegisterMissinLatLng.class);
                         intent.putExtra("latitude", list.get(0).getLatitude());
                         intent.putExtra("longitude", list.get(0).getLongitude());
                         startActivityForResult(intent, PICK_LOCATION);
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
