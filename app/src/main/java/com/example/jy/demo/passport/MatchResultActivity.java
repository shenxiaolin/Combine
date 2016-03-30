package com.example.jy.demo.passport;


import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MatchResultActivity extends Activity implements OnClickListener {

    private ImageView img_photo = null;
    @SuppressWarnings("unused")
    private TextView tv_id, tv_name, tv_gender, tv_birthday, tv_address;
    private TextView edt_id, edt_name, edt_gender, edt_birthday, edt_address;

    private Button bt_back, entryBT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_result_info);

        edt_id = (TextView) findViewById(R.id.dcsinfo_id);
        edt_name = (TextView) findViewById(R.id.dcsinfo_name);
        edt_gender = (TextView) findViewById(R.id.dcsinfo_gender);
        edt_birthday = (TextView) findViewById(R.id.dcsinfo_bir);
        edt_address = (TextView) findViewById(R.id.dcsinfo_address);
        img_photo = (ImageView) findViewById(R.id.usr_photo);

        Intent data = getIntent();
        boolean haveData = data.getBooleanExtra("haveData", false);
        if (haveData) {
            if (null != gatherFinger.gather_id) {
                edt_id.setText(gatherFinger.gather_id);
            }
            if (null != gatherFinger.gather_name) {
                edt_name.setText(gatherFinger.gather_name);
            }
            if (null != gatherFinger.gender) {
                edt_gender.setText(gatherFinger.gender);
            }
            if (null != gatherFinger.birthday) {
                edt_birthday.setText(gatherFinger.birthday);
            }
            if (null != gatherFinger.address) {
                edt_address.setText(gatherFinger.address);
            }

            if (null != gatherFinger.photo_pic_path) {
                try {
                    FileInputStream fis = new FileInputStream(gatherFinger.photo_pic_path);
                    img_photo.setImageBitmap(BitmapFactory.decodeStream(fis));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        bt_back = (Button) findViewById(R.id.dcs_info_back);
        bt_back.setOnClickListener(this);

        entryBT = (Button) findViewById(R.id.entry_fingerprint_bt);
        entryBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.entry_fingerprint_bt:
                Intent intent = new Intent(MatchResultActivity.this, CameraOpen.class);
                startActivity(intent);
                finish();
                break;
            case R.id.dcs_info_back:
                finish();
                break;
            default:
                break;
        }
    }
}
