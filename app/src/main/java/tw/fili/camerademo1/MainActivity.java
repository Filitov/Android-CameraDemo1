package tw.fili.camerademo1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cit = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if( cit.resolveActivity(getPackageManager()) !=null ){
                    startActivityForResult( cit, 9453 );
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode==9453 && resultCode==RESULT_OK ){
            Bundle bundle = data.getExtras();
            Bitmap bmp = (Bitmap)bundle.get("data");
            ((ImageView)findViewById(R.id.imageView)).setImageBitmap( bmp );
        }
    }
}
