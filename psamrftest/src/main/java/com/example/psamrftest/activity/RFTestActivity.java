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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.psamrftest.R;
import com.example.psamrftest.util.Converter;
import com.example.psamrftest.util.RadiofrequencyUtil;
import com.example.psamrftest.util.ToastUtil;

import java.util.Arrays;

/**
 * Created by moubiao on 2016/5/13.
 * <p/>
 * RF测试界面
 */
public class RFTestActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private String TAG = "moubiao";

    private RadioGroup mRadioGroup;
    private RadioButton innerRB, outRB;
    private Button RFStartTestBT, RFStopTestBT;
    private TextView cardTypeTV, cardSerialTV, testResultTV;
    private EditText cycleCountET;
    private ProgressBar testProgress;

    private int cycleCount = 10;
    private boolean abort = false;//是否中断测试
    private RFTestTask mTestTask;

    private int aerialIndex = 1;
    private byte[] uidlen = new byte[1];
    private byte[] pUID = new byte[256];
    private short ATQA;
    private byte SAK;
    private byte[] RevBuff = new byte[400];
    private byte[] TmpBuff = new byte[1000];
    private short[] OutLen = new short[1];
    private byte[] Serial = new byte[4];
    private int errorCode = 0;//0表示成功 -1:中断测试


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
        innerRB = (RadioButton) findViewById(R.id.inner_aerial_radio_bt);
        outRB = (RadioButton) findViewById(R.id.out_aerial_radio_bt);

        cycleCountET = (EditText) findViewById(R.id.cycle_count_tv);
        cardTypeTV = (TextView) findViewById(R.id.card_type_tv);
        cardSerialTV = (TextView) findViewById(R.id.card_serial_number_tv);
        testResultTV = (TextView) findViewById(R.id.m1_card_test_result);

        testProgress = (ProgressBar) findViewById(R.id.m1_card_test_progress);

        RFStartTestBT = (Button) findViewById(R.id.start_test_bt);
        RFStopTestBT = (Button) findViewById(R.id.stop_test_bt);
    }

    private void setListener() {
        mRadioGroup.setOnCheckedChangeListener(this);
        RFStartTestBT.setOnClickListener(this);
        RFStopTestBT.setOnClickListener(this);
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
            case R.id.start_test_bt:
                mTestTask = new RFTestTask();
                mTestTask.execute();
                break;
            case R.id.stop_test_bt:
                abort = true;
                break;
            default:
                break;
        }
    }

    private void enableOrDisableButton(boolean enable) {
        innerRB.setClickable(enable);
        outRB.setClickable(enable);
        cycleCountET.setFocusable(enable);
        cycleCountET.setFocusableInTouchMode(enable);
        RFStartTestBT.setClickable(enable);
    }

    private class RFTestTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showOrHideProgress(true);
            enableOrDisableButton(false);
            cardTypeTV.setText("");
            cardSerialTV.setText("");
            abort = false;
            cycleCount = Integer.parseInt(cycleCountET.getText().toString());
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

            //获取序列号
            boolean searchCard = true;
            while (searchCard) {
                if (abort) {
                    return false;
                }

                if (!RadiofrequencyUtil.getSerialNumber(0, uidlen, pUID)) {
                    Log.e(TAG, "doInBackground: searching card....");
                } else {
                    searchCard = false;
                    Log.e(TAG, "doInBackground: get serial number success!");
                }
            }

            if (uidlen[0] < 4) {/*yzq: 这里有个问题:深圳通卡慢慢靠近会出现uidlen=2错误 */
                publishProgress("serial number is error", "", String.valueOf(true));
                return false;
            }

            ATQA = (short) (pUID[uidlen[0] - 1] * 256 + pUID[uidlen[0] - 2]);
            SAK = pUID[uidlen[0] - 3];

            ///////////////////////根据不同的卡做不同的操作////////////////////////////////////
            if (((SAK & 0x20) != 0) && (ATQA != 0x0344) && (ATQA != 0x0000)) {//A卡
                publishProgress(getString(R.string.card_type_a), Converter.BytesToHexString(pUID, uidlen[0]), String.valueOf(false));
                if (!RadiofrequencyUtil.resetCpuCard(RevBuff)) {
                    publishProgress("cpu card reset failed!", "", String.valueOf(true));
                    Log.e(TAG, "doInBackground: cpu card reset failed!");
                    return false;
                }

                byte[] TmpData = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x04};
                System.arraycopy(TmpData, 0, TmpBuff, 0, TmpData.length);

                int tempCycleCount = cycleCount;
                while (true) {
                    if (abort) {
                        return false;
                    }
                    if (!RadiofrequencyUtil.sendApdu(TmpBuff, 5, RevBuff, OutLen)) {
                        publishProgress("send apdu failed!", "", String.valueOf(true));
                        Log.e(TAG, "doInBackground: send apdu failed!");
                        return false;
                    } else {
                        Log.d(TAG, "doInBackground: apdu data = " + Converter.BytesToHexString(RevBuff, OutLen[0]));
                    }

                    if (cycleCount != 0) {
                        //控制循环
                        tempCycleCount--;
                        if (0 == tempCycleCount) {
                            publishProgress("test cpu card success!", "", String.valueOf(true));
                            return true;
                        }
                    }
                }

            } else if (ATQA == 0x0004) {//M卡，1K
                publishProgress(getString(R.string.card_type_m_1k), Converter.BytesToHexString(pUID, uidlen[0]), String.valueOf(false));
                System.arraycopy(pUID, 0, Serial, 0, 4);

                if (!testM1Card(Serial)) {
                    publishProgress("M1 card 1K test failed!", "", String.valueOf(true));
                    Log.e(TAG, "doInBackground: M1 card 1K test failed!");
                }
            } else if (ATQA == 0x0002) {//M卡，4K
                publishProgress(getString(R.string.card_type_m_4k), Converter.BytesToHexString(pUID, uidlen[0]), Converter.BytesToHexString(pUID, uidlen[0]), String.valueOf(false));

                System.arraycopy(pUID, 0, Serial, 0, 4);
                int tempCycleCount = cycleCount;
                while (true) {
                    if (abort) {
                        return false;
                    }

                    if (!testM1Card(Serial)) {
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
                        } else if (-1 == errorCode) {
                            publishProgress(getString(R.string.abort_test), "", String.valueOf(true));
                        }

                        return false;
                    }

                    if (cycleCount != 0) {
                        //控制循环
                        tempCycleCount--;
                        if (0 == tempCycleCount) {
                            publishProgress("test M1 card success!", "", String.valueOf(true));
                            return true;
                        }
                    }
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

            if (abort) {
                testResultTV.setText(getString(R.string.abort_test));
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showOrHideProgress(false);
            if (abort) {
                testResultTV.setText(getString(R.string.abort_test));
            }
            enableOrDisableButton(true);
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
     * @param SNR 序列号
     * @return
     */
    private boolean testM1Card(byte[] SNR) {
        int i;
        byte[] TmpBuff = new byte[16];
        byte[] RevBuff = new byte[16];

        Arrays.fill(TmpBuff, (byte) 0xFF);

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.authenticateM1Card((byte) 0x0A, (byte) 4, TmpBuff, SNR)) {
            errorCode = 1;
            return false;
        }

        for (i = 0; i < 16; i++) {
            TmpBuff[i] = (byte) i;
        }

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.writeM1Card((byte) 16, TmpBuff)) {
            errorCode = 2;
            return false;
        }

        Arrays.fill(RevBuff, (byte) 0);

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 16, RevBuff)) {
            errorCode = 3;
            return false;
        }

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

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.writeValueM1Card((byte) 17, TmpBuff)) {
            errorCode = 4;
            return false;
        }
        if (abort) {
            errorCode = -1;
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

        if (abort) {
            errorCode = -1;
            return false;
        }
        TmpBuff[0] = 0x04;
        TmpBuff[1] = 0;
        TmpBuff[2] = 0;
        TmpBuff[3] = 0;
        if (!RadiofrequencyUtil.incWriteValueM1Card((byte) 17, (byte) 17, TmpBuff)) {
            errorCode = 6;
            return false;
        }
        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 17, RevBuff)) {
            errorCode = 3;
            return false;
        }

        if (abort) {
            errorCode = -1;
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
        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 17, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.restoreTransferM1Card((byte) 17, (byte) 16)) {
            errorCode = 8;
            return false;
        }
        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 16, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        if (abort) {
            errorCode = -1;
            return false;
        }
        if (!RadiofrequencyUtil.readM1Card((byte) 17, TmpBuff)) {
            errorCode = 3;
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mTestTask != null && AsyncTask.Status.RUNNING == mTestTask.getStatus()) {
            ToastUtil.getInstance().showToast(getApplicationContext(), "请先停止测试");
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!RadiofrequencyUtil.closeRFModel()) {
            ToastUtil.getInstance().showToast(getApplicationContext(), "close RF model failed!");
        }
    }
}
