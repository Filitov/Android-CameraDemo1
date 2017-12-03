package tw.fili.camerademo1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStorePremission();

                Uri photoURI = getOutputPhotoURI();
                if(photoURI==null) return;

                Intent cit = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cit.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if( cit.resolveActivity(getPackageManager()) !=null ){
                    startActivityForResult( cit, 9453 );
                }
            }
        });
    }

    private void checkStorePremission(){
        String toCheck = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 使用 ContextCompat 避免 6.0 以前沒有這功能的問題
        if(ContextCompat.checkSelfPermission(this, toCheck)
                != PackageManager.PERMISSION_GRANTED ){
            if( Build.VERSION.SDK_INT>=23 ) {
                if (shouldShowRequestPermissionRationale(toCheck)) {
                    showRationaleForStore();//這裡應該要顯示為何要這個權限的介面
                }
                requestPermissions(new String[]{toCheck}, 1234);
            }else{
                showRationaleForStore();//這裡應該要顯示為何要這個權限的介面
            }
        }
    }

    private void showRationaleForStore(){
        new AlertDialog.Builder(this)
                .setMessage("操作相機需要儲存到檔案的功能")
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if( requestCode==1234 && Build.VERSION.SDK_INT>=23 ){
            if( grantResults.length==1 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                Toast.makeText(this,"取得權限",Toast.LENGTH_SHORT).show();
            }else{
                if( shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
                    showRationaleForStore();//這裡應該要顯示為何要這個權限的介面
                }else{
                    Toast.makeText(this,"無法取得權限",Toast.LENGTH_SHORT).show();
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    String mPhotoFilename;

    private Uri getOutputPhotoURI(){
        //照片檔名
        final SimpleDateFormat photoSDF = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String photoFilename = "My photo " + photoSDF.format(new Date());

        //存檔位置
        //File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File photoFile;
        try {
            photoFile = File.createTempFile(
                    photoFilename,
                    ".jpg",
                    storeDir
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "無法建立暫時保存相片的檔案",Toast.LENGTH_SHORT).show();
            return null;
        }

        mPhotoFilename = photoFile.getAbsolutePath();
        Log.i("filitov", mPhotoFilename);

        if( Build.VERSION.SDK_INT>=24 ) {
            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
        }else {
            return Uri.fromFile(photoFile);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode==9453 && resultCode==RESULT_OK ){
            galleryAddPic2();
            setPic();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(mPhotoFilename)));
        this.sendBroadcast(mediaScanIntent);
    }

    private void galleryAddPic2() {
        MediaScannerConnection.scanFile(this,
                new String[]{new File(mPhotoFilename).getPath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }


    private void setPic() {
        ImageView imgView = (ImageView)findViewById(R.id.imageView);
        // Get the dimensions of the View
        int targetW, targetH;
        targetW = imgView.getWidth();
        targetH = imgView.getHeight();

        //open file to get dimensions
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoFilename, bmOptions);

        // Get the dimensions of the bitmap
        int photoW, photoH;
        photoW = bmOptions.outWidth;
        photoH = bmOptions.outHeight;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = Math.min(photoW/targetW, photoH/targetH);
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFilename, bmOptions);
        imgView.setImageBitmap(bitmap);
    }
}
