package com.example.woo.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {
    LoginActivity _LoginActivity = (LoginActivity)LoginActivity._LoginActivity;
    protected Button logout_btn;
    protected Button myPage_btn;
	// Hello I'm minjeong.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _LoginActivity.finish();
        logout_btn = (Button) findViewById(R.id.logout_btn);
        myPage_btn = (Button) findViewById(R.id.my_page_btn);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String DistrictName = parent.getItemAtPosition(position).toString();
                //입력값을 변수에 저장한다.

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Intent intent = new Intent(MainActivity.this, ListVieww.class);
        //현재 클래스에서 리스트뷰로 이동하는 인텐트

        //버튼을 누르면 실종자 리스트로 이동한다
        Button SelectionButton = (Button) findViewById(R.id.Button_SelectionComplete);
        SelectionButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor auto_editor = auto.edit();
                auto_editor.clear();
                auto_editor.commit();
                Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent1);
                finish();
            }
        });
        myPage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(),MyPageActivity.class);
                startActivity(intent1);
            }
        });


    }

}



