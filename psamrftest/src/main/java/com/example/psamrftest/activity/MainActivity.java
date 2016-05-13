package com.example.psamrftest.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.psamrftest.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button PSAMbt, RFbt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
    }

    private void initView() {
        PSAMbt = (Button) findViewById(R.id.psam_bt);
        RFbt = (Button) findViewById(R.id.rf_bt);
    }

    private void setListener() {
        PSAMbt.setOnClickListener(this);
        RFbt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.psam_bt:
                intent.setClass(MainActivity.this, PSAMTestActivity.class);
                break;
            case R.id.rf_bt:
                intent.setClass(MainActivity.this, RFTestActivity.class);

                break;
            default:
                break;
        }

        startActivity(intent);
    }
}
