package com.example.psamrftest.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.psamrftest.R;
import com.example.psamrftest.util.Converter;
import com.example.psamrftest.util.RadiofrequencyUtil;
import com.example.psamrftest.util.ToastUtil;
import com.xiongdi.EmpPad;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by moubiao on 2016/5/13.
 * <p>
 * RF测试界面
 */
public class RFTestActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private String TAG = "moubiao";

    private RadioGroup mRadioGroup;
    private Button RFTestBT;
    private TextView cardTypeTV, cardSerialTV, testResultTV;
    private ProgressBar testProgress;

    private int aerialIndex = 1;
    private byte[] uidlen = new byte[1];
    private byte[] pUID = new byte[256];
    private short ATQA;
    private byte SAK;
    private byte[] RevBuff = new byte[400];
    private byte[] TmpBuff = new byte[1000];
    private short[] OutLen = new short[1];
    private byte[] Serial = new byte[4];
    private int errorCode = 0;//0表示成功


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rf_test_layout);

        initData();
        initView();
        setListener();
    }

    private void initData() {
        if (!RadiofrequencyUtil.openRFModel()) {
            ToastUtil.getInstance().showToast(getApplicationContext(), "RF model open failed!");
        }
    }

    private void initView() {
        mRadioGroup = (RadioGroup) findViewById(R.id.aerial_radio_group);

        cardTypeTV = (TextView) findViewById(R.id.card_type_tv);
        cardSerialTV = (TextView) findViewById(R.id.card_serial_number_tv);
        testResultTV = (TextView) findViewById(R.id.m1_card_test_result);

        testProgress = (ProgressBar) findViewById(R.id.m1_card_test_progress);

        RFTestBT = (Button) findViewById(R.id.RF_test_bt);
    }

    private void setListener() {
        mRadioGroup.setOnCheckedChangeListener(this);
        RFTestBT.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.inner_aerial_radio_bt:
                aerialIndex = 1;
                break;
            case R.id.out_aerial_radio_bt:
                aerialIndex = 2;
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.RF_test_bt:
                new RFTestTask().execute();
                break;
            default:
                break;
        }
    }

    private class RFTestTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showOrHideProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (!RadiofrequencyUtil.chooseAerial(aerialIndex)) {
                Log.e(TAG, "doInBackground: choose aerial failed!");
                publishProgress("choose aerial failed!", "", String.valueOf(true));
            }
            if (!RadiofrequencyUtil.initRFModel(aerialIndex)) {
                Log.e(TAG, "doInBackground: init RF model failed!");
                publishProgress("init RF model failed!", "", String.valueOf(true));
            }

            boolean search = true;
            while (search) {
                if (!RadiofrequencyUtil.getSerialNumber(0, uidlen, pUID)) {
                    Log.e(TAG, "doInBackground: get serial number failed!");
                } else {
                    search = false;
                    Log.e(TAG, "doInBackground: get serial number success!");
                }
            }

            if (uidlen[0] < 4) {/*yzq: 这里有个问题:深圳通卡慢慢靠近会出现uidlen=2错误 */
                publishProgress("serial number is error", "", String.valueOf(true));
                return false;
            }

            ATQA = (short) (pUID[uidlen[0] - 1] * 256 + pUID[uidlen[0] - 2]);
            SAK = pUID[uidlen[0] - 3];

            if (((SAK & 0x20) != 0) && (ATQA != 0x0344) && (ATQA != 0x0000)) {//A卡
                publishProgress(getString(R.string.card_type_a), Converter.BytesToHexString(pUID, pUID.length), String.valueOf(false));
                if (!RadiofrequencyUtil.resetCpuCard(RevBuff)) {
                    publishProgress("cpu card reset failed!", "", String.valueOf(true));
                    Log.e(TAG, "doInBackground: cpu card reset failed!");
                }

                byte[] TmpData = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x04};
                System.arraycopy(TmpData, 0, TmpBuff, 0, TmpData.length);

                for (int i = 0; i < 10; i++) {
                    if (!RadiofrequencyUtil.sendApdu(TmpBuff, 5, RevBuff, OutLen)) {
                        publishProgress("send apdu failed!", "", String.valueOf(true));
                        Log.e(TAG, "doInBackground: send apdu failed!");
                    } else {
                        Log.d(TAG, "doInBackground: apdu data = " + Converter.BytesToHexString(RevBuff, RevBuff.length));
                    }
                    if (9 == i) {
                        publishProgress("test cpu card success!", "", String.valueOf(true));
                    }
                }

            } else if (ATQA == 0x0004) {//M卡，1K
                publishProgress(getString(R.string.card_type_m_1k), Converter.BytesToHexString(pUID, pUID.length), String.valueOf(false));
                System.arraycopy(pUID, 0, Serial, 0, 4);

                if (!testM1Card(Serial)) {
                    publishProgress("M1 card 1K test failed!", "", String.valueOf(true));
                    Log.e(TAG, "doInBackground: M1 card 1K test failed!");
                }
            } else if (ATQA == 0x0002) {//M卡，4K
                publishProgress(getString(R.string.card_type_m_4k), Converter.BytesToHexString(pUID, pUID.length), Converter.BytesToHexString(pUID, pUID.length), String.valueOf(false));

                System.arraycopy(pUID, 0, Serial, 0, 4);
                if (!testM1Card(Serial)) {
                    Log.e(TAG, "doInBackground: M1 card 4K test failed!");
                    if (1 == errorCode) {
                        publishProgress("authenticate M1 Card failed!", "", String.valueOf(true));
                    } else if (2 == errorCode) {
                        publishProgress("write M1 card failed!", "", String.valueOf(true));
                    } else if (3 == errorCode) {
                        publishProgress("read M1 card failed!", "", String.valueOf(true));
                    } else if (4 == errorCode) {
                        publishProgress("write value M1 card failed!", "", String.valueOf(true));
                    } else if (5 == errorCode) {
                        publishProgress("read value M1 card failed!", "", String.valueOf(true));
                    } else if (6 == errorCode) {
                        publishProgress("increase write M1 card failed!", "", String.valueOf(true));
                    } else if (7 == errorCode) {
                        publishProgress("decrease write M1 card failed!", "", String.valueOf(true));
                    } else if (8 == errorCode) {
                        publishProgress("restore write M1 card failed!", "", String.valueOf(true));
                    }
                } else {
                    publishProgress("test M1 card success!", "", String.valueOf(true));
                }
            } else if (ATQA == 0x0044) {//未定义

            }

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (Boolean.parseBoolean(values[2])) {
                showOrHideProgress(false);
                testResultTV.setText(values[0]);
            } else {
                cardTypeTV.setText(values[0]);
                cardSerialTV.setText(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showOrHideProgress(false);
        }
    }

    /**
     * 显示或者隐藏进度条
     */
    private void showOrHideProgress(boolean show) {
        if (show) {
            testResultTV.setVisibility(View.GONE);
            testProgress.setVisibility(View.VISIBLE);
        } else {
            testResultTV.setVisibility(View.VISIBLE);
            testProgress.setVisibility(View.GONE);
        }
    }

    /**
     * 测试M1卡
     *
     * @param SNR
     * @return
     */
    private boolean testM1Card(byte[] SNR) {
        int i;
        byte[] TmpBuff = new byte[16];
        byte[] RevBuff = new byte[16];

        for (i = 0; i < TmpBuff.length; i++) {
            TmpBuff[i] = (byte) 0xFF;
        }
        Arrays.fill(TmpBuff, (byte) 0xFF);

        if (!RadiofrequencyUtil.authenticateM1Card((byte) 0x0A, (byte) 4, TmpBuff, SNR)) {
            errorCode = 1;
            return false;
        }

        for (i = 0; i < 16; i++) {
            TmpBuff[i] = (byte) i;
        }

        if (!RadiofrequencyUtil.writeM1Card((byte) 16, TmpBuff)) {
            errorCode = 2;
            return false;
        }

//        for (i = 0; i < RevBuff.length; i++) {
//            RevBuff[i] = (byte) 0;
//        }
        Arrays.fill(RevBuff, (byte) 0);

        if (!RadiofrequencyUtil.readM1Card((byte) 16, RevBuff)) {
            errorCode = 3;
            return false;
        }
        Log.d(TAG, "BLOCK16:" + Converter.BytesToHexString(RevBuff, 16));//BCD2ASC(RevBuff,16)
        for (i = 0; i < RevBuff.length; i++) {
            if (TmpBuff[i] != RevBuff[i]) {
                Log.e(TAG, "RFID Write or Read ..........failed!");
                errorCode = 3;
                return false;
            }
        }

        TmpBuff[0] = 0x64;
        TmpBuff[1] = 0;
        TmpBuff[2] = 0;
        TmpBuff[3] = 0;

        if (!RadiofrequencyUtil.writeValueM1Card((byte) 17, TmpBuff)) {
            errorCode = 4;
            return false;
        }
        if (!RadiofrequencyUtil.readValueM1Card((byte) 17, RevBuff)) {
            errorCode = 5;
            return false;
        }

        for (i = 0; i < 4; i++) {
            if (TmpBuff[i] != RevBuff[i]) {
                Log.e(TAG, "Write Value...........failed");
                errorCode = 4;
                return false;
            }
        }

        TmpBuff[0] = 0x04;
        TmpBuff[1] = 0;
        TmpBuff[2] = 0;
        TmpBuff[3] = 0;
        if (!RadiofrequencyUtil.incWriteValueM1Card((byte) 17, (byte) 17, TmpBuff)) {
            errorCode = 6;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 17, RevBuff)) {
            errorCode = 3;
            return false;
        }

        TmpBuff[0] = 0x04;
        TmpBuff[1] = 0;
        TmpBuff[2] = 0;
        TmpBuff[3] = 0;
        if (!RadiofrequencyUtil.decWriteValueM1Card((byte) 17, (byte) 17, TmpBuff)) {
            errorCode = 7;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 17, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        if (!RadiofrequencyUtil.restoreTransferM1Card((byte) 17, (byte) 16)) {
            errorCode = 8;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 16, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        if (!RadiofrequencyUtil.readM1Card((byte) 17, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!RadiofrequencyUtil.closeRFModel()) {
            ToastUtil.getInstance().showToast(getApplicationContext(), "close RF model failed!");
        }
    }
}
