package com.example.psamrftest.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.psamrftest.R;
import com.example.psamrftest.fragment.PSAMSetParamsDialog;
import com.example.psamrftest.interfaces.SetPSAMParamsInterface;
import com.example.psamrftest.util.Converter;
import com.example.psamrftest.util.PSAMUtil;
import com.example.psamrftest.util.ToastUtil;

import java.util.List;

/**
 * Created by moubiao on 2016/5/13.
 * <p/>
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
    private ProgressBar mProgressBar;

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

    private TestAsyncTask mTestAsyncTask;
    private boolean testState = false;//判断是否正在测试
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
        mProgressBar = (ProgressBar) findViewById(R.id.test_progress);

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
        testState = true;
        mTestAsyncTask = new TestAsyncTask();
        mTestAsyncTask.execute();
    }

    private void stopTestPSAM() {
        if (testState) {
            flag = false;
            testState = false;
            mProgressBar.setVisibility(View.GONE);
            showDataTV.setVisibility(View.VISIBLE);
            showDataTV.setText("测试中止");
        }
    }

    private void enableOrDisableButton(boolean enable) {
        psamFirstTV.setClickable(enable);
        psamSecondTV.setClickable(enable);
        psamThirdTV.setClickable(enable);
        psamFourthTV.setClickable(enable);

        testCountET.setFocusable(enable);
        testCountET.setFocusableInTouchMode(enable);

        startBT.setClickable(enable);
        stopBT.setClickable(!enable);
    }

    private class TestAsyncTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            enableOrDisableButton(false);
            showDataTV.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            int tempCount = cycleCount;//正在循环的次数
            long startTime = System.currentTimeMillis();
            long spendTime = 0L;
            while (flag) {
                //卡槽1
                long slotFirstStart = System.currentTimeMillis();
                if (!PSAMUtil.resetPSAM((byte) 1, firstSlotRate, firstSlotVoltage, resetLen, resetReceBuff, firstSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 1 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 1 reset failed!");
                    publishProgress("slot 1 reset failed!", String.valueOf(false));
                    flag = false;
                    testState = false;
                    return false;
                }

                long slotSecondStart = System.currentTimeMillis();
                spendTime += slotSecondStart - slotFirstStart;
                Log.d(TAG, "doInBackground: ----------slot first reset time = " + (slotSecondStart - slotFirstStart));

                //卡槽2

                if (!PSAMUtil.resetPSAM((byte) 2, secondSlotRate, secondSlotVoltage, resetLen, resetReceBuff, secondSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 2 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 2 reset failed!");
                    publishProgress("slot 2 reset failed!", String.valueOf(false));
                    flag = false;
                    testState = false;
                    return false;
                }

                long slotThirdStart = System.currentTimeMillis();
                spendTime += slotThirdStart - slotSecondStart;
                Log.d(TAG, "doInBackground: ----------slot second reset time = " + (slotThirdStart - slotSecondStart));

                //卡槽3
                if (!PSAMUtil.resetPSAM((byte) 3, thirdSlotRate, thirdSlotVoltage, resetLen, resetReceBuff, thirdSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 3 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 3 reset failed!");
                    publishProgress("slot 3 reset failed!", String.valueOf(false));
                    flag = false;
                    testState = false;
                    return false;
                }

                long slotFourthStart = System.currentTimeMillis();
                spendTime += slotFourthStart - slotThirdStart;
                Log.d(TAG, "doInBackground: ----------slot third reset time = " + (slotFourthStart - slotThirdStart));

                //卡槽4
                if (!PSAMUtil.resetPSAM((byte) 4, fourthSlotRate, fourthSlotVoltage, resetLen, resetReceBuff, fourthSlotMode)) {
                    failedInfo.delete(0, failedInfo.length());
                    failedInfo.append("slot 4 reset info = ");
                    failedInfo.append(Converter.BytesToHexString(resetReceBuff, (int) resetLen[0]));
                    Log.e(TAG, "doInBackground: slot 4 reset failed!");
                    publishProgress("slot 4 reset failed!", String.valueOf(false));
                    flag = false;
                    testState = false;
                    return false;
                }

                spendTime += System.currentTimeMillis() - slotFourthStart;
                Log.d(TAG, "doInBackground: ----------slot fourth reset time = " + (System.currentTimeMillis() - slotFourthStart));

                //发送apdu指令
                for (int i = 1; i < 5; i++) {
                    long startAPDU = System.currentTimeMillis();
                    if (!PSAMUtil.sendAPDU((byte) i, apduSendBufByte, (short) apduSendBufByte.length, apduReceBuff, Revlen, SW)) {
                        Log.e(TAG, "doInBackground: " + Converter.BytesToHexString(apduReceBuff, (int) Revlen[0]));
                        String error = "slot " + i + " send apdu failed!";
                        failedInfo.delete(0, failedInfo.length());
                        failedInfo.append(error);
                        publishProgress(failedInfo.toString(), String.valueOf(false));
                        flag = false;
                        testState = false;
                        return false;
                    }
                    spendTime += System.currentTimeMillis() - startAPDU;
                    Log.d(TAG, "doInBackground: ----------send apdu time = " + (System.currentTimeMillis() - startAPDU));
                }

                if (cycleCount != 0) {
                    //控制循环
                    tempCount--;
                    if (0 == tempCount) {
                        publishProgress("测试通过", String.valueOf(true));
                        flag = false;
                        testState = false;
                        return true;
                    }
                }
            }

            Log.d(TAG, "doInBackground: only spend time = " + spendTime);
            Log.d(TAG, "doInBackground: spend time = " + (System.currentTimeMillis() - startTime));

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (!Boolean.parseBoolean(values[1])) {
                mProgressBar.setVisibility(View.GONE);
                showDataTV.setVisibility(View.VISIBLE);
                showDataTV.setText(values[0]);
            } else {
                mProgressBar.setVisibility(View.GONE);
                showDataTV.setVisibility(View.VISIBLE);
                showDataTV.setText(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            enableOrDisableButton(true);
        }
    }

    @Override
    protected void onDestroy() {
        PSAMUtil.closePSAMModule();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mTestAsyncTask != null && mTestAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            ToastUtil.getInstance().showToast(getApplicationContext(), "请先停止测试");
            return;
        }
        super.onBackPressed();
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
