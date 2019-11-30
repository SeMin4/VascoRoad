package com.example.woo.myapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.woo.myapplication.MyGlobals;
import com.example.woo.myapplication.OverlapExamineData;
import com.example.woo.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnusualRecordActivity extends Activity {
    private final int PICK_IMAGE = 0;
    private final int CAPTURE_IMAGE = 1;
    private int mapId;
    private double latitude;
    private double longitude;
    private Socket mSocket;
    private ImageView imageView;
    private String currentPhotoPath=null;
    private MyGlobals.RetrofitExService retrofitExService=null;
    private  EditText unusual_things;
    private String lat,lng,content,photo_name;
    private int index;
    private int received_index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_unusual_status);
        if(ExistingMapActivity.mSocket!=null)
            mSocket = ExistingMapActivity.mSocket; //기존지도에서 들어옴
        else
            mSocket = NewMapActivity.mSocket; //새로만든 지도에서 들어옴
        mSocket.on("not_complete", not_complete);
        unusual_things = findViewById(R.id.editText_for_unusual);
        retrofitExService = MyGlobals.getInstance().getRetrofitExService();
        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v ->{
            showPictureDialog();
        });

        Intent intent = getIntent();
        mapId = intent.getIntExtra("mapId", -1);
        latitude = intent.getDoubleExtra("latitude", -1);
        longitude = intent.getDoubleExtra("longitude", -1);
        index = intent.getIntExtra("index",-1);
        System.out.println("unusualRecord  index : "+index);
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
                String[] proj = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(contentURI, proj, null, null, null);
                cursor.moveToNext();
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                currentPhotoPath = path;
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
    private void uploadImage(String filePath){

        System.out.println("f_path : "+filePath );
        File file = new File(filePath);
        System.out.println("upload 이미지@@@@@@@@@@@@");

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload", file.getName(), fileReqBody);
        RequestBody map_id = RequestBody.create(MediaType.parse("text/plain"), "" + mapId);
        System.out.println("mapid : " + mapId);

        retrofitExService.postNotComplete(map_id,part).enqueue(new Callback<OverlapExamineData>() {
            @Override
            public void onResponse(Call<OverlapExamineData> call, Response<OverlapExamineData> response) {
                System.out.println("onResponse 호출됨@@@@@@@@@@@@@@@");
                OverlapExamineData data = response.body();
                if (data.getOverlap_examine().equals("yes")) {
                    System.out.println("yes");
                    Toast.makeText(getApplicationContext(), data.getOverlap_examine(), Toast.LENGTH_SHORT).show();
                } else {
                    // Toast.makeText(getApplicationContext(),"insert 실패",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OverlapExamineData> call, Throwable t) {
                System.out.println("onFailure 호출됨@@@@@@@@@@@@@@@");
                Toast.makeText(getApplicationContext(), "insert 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void mOnSave(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Saved");
        intent.putExtra("desc", content);
        intent.putExtra("image", currentPhotoPath);
        setResult(RESULT_OK, intent);
        String content = unusual_things.getText().toString();

        if(currentPhotoPath!=null){
            uploadImage(currentPhotoPath);
            JSONObject data = new JSONObject();
            try{
                data.put("mid",mapId);
                data.put("desc",content);
                data.put("lat",latitude);
                data.put("lng",longitude);
                String[] temp = currentPhotoPath.split("/");
                data.put("photo_name", temp[temp.length-1]);
                data.put("index",index);
            }catch (JSONException e){
                System.out.println("수색불과 정보 보냈을 때 에러");
            }
            mSocket.emit("not_complete", data);
        }else{
            JSONObject data = new JSONObject();
            try{
                data.put("mid",mapId);
                data.put("desc",content);
                data.put("lat",latitude);
                data.put("lng",longitude);
                data.put("index",index);
            }catch (JSONException e){
                System.out.println("수색불과 정보 보냈을 때 에러");
            }
            mSocket.emit("not_complete", data);
        }


        finish();
    }

    private Emitter.Listener not_complete = new Emitter.Listener() { //수색불가
        @Override
        public void call(Object... args) {
            try{
                JSONObject receivedData = (JSONObject)args[0];
                lat = receivedData.getString("lat");
                lng = receivedData.getString("lng");
                content = receivedData.getString("desc");
                photo_name = receivedData.getString("photo_name");
                received_index =Integer.parseInt(receivedData.getString("index"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
    };


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
