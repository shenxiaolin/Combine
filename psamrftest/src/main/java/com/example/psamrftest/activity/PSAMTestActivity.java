package com.example.psamrftest.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.psamrftest.R;
import com.example.psamrftest.fragment.PSAMSetParamsDialog;
import com.example.psamrftest.interfaces.SetPSAMParamsInterface;
import com.example.psamrftest.util.Converter;
import com.example.psamrftest.util.PSAMUtil;

import java.util.List;

/**
 * Created by moubiao on 2016/5/13.
 * <p>
 * PASAM卡测试界面
 */
public class PSAMTestActivity extends AppCompatActivity implements View.OnClickListener, SetPSAMParamsInterface {
    private final String TAG = "moubiao";

    private final int SLOT_FIRST = 1;
    private final int SLOT_SECOND = 2;
    private final int SLOT_THIRD = 3;
    private final int SLOT_FOURTH = 4;

    private Button startBT, stopBT;
    private EditText testCountET;
    private TextView psamFirstTV, psamSecondTV, psamThirdTV, psamFourthTV, showDataTV;

    private int slotIndex = 1;
    StringBuilder failedInfo;
    //复位
    byte[] resetLen = new byte[1];
    byte[] resetReceBuff = new byte[400];
    //apdu
    byte[] apduSendBufByte = null;
    byte[] apduReceBuff = new byte[400];
    short[] Revlen = new short[1];
    short[] SW = new short[1];
    //卡槽1
    private int firstSlotRate;
    private byte firstSlotVoltage;
    private byte firstSlotMode;

    //卡槽2
    private int secondSlotRate;
    private byte secondSlotVoltage;
    private byte secondSlotMode;

    //卡槽3
    private int thirdSlotRate;
    private byte thirdSlotVoltage;
    private byte thirdSlotMode;

    //卡槽4
    private int fourthSlotRate;
    private byte fourthSlotVoltage;
    private byte fourthSlotMode;

    private boolean testState = false;//判断是否正在检测
    private int cycleCount;//循环检测的次数
    private boolean flag = true;//控制线程结束


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psam_test_layout);

        iniData();
        initView();
        setListener();
        PSAMUtil.openPSAMModule();
    }

    private void iniData() {
        //卡槽1
        firstSlotRate = Integer.parseInt(getString(R.string.PSAM_default_baud_rate));
        firstSlotVoltage = 1;
        firstSlotMode = 0;
        //卡槽2
        secondSlotRate = Integer.parseInt(getString(R.string.PSAM_default_baud_rate));
        secondSlotVoltage = 1;
        secondSlotMode = 0;
        //卡槽3
        thirdSlotRate = Integer.parseInt(getString(R.string.PSAM_default_baud_rate));
        thirdSlotVoltage = 1;
        thirdSlotMode = 0;
        //卡槽4
        fourthSlotRate = Integer.parseInt(getString(R.string.PSAM_default_baud_rate));
        fourthSlotVoltage = 1;
        fourthSlotMode = 0;


        failedInfo = new StringBuilder();
        apduSendBufByte = Converter.hexStringToBytes("00A40000023F00");
    }

    private void initView() {
        psamFirstTV = (TextView) findViewById(R.id.psam_tv_1);
        psamSecondTV = (TextView) findViewById(R.id.psam_tv_2);
        psamThirdTV = (TextView) findViewById(R.id.psam_tv_3);
        psamFourthTV = (TextView) findViewById(R.id.psam_tv_4);

        showDataTV = (TextView) findViewById(R.id.show_data_tv);

        testCountET = (EditText) findViewById(R.id.test_count_tv);
        startBT = (Button) findViewById(R.id.start_bt);
        stopBT = (Button) findViewById(R.id.stop_bt);
    }

    private void setListener() {
        psamFirstTV.setOnClickListener(this);
        psamSecondTV.setOnClickListener(this);
        psamThirdTV.setOnClickListener(this);
        psamFourthTV.setOnClickListener(this);

        testCountET.setOnClickListener(this);
        startBT.setOnClickListener(this);
        stopBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.psam_tv_1:
                setPSAMParams(1);
                break;
            case R.id.psam_tv_2:
                setPSAMParams(2);
                break;
            case R.id.psam_tv_3:
                setPSAMParams(3);
                break;
            case R.id.psam_tv_4:
                setPSAMParams(4);
                break;
            case R.id.start_bt:
                startTestPSAM();

                break;
            case R.id.stop_bt:
                stopTestPSAM();

                break;
            default:
                break;
        }
    }

    private void setPSAMParams(int slotIndex) {
        PSAMSetParamsDialog setParamsDialog = new PSAMSetParamsDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("slotIndex", slotIndex);
        setParamsDialog.setArguments(bundle);
        setParamsDialog.setListener(this);
        setParamsDialog.show(getSupportFragmentManager(), "PSAM1");
    }

    private void startTestPSAM() {
        //如果正在检测则返回
        if (testState) {
            return;
        }
        cycleCount = Integer.parseInt(testCountET.getText().toString());
        flag = true;
        new TestAsyncTask().execute();
    }

    private void stopTestPSAM() {
        flag = false;
    }

    private class TestAsyncTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            int tempCount = cycleCount;//正在循环的次数
            while (flag) {
                //卡槽1
                if (!PSAMUtil.resetPSAM((byte) 1, firstSlotRate, firstSlotVoltage, resetLen, resetReceBuff, firstSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 1 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 1 reset failed!");
                    flag = false;
                }

                //卡槽2
                if (!PSAMUtil.resetPSAM((byte) 2, secondSlotRate, secondSlotVoltage, resetLen, resetReceBuff, secondSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 2 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 2 reset failed!");
                    flag = false;
                }

                //卡槽3
                if (!PSAMUtil.resetPSAM((byte) 3, thirdSlotRate, thirdSlotVoltage, resetLen, resetReceBuff, thirdSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 3 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 3 reset failed!");
                    flag = false;
                }

                //卡槽4
                if (!PSAMUtil.resetPSAM((byte) 4, fourthSlotRate, fourthSlotVoltage, resetLen, resetReceBuff, fourthSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 4 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 4 reset failed!");
                    flag = false;
                }

                //发送apdu指令
                for (int i = 1; i < 5; i++) {
                    if (!PSAMUtil.sendAPDU((byte) 1, apduSendBufByte, (short) apduSendBufByte.length, apduReceBuff, Revlen, SW)) {
                        Log.e(TAG, "doInBackground: " + Converter.BytesToHexString(apduReceBuff, (int) Revlen[0]));
                    } else {
                        Log.d(TAG, "doInBackground: " + Converter.BytesToHexString(apduReceBuff, (int) Revlen[0]));
                    }
                }

                if (cycleCount != 0) {
                    //控制循环
                    tempCount--;
                    if (-1 == tempCount) {
                        flag = false;
                    }
                }

                publishProgress(Converter.BytesToHexString(apduReceBuff, (int) Revlen[0]));
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            showDataTV.setText(values[0]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PSAMUtil.closePSAMModule();
    }

    @Override
    public void OnSureClick(int slotIndex, List<String> params) {
        switch (slotIndex) {
            case SLOT_FIRST:
                firstSlotRate = Integer.parseInt(params.get(0));
                firstSlotVoltage = (byte) Integer.parseInt(params.get(1));
                firstSlotMode = (byte) Integer.parseInt(params.get(2));

                break;
            case SLOT_SECOND:
                secondSlotRate = Integer.parseInt(params.get(0));
                secondSlotVoltage = (byte) Integer.parseInt(params.get(1));
                secondSlotMode = (byte) Integer.parseInt(params.get(2));

                break;
            case SLOT_THIRD:
                thirdSlotRate = Integer.parseInt(params.get(0));
                thirdSlotVoltage = (byte) Integer.parseInt(params.get(1));
                thirdSlotMode = (byte) Integer.parseInt(params.get(2));

                break;
            case SLOT_FOURTH:
                fourthSlotRate = Integer.parseInt(params.get(0));
                fourthSlotVoltage = (byte) Integer.parseInt(params.get(1));
                fourthSlotMode = (byte) Integer.parseInt(params.get(2));

                break;
            default:
                break;
        }
    }

    @Override
    public void OnCancelClick() {

    }
}
