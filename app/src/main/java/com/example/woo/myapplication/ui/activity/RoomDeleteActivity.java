package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.R;
import com.example.woo.myapplication.data.User;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RoomDeleteActivity extends AppCompatActivity {

    protected Button room_delete_confirm_btn;
    protected EditText room_delete_password;
    protected int position;
    Retrofit retrofit;
    MyGlobals.RetrofitExService retrofitExService;
    protected String mapId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_room_delete);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        mapId = intent.getStringExtra("mapId");

        room_delete_confirm_btn = (Button)findViewById(R.id.room_delete_confirm_btn);
        room_delete_password = (EditText)findViewById(R.id.room_delete_password);
        /*room_delete_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(room_delete_password.getText().toString().equals("12")){
                    Toast.makeText(getApplicationContext(), MyPageActivity.roomListAdapter.myRoomList.get(position).getTitle_person() + "의 방이 삭제 되었습니다", Toast.LENGTH_SHORT).show();
                    MyPageActivity.roomListAdapter.myRoomList.remove(position);
                    MyPageActivity.roomListAdapter.notifyDataSetChanged();
                    finish();
                }
            }
        });*/
        room_delete_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                retrofit=MyGlobals.getInstance().getRetrofit();
                retrofitExService=MyGlobals.getInstance().getRetrofitExService();
                HashMap<String,String> input = new HashMap<>();
                input.put("mapId", mapId);
                input.put("password", room_delete_password.getText().toString());
                retrofitExService.postDeleteRoom(input).enqueue(new Callback<OverlapExamineData>() {
                    @Override
                    public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                        OverlapExamineData data = response.body();
                        if(data.getOverlap_examine().equals("yes")){
                            MyPageActivity.roomListAdapter.myRoomList.remove(position);
                            MyPageActivity.roomListAdapter.notifyDataSetChanged();
                            MyGlobals.getInstance().setMaplist(MyPageActivity.roomListAdapter.myRoomList);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "비밀번호를 다시 확인하세요.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                        Log.w("hong....", t);
                    }
                });

            }
        });
    }
}
