package jp.techacademy.kimie.kajiura.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;

    Handler mHandler = new Handler();
    //メンバ変数
    //メンバ変数にすることによって、どこからでも使うことが出来る
    Cursor mCursor;

    Timer mTimer;
    double mTimersec = 0.0;

    Button mStartButton;
    Button mModoruButton;
    Button mStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartButton = (Button) findViewById(R.id.moveon);
        mModoruButton = (Button) findViewById(R.id.modoru);
        mStopButton = (Button) findViewById(R.id.stop);

        //Android6.0以降
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            }else {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;

            }
        }else {
            getContentsInfo();
        }

        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    //次の画像がある時にクリックした時はここにくる。
                    if (mCursor.moveToNext()) {

                    }else{
                        //画像の一番最後を表示させている時にクリックした時はここにくる。
                        mCursor.moveToFirst();
                    }
                    //カーソルが指す場所の情報を取得
                    int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Long id = mCursor.getLong(fieldIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    //画像を表示
                    ImageView image_view = (ImageView) findViewById(R.id.image_view);
                    image_view.setImageURI(imageUri);

                }catch (Exception e){
                    showAlertDialog();
                }
            }
        });

        mModoruButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    //前の画像がある時にクリックした時はここにくる。
                    if (mCursor.moveToPrevious()) {

                    }else{
                        //画像の一番最初を表示させている時にクリックした時はここにくる。
                        mCursor.moveToLast();
                    }
                    int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Long id = mCursor.getLong(fieldIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    ImageView image_view = (ImageView) findViewById(R.id.image_view);
                    image_view.setImageURI(imageUri);

                }catch (Exception e){
                    showAlertDialog();
                }
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTimer != null){
                    mTimer.cancel();
                    mTimer = null;
                    //ボタンの文字
                    Button button = (Button) findViewById(R.id.stop);
                    button.setText("再生");

                    mStartButton.setEnabled(true);
                    mModoruButton.setEnabled(true);
                }else {
                    //ボタンの文字
                    Button button = (Button) findViewById(R.id.stop);
                    button.setText("停止");

                    mStartButton.setEnabled(false);
                    mModoruButton.setEnabled(false);

                    mTimer = new Timer();
                    //タイマー始動関数
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //タイマー処理
                            //単位は秒
                            mTimersec += 2;

                            mHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    if (mCursor.moveToNext()) {

                                    }else{
                                        mCursor.moveToFirst();
                                    }
                                    try{
                                        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                                        Long id = mCursor.getLong(fieldIndex);
                                        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                                        ImageView image_view = (ImageView) findViewById(R.id.image_view);
                                        image_view.setImageURI(imageUri);

                                    }catch (Exception e){
                                        showAlertDialog();
                                        mTimer.cancel();
                                        mTimer = null;
                                        Button button = (Button) findViewById(R.id.stop);
                                        button.setText("再生/停止");
                                        mStartButton.setEnabled(true);
                                        mModoruButton.setEnabled(true);
                                     }
                                }
                            });
                        }
                    },2000,2000);//第2引数：最初の始動まで2000ミリ秒、第3引数：ループ間隔2000ミリ秒
                }
            }
        });
    }


    private void  showAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("データがありません。");

        alertDialogBuilder.setPositiveButton("ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] Permissions, int[] grantResult) {
        if (grantResult[0] != PackageManager.PERMISSION_GRANTED) {
            System.exit(0);
        }else{
            getContentsInfo();
        }
    }

    private void getContentsInfo() {
        //画像情報取得
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query (
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        );

        if (mCursor.moveToFirst()){
            try{
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView image_view = (ImageView) findViewById(R.id.image_view);
                image_view.setImageURI(imageUri);

            }catch (Exception e){

            }
        }
        //mCursor.close();
    }
}
