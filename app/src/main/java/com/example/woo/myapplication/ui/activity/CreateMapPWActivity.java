package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.R;

public class CreateMapPWActivity extends Activity {
    private EditText password1;
    private EditText password2;
    private ConstraintLayout createMapPWActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_create_mappw);

        password1 = (EditText) findViewById(R.id.EditText_password);
        password2 = (EditText) findViewById(R.id.EditText_re_password);
        createMapPWActivity = (ConstraintLayout)findViewById(R.id.create_map_password_layout);
        TextView error = (TextView) findViewById(R.id.textView_error);

        password2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String strPass1 = password1.getText().toString();
                String strPass2 = password2.getText().toString();
                if (strPass1.equals(strPass2)) {
                    error.setText(R.string.settings_pwd_equal);
                } else {
                    error.setText(R.string.settings_pwd_not_equal);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        createMapPWActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(password1.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(password2.getWindowToken(), 0);
            }
        });
    }

    public void mOnAccept(View v){
        String strPassword1 = password1.getText().toString();
        String strPassword2 = password2.getText().toString();
        if (strPassword1.equals(strPassword2)&&!strPassword1.equals("")&&!strPassword2.equals("")) {
            Toast.makeText(this,
                    "Matching passwords="+strPassword2, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("password", strPassword1);
            setResult(RESULT_OK, intent);

            //액티비티(팝업) 닫기
            finish();
        }
        else{
            Toast.makeText(this, "비밀번호를 다시 확인하세요", Toast.LENGTH_SHORT).show();
        }
    }


    public void mOnCancel(View v){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
