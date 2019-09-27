package com.example.woo.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPageActivity extends AppCompatActivity implements MyRoomListAdapter.ListBtnClickListener{
    protected LinearLayout MyPageLayout;
    protected EditText change_password;
    protected EditText change_check_password;
    protected EditText change_department;
    protected Button change_confirm_btn;
    protected TextView user_id;
    protected TextView user_name;
    protected ListView my_room_list_view;
    static MyRoomListAdapter roomListAdapter;
    protected Button change_password_btn,change_department_btn;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        MyPageLayout = (LinearLayout) findViewById(R.id.my_page_layout);
        change_password = (EditText) findViewById(R.id.change_password);
        change_check_password = (EditText) findViewById(R.id.change_check_password);
        change_department = (EditText) findViewById(R.id.change_department);
        change_confirm_btn = (Button) findViewById(R.id.change_confirm_btn);
        user_id = (TextView)findViewById(R.id.user_id);
        user_name = (TextView)findViewById(R.id.user_name);
        my_room_list_view = (ListView)findViewById(R.id.my_room_list);

        roomListAdapter = new MyRoomListAdapter(this);
        my_room_list_view.setAdapter(roomListAdapter);

        roomListAdapter.addItem("천지완","대구광역시 북구");
        roomListAdapter.addItem("홍성기","대구광역시 동구");
        roomListAdapter.addItem("황보승우","대구광역시 서구");
        roomListAdapter.addItem("강민정","대구광역시 서구");
        roomListAdapter.addItem("오세민","대구광역시 서구");
        roomListAdapter.addItem("신준희","대구광역시 서구");
        roomListAdapter.addItem("황보승우","대구광역시 서구");
        roomListAdapter.addItem("황보승우","대구광역시 서구");
        roomListAdapter.addItem("황보승우","대구광역시 서구");
        roomListAdapter.addItem("황보승우","대구광역시 서구");
        change_password_btn = (Button)findViewById(R.id.change_pasaword_btn);
        change_department_btn = (Button)findViewById(R.id.change_department_btn);
        retrofit = MyGlobals.getInstance().getRetrofit();
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();

        user_id.setText(MyGlobals.getInstance().getUser().getU_email());
        user_name.setText(MyGlobals.getInstance().getUser().getU_name());
        change_department.setHint(MyGlobals.getInstance().getUser().getU_department());


        MyPageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(change_password.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(change_check_password.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(change_department.getWindowToken(), 0);

            }
        });
        change_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                else {
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                startActivity(intent);
            }
        });
        change_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(change_password.getText().toString().equals(change_check_password.getText().toString())){
                    HashMap<String,String> input = new HashMap<>();
                    input.put("u_id",MyGlobals.getInstance().getUser().getU_id());
                    input.put("password",change_password.getText().toString());

                    retrofitExService.postChangePassword(input).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            System.out.println("onResponse 호출@@@@@@@@@@@@@@");
                            User user = response.body();
                            if(user.getCheck().equals("yes")){
                                Toast.makeText(getApplicationContext(),"비밀번호 변경 되었습니다",Toast.LENGTH_SHORT).show();
                            }else if(user.getCheck().equals("no")){
                                Toast.makeText(getApplicationContext(),"비밀번호 변경 에러입니다.",Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            System.out.println("onFailure 호출@@@@@@@@@@@@@@");
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(),"비밀번호를 체크해주세요",Toast.LENGTH_SHORT).show();
                }
            }
        });

        change_department_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !(change_department.getText().toString().equals(""))){
                    retrofitExService.getChangeDepartment(MyGlobals.getInstance().getUser().getU_id(), change_department.getText().toString()).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            System.out.println("onResponse@@@@@@@@@@@@@@@@@@@");
                            User user = response.body();
                            if(user.getCheck().equals("yes")) {
                                MyGlobals.getInstance().getUser().setU_department(change_department.getText().toString());
                                change_department.setText("");
                                change_department.setHint(MyGlobals.getInstance().getUser().getU_department());
                            }
                            else if(user.getCheck().equals("no"))
                                Toast.makeText(getApplication(),"에러 발생 department 변경 실패",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            System.out.println("onFailure@@@@@@@@@@@@@@@@@@@@");
                            Toast.makeText(getApplication(),"에러 발생 department 변경 실패",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        //roomList 클릭 이벤트 핸들러 || 혹시 몰라서 적어놓음
        my_room_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                MyRoomItem item = (MyRoomItem) parent.getItemAtPosition(position) ;

                String titlePerson = item.getTitle_person() ;
                String locationMap = item.getLocation_map() ;

            }
        }) ;
    }

    @Override
    public  void onListBtnClick(int position){
        Intent intent = new Intent (getApplicationContext(),RoomDeleteActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }
}
