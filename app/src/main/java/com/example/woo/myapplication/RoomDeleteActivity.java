package com.example.woo.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RoomDeleteActivity extends AppCompatActivity {

    protected Button room_delete_confirm_btn;
    protected EditText room_delete_password;
    protected int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_room_delete);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

        room_delete_confirm_btn = (Button)findViewById(R.id.room_delete_confirm_btn);
        room_delete_password = (EditText)findViewById(R.id.room_delete_password);
        room_delete_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(room_delete_password.getText().toString().equals("12")){
                    Toast.makeText(getApplicationContext(), MyPageActivity.roomListAdapter.myRoomList.get(position).getTitle_person() + "의 방이 삭제 되었습니다", Toast.LENGTH_SHORT).show();
                    MyPageActivity.roomListAdapter.myRoomList.remove(position);
                    MyPageActivity.roomListAdapter.notifyDataSetChanged();
                    finish();
                }
            }
        });
    }
}
