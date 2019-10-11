package com.example.woo.myapplication.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.woo.myapplication.R;

import java.io.IOException;

public class UnusualRecordActivity extends Activity {
    private final int PICK_IMAGE = 0;
    private final int CAPTURE_IMAGE = 1;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_unusual_status);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener(v ->{
            showPictureDialog();
        });

    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this, R.style.AlertDialog);
        pictureDialog.setTitle("사진 등록");
        String[] pictureDialogItems = {
                "갤러리에서 사진 선택하기",
                "카메라로 촬영하기" };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallary();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    saveImage(bitmap);

                    Toast.makeText(UnusualRecordActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(UnusualRecordActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAPTURE_IMAGE) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(UnusualRecordActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveImage(Bitmap bitmapImage) {
        String filename = "firma-"+ System.currentTimeMillis()+".png";
        String url = MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, filename , "Firma creada desde Signature app");
        Log.d("image", "path: " + url);

    }

    public void mOnSave(View v){
        EditText unusual_things = findViewById(R.id.editText_for_unusual);
        String content = unusual_things.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("Image", "PUT IMAGE HERE");
        intent.putExtra("content", content);
        intent.putExtra("result", "Saved");
        setResult(RESULT_OK, intent);

        finish();
    }


    public void mOnCancel(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

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
        //안드로이드 백버튼 막기
        return;
    }
}
