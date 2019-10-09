package com.example.woo.myapplication.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_room_delete);
        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

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
                User user = MyGlobals.getInstance().getUser();
                HashMap<String,String> input = new HashMap<>();
                System.out.println("user id : " + user.getU_id());
                System.out.println("user pass : " + room_delete_password.getText().toString());

                input.put("u_id", user.getU_id());
                input.put("u_password",room_delete_password.getText().toString());

                retrofitExService.postDeleteRoom(input).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        System.out.println("onResponse 호출");
                        User user = response.body();
                        if(user.getCheck().equals("no")){
                            Toast.makeText(getApplicationContext(),"비밀번호가 틀립니다.",Toast.LENGTH_SHORT).show();
                        }else if(user.getCheck().equals("yes")){
                            Toast.makeText(getApplicationContext(),"방이 삭제 되었습니다.",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"방 삭제 실패입니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        System.out.println("onFailure 호출");
                        System.out.println(t);
                        Toast.makeText(getApplicationContext(),"방 삭제 실패",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
