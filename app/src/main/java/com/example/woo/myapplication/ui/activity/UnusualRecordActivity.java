package com.example.woo.myapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.woo.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UnusualRecordActivity extends Activity {
    private final int PICK_IMAGE = 0;
    private final int CAPTURE_IMAGE = 1;
    private int mapId;
    private double latitude;
    private double longitude;
    private ImageView imageView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_unusual_status);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v ->{
            showPictureDialog();
        });

        Intent intent = getIntent();
        mapId = intent.getIntExtra("mapId", -1);
        latitude = intent.getDoubleExtra("latitude", -1);
        longitude = intent.getDoubleExtra("longitude", -1);
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
                        case PICK_IMAGE:
                            choosePhotoFromGallary();
                            break;
                        case CAPTURE_IMAGE:
                            dispatchTakePictureIntent();
                            break;
                    }
                });
        pictureDialog.show();
    }

    private void choosePhotoFromGallary() {
        // 갤러리 실행
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    private void galleryAddPic() {
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Log.d("image", "사진위치: " + contentUri.getPath());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(f));
        sendBroadcast(intent);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.woo.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

                    Toast.makeText(UnusualRecordActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(UnusualRecordActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAPTURE_IMAGE) {
            galleryAddPic();
            try{
                Uri contentUri = Uri.fromFile(new File(currentPhotoPath));
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                imageView.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void mOnSave(View v){
        EditText unusual_things = findViewById(R.id.editText_for_unusual);

        Intent intent = new Intent();
        intent.putExtra("result", "Saved");
        setResult(RESULT_OK, intent);

        /* 수색 불가 지점에 대한 정보를 서버로 전송(홍성기) */
        // currentPhotoPath: 이미지 저장 경로
        // content: 수색불가한 이유
        String content = unusual_things.getText().toString();

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
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){ // 바깥레이어 클릭시 안닫히게
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        finish();
    }
}
