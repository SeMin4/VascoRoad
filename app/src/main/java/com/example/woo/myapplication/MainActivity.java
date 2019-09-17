package com.example.woo.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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



    }

}



