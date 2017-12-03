package tw.fili.camerademo1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

    String mPhotoFilename;

    private Uri getOutputPhotoURI(){
        final SimpleDateFormat photoSDF = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        mPhotoFilename = null;
        File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile;
        try {
            photoFile = File.createTempFile(
                    "My" + photoSDF.format(new Date()),
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

        return FileProvider.getUriForFile(this, "tw.fili.camerademo1.fileprovider", photoFile );
//        return photoURI;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode==9453 && resultCode==RESULT_OK ){
            galleryAddPic();
            setPic();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPhotoFilename);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private void setPic() {
        ImageView imgView = (ImageView)findViewById(R.id.imageView);
        // Get the dimensions of the View
        int targetW, targetH;
        targetW = imgView.getWidth();
        targetH = imgView.getHeight();

        // Get the dimensions of the bitmap
        int photoW, photoH;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoFilename, bmOptions);
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
