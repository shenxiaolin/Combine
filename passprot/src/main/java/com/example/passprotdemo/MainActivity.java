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
                //
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
                    Log.e("", "打开模块成功...");
                    if (0 != cardKit.ReadCard()) {
                        Log.e("读卡失败", "锟诫将锟斤拷锟秸匡拷锟斤拷锟斤拷锟斤拷锟斤拷锟�");
                        Toast.makeText(MainActivity.this, "锟斤拷锟斤拷锟铰讹拷锟斤拷", 0).show();
                    } else {
                        Bitmap bmp = cardKit.getPhotoBmp();
                        if (null != bmp) {
                            usr_photo.setImageBitmap(bmp);
                            Toast.makeText(MainActivity.this, "读取头像成功", 1).show();
                            Log.e("", "锟斤拷锟斤拷锟缴癸拷");

                            Log.e("锟斤拷锟斤拷锟斤拷锟斤拷", cardKit.getName());//锟斤拷锟斤拷锟斤拷
                            Log.e("MRZ1", cardKit.getMRZ2());//MRZ1
                            Log.e("MRZ2", cardKit.getMRZ1());//MRZ2
                            //Log.e("签锟斤拷锟斤拷锟�, cardKit.getIssuer());//签锟斤拷锟斤拷锟�
                            Log.e("锟斤拷锟�", cardKit.getDocType());//锟斤拷锟
                            Log.e("锟斤拷锟斤拷锟斤拷锟斤拷", cardKit.getBirthDate());//锟斤拷锟斤拷锟斤拷锟斤拷
                            Log.e("证锟斤拷锟斤拷锟斤拷", cardKit.getDocNumber());//证锟斤拷锟斤拷锟斤拷
                            Log.e("锟斤拷", cardKit.getNationlity());//锟斤拷
                            Log.e("锟斤拷选锟斤拷锟�", cardKit.getOptData());//锟斤拷选锟斤拷锟�
                            Log.e("锟皆憋拷", cardKit.getSex());//锟皆憋拷
                            Log.e("英锟斤拷锟斤拷", cardKit.getSurname());//英锟斤拷锟斤拷
                            Log.e("锟斤拷效锟斤拷", cardKit.getValiduntil());//锟斤拷效锟斤拷

                        } else {
                            Toast.makeText(MainActivity.this, "图片锟斤拷", 0).show();
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
