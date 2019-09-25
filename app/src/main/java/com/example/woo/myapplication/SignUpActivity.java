package com.example.woo.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {

    private EditText email,password,name,department;
    private Button overlap_check;
    private Retrofit retrofit;
    private MyGlobals.RetrofitExService retrofitExService;
    private Button admit; //회원가입 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        overlap_check = (Button)findViewById(R.id.overlap_check);
        email = (EditText) findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        name = (EditText)findViewById(R.id.name);
        department = (EditText)findViewById(R.id.department);
        admit = (Button)findViewById(R.id.admit);

        if( (MyGlobals.getInstance().getRetrofit() == null) || (MyGlobals.getInstance().getRetrofitExService() ==null) ){
            retrofit = new Retrofit.Builder().baseUrl(MyGlobals.RetrofitExService.URL).addConverterFactory(GsonConverterFactory.create()).build();
            retrofitExService = retrofit.create(MyGlobals.RetrofitExService.class);
            MyGlobals.getInstance().setRetrofit(retrofit);
            MyGlobals.getInstance().setRetrofitExService(retrofitExService);
        }else{
            retrofit = MyGlobals.getInstance().getRetrofit();
            retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        }


        overlap_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //중복체크 할 떄
                HashMap<String,String> input = new HashMap<>();
                input.put("email",email.getText().toString());
                //input.put("test","test");
                retrofitExService.postData(input).enqueue(new Callback<OverlapExamineData>() {
                    @Override
                    public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                        System.out.println("onResponse 호출");
                        OverlapExamineData overlapData = response.body();
                        if(overlapData.getOverlap_examine().equals("access")) {
                            Toast.makeText(getApplicationContext(),"사용 가능한 이메일 입니다.",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"이미 존재하는 이메일 입니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                        System.out.println("onFailure 호출");
                        System.out.println(t);
                    }
                });
            }
        });

        admit.setOnClickListener(new View.OnClickListener() {  //회원가입 버튼 눌렀을떄
            @Override
            public void onClick(View view) {
                String e = email.getText().toString(); //입력한 이메일
                String p = password.getText().toString(); //입력한 패스워드
                String n = name.getText().toString(); //입력한 이름
                String d = department.getText().toString();

                if( e.equals("") || p.equals("") || n.equals("") || d.equals("")){
                    System.out.println("모두 입력해주세요");
                }else{
                    HashMap<String,String> input = new HashMap<>();
                    input.put("email",e);
                    input.put("password",p);
                    input.put("department",d);
                    input.put("name",n);

                    retrofitExService.postAdmin(input).enqueue(new Callback<OverlapExamineData>() {
                        @Override
                        public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                            System.out.println("onResponse@@@@@@@@@@@@@@");
                            OverlapExamineData overlapExamineData = response.body();
                            if(overlapExamineData.getOverlap_examine().equals("success")) {
                                Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else if(overlapExamineData.getOverlap_examine().equals("deny"))
                                Toast.makeText(getApplicationContext(),"회원가입 실패",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                            System.out.println("onFailure@@@@@@@@@@@@@@");
                        }
                    });

                }


            }
        });
    }
}
