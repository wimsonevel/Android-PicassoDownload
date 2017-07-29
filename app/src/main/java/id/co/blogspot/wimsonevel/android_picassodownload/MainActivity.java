package id.co.blogspot.wimsonevel.android_picassodownload;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private static final String PREF_CAMERA_REQUESTED = "cameraRequested";

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivImage= (ImageView) findViewById(R.id.iv_image);
        Button btnSave = (Button) findViewById(R.id.btn_save);

        url = "https://scontent-sit4-1.xx.fbcdn.net/v/t1.0-9/20228885_1440264296060520_6773935769024601349_n.jpg?oh=da445041a4bc99ec499b78d39b8832eb&oe=5A371E89";

        Picasso.with(this)
                .load(url)
                .into(ivImage);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage(url, "newfile.jpg");
            }
        });
    }

    private void saveImage(String url, final String imgName) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "In progress...", Toast.LENGTH_SHORT).show();

            Picasso.with(this)
                    .load(url)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File folder = new File(sd, "/PicassoDownload/");
                            if (!folder.exists()) {
                                if (!folder.mkdir()) {
                                    Log.e("ERROR", "Cannot create a directory!");
                                } else {
                                    folder.mkdir();
                                }
                            }

                            File fileName = new File(folder, imgName);

                            try {
                                fileName.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(fileName);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                                ostream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(MainActivity.this, "Image Saved Successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Toast.makeText(MainActivity.this, "Image Failed to Save", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        } else {
            requestWriteExternalPermission();
        }
    }

    private void requestWriteExternalPermission() {
        final String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            if (!isPermissionRequested(PREF_CAMERA_REQUESTED)) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                setPermissionRequested(PREF_CAMERA_REQUESTED);
            } else {
                Toast.makeText(MainActivity.this, "Please grant storage permission to save images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(permission, true);
        editor.apply();
    }

    private boolean isPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(permission, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SUCCESS", "Write External permission granted");
                    saveImage(url, "newFile.jpg");
                    return;
                }
                Log.e("ERROR", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                finish();
            }
            default: {
                Log.d("ERROR", "Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }
}
