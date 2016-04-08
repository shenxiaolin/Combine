package com.example.passprotdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helper.ReadCardKit;
import com.example.mrzdemo.R;

public class MainActivity extends Activity {
    private static String TAG = "moubiao";

    private AndroidAPP application;
    private static int DES_ENCRYPT = 1;
    private static int DES_DECRYPT = 0;
    private ImageView usr_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        application = (AndroidAPP) MainActivity.this.getApplicationContext();
        TextView mbuttonLockVote = (TextView) findViewById(R.id.tv_English);
        TextView mbuttonDataQuery = (TextView) findViewById(R.id.tv_Turkish);

        usr_photo = (ImageView) findViewById(R.id.usr_photo);


        mbuttonLockVote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /*		Intent intent = new Intent(MainActivity.this, PassprotActivity.class);
                          Bundle bundle=new Bundle();
			              bundle.putString("lan_id", "0");
			              intent.putExtras(bundle);   
			              startActivity(intent);  	
			    */
            }
        });

        mbuttonDataQuery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {


                ReadCardKit cardKit = new ReadCardKit();
                usr_photo.setBackgroundResource(R.drawable.photo);
                usr_photo.invalidate();
                //
                if (cardKit.openModule()) {
                    Log.e("TAG", "打开模块成功...");
                    int readRet = cardKit.ReadCard();//moubiao expend time here
                    if (0 != readRet) {
                        Log.d("TAG", "read failed");
                        Toast.makeText(MainActivity.this, "read failed", Toast.LENGTH_SHORT).show();
                    } else {
                        Bitmap bmp = cardKit.getPhotoBmp();//moubiao expend time here
                        if (null != bmp) {
                            usr_photo.setImageBitmap(bmp);
                            Toast.makeText(MainActivity.this, "读取头像成功", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "read success");
                            Log.d("TAG", cardKit.getName());
                            Log.d("TAG", cardKit.getMRZ2());
                            Log.d("TAG", cardKit.getMRZ1());
                            Log.d("TAG", cardKit.getDocType());
                            Log.d("TAG", cardKit.getBirthDate());
                            Log.d("TAG", cardKit.getDocNumber());
                            Log.d("TAG", cardKit.getNationlity());
                            Log.d("TAG", cardKit.getOptData());
                            Log.d("TAG", cardKit.getSex());
                            Log.d("TAG", cardKit.getSurname());
                            Log.d("TAG", cardKit.getValiduntil());
                        } else {
                            Toast.makeText(MainActivity.this, "no picture", Toast.LENGTH_SHORT).show();
                        }
                    }

                    cardKit.closeModule();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
