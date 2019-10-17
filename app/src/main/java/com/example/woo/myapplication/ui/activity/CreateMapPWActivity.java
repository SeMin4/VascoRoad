package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woo.myapplication.R;

public class CreateMapPWActivity extends Activity {
    private EditText password1;
    private EditText password2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_create_mappw);

        password1 = (EditText) findViewById(R.id.EditText_password);
        password2 = (EditText) findViewById(R.id.EditText_re_password);
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

    }

    public void mOnAccept(View v){
        String strPassword1 = password1.getText().toString();
        String strPassword2 = password2.getText().toString();
        if (strPassword1.equals(strPassword2)) {
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
