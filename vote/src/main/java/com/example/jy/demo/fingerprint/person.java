package com.example.jy.demo.fingerprint;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ToneGenerator;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencv.LibImgFun;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class person extends Activity {
    String TAG = "moubiao";

    private Button mbuttonback, mbuttonquery, mbuttonread, mbuttonfp;
    private TextView mTextView_result;
    private ImageButton mbutton_closeresult;
    private LinearLayout mRusultLayout;
    private EditText mEditText_gender, mEditText_birthday, mEditText_occ, mEditText_name, mEditText_vin, mEditText_code, mEditText_surname;
    private ImageView imagepic, imagefinger;
    ProgressDialog MyDialog;

    private final int comparedata_time = 3000;

    private Handler mProgresshandler;
    Message myMessage = new Message();// 消息对象

    private SharedPreferences preferences;
    private VoteVin_DBHelper mVoteDB;
    private Cursor mCursor;
    private String VIN_TABLE_NAME;
    private Boolean mthreadRun = true;
    private String configPucode;
    private String dataPuCode, dataVin, dataVtype, dataStatus;

    private static audioPlay ap = new audioPlay();
    public byte[] bVinCode = new byte[10];
    public boolean bRunning = false;
    public boolean bReadOk = false;
    public final static int GET_IMAGE_VIA_CAMERA = 1;

    public static final String FILE_NAME = "card.bin";
    public static final String LOG_FILE_NAME = "log.txt";
    public static String strvin;

    public AssetManager am;

    //nfc
    private PendingIntent mPendingIntent;
    private IntentFilter ndef;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private NfcAdapter mAdapter;

    public boolean pucode_is_ok = false;
    public boolean fp_is_ok = false;
    public boolean is_fp_back = false;

    private int fp_matchvalue = 16;

    //12.19
    public boolean LockVoteStatus = false;

    //wt
    private String[] type = {"一代身份证", "二代身份证正面", "二代身份证证背面", "临时身份证",
            "驾照", "行驶证", "军官证", "士兵证", "港澳通行证", "大陆通行证", "台湾通行证", "签证", "护照",
            "内地通行证正面", "内地通行证背面", "户口本", "居住证", "香港永久性居民身份证", "边防证A", "边防证B", "澳门身份证", "领取凭证", "律师证A", "律师证B", "中华人民共和国道路运输证IC卡"};
    private int nMainID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        //nfc
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // 做一个IntentFilter过滤你想要的action 这里过滤的是ndef
        ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        // 生成intentFilter
        mFilters = new IntentFilter[]{ndef,};

        mTechLists = new String[][]{new String[]{NfcF.class.getName()},
                new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcV.class.getName()}};
        //nfc
        mbuttonread = (Button) findViewById(R.id.button_rc);
        mbuttonfp = (Button) findViewById(R.id.button_fp);
        mbuttonback = (Button) findViewById(R.id.button4_back);
        mEditText_surname = (EditText) findViewById(R.id.editText_surname);
        mEditText_name = (EditText) findViewById(R.id.editText_name);
        mEditText_vin = (EditText) findViewById(R.id.editText_vin);
        mEditText_code = (EditText) findViewById(R.id.editText_code);
        mEditText_gender = (EditText) findViewById(R.id.editText_gender);
        imagepic = (ImageView) findViewById(R.id.imageView_avater);
        imagefinger = (ImageView) findViewById(R.id.imageView_finger);
        // result layout
        mTextView_result = (TextView) findViewById(R.id.textView_result);
        mRusultLayout = (LinearLayout) findViewById(R.id.linearLayout_result);
        mbutton_closeresult = (ImageButton) findViewById(R.id.button_result_close);
        mbutton_closeresult.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mRusultLayout.setVisibility(View.GONE);
            }
        });

        am = getAssets();
        preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp), MODE_PRIVATE);

        configPucode = preferences.getString("PU_CODE", "34-16-10-003");
        dataVtype = preferences.getString("CURRENT_ELECTION_TYPE", "President");
        fp_matchvalue = Integer.parseInt(preferences.getString("CURRENT_FINGERPRINT_NUM", "16"));

        Rifhandler = new Handler();
        mVoteDB = new VoteVin_DBHelper(this);
        mCursor = mVoteDB.Query_Vin_table();
        VIN_TABLE_NAME = mVoteDB.VIN_TABLE_NAME;

        mProgresshandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                switch (msg.what) {
                    case 0:
                        if (MyDialog != null) {
                            MyDialog.dismiss();
                            mProgresshandler.removeMessages(1);
                            mProgresshandler.removeMessages(0);
                            MyDialog.cancel();
                            MyDialog = null;
                            mthreadRun = false;
                        }
                        break;

                    case 1:
                        try {
                            if (MyDialog != null) {
                                MyDialog.dismiss();
                                mProgresshandler.removeMessages(1);
                                mProgresshandler.removeMessages(0);
                                MyDialog.cancel();
                                MyDialog = null;
                            }

                            MyDialog = ProgressDialog.show(person.this, "", getResources().getString(R.string.handle_text_wait), true);
                            MyDialog.setCanceledOnTouchOutside(false);
                            MyDialog.setCancelable(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 2:

                        if (MyDialog != null) {
                            MyDialog.dismiss();
                            mProgresshandler.removeMessages(1);
                            mProgresshandler.removeMessages(0);
                            MyDialog.cancel();
                            MyDialog = null;
                            mthreadRun = false;
                            if (is_fp_back == true) {
                                try {
                                    String filePath = getFilesDir().getParent().toString() + "/" + getResources().getString(R.string.app_name) + "2" + ".bmp";

                                    File mfile2 = new File(filePath);

                                    FileInputStream fis;
                                    fis = new FileInputStream(mfile2);
                                    Bitmap bmp = BitmapFactory.decodeStream(fis);

                                    Matrix m = new Matrix();
                                    m.postRotate(90);

                                    Bitmap bm = Bitmap.createBitmap(bmp, 50, 50, bmp.getWidth() - 100, bmp.getHeight() - 100, m, true);

                                    Drawable drawable = new BitmapDrawable(bm);
//                                    imagefinger.setBackground(drawable);
                                    imagefinger.setBackgroundDrawable(drawable);
                                    if (fp_is_ok) {
                                        imagefinger.setImageResource(R.drawable.fp_ok);
                                    } else {
                                        imagefinger.setImageResource(R.drawable.fp_fail);
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                is_fp_back = false;
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        mbuttonfp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (bReadOk) {
                    if (pucode_is_ok) {
                        if (!LockVoteStatus) {
                            //if(!judgeVin_status()){	// zanshi
                            if (true) {
                                if (ap != null) {

                                    try {
                                        ap.PlayAsset(PLAY_SOUND_PUT_FINGER, am);    // 1
                                    } catch (IllegalArgumentException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (SecurityException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IllegalStateException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }

                                Intent it = new Intent(person.this, CameraOpen_Automatic.class);
                                startActivityForResult(it, 1);
                            } else {
                                mRusultLayout.setVisibility(View.VISIBLE);
                                mRusultLayout.setBackgroundColor((0xAACB1D04));
                                mTextView_result
                                        .setText(R.string.personscreen_text_result_fail2);
                            }

                        } else {

                            mRusultLayout.setVisibility(View.VISIBLE);
                            mRusultLayout.setBackgroundColor((0xAACB1D04));
                            mTextView_result
                                    .setText(R.string.personscreen_text_lockvote_fail);

                        }
                    } else {

                        mRusultLayout.setVisibility(View.VISIBLE);
                        mRusultLayout.setBackgroundColor((0xAACB1D04));
                        mTextView_result
                                .setText(R.string.personscreen_text_pucode_fail);
                    }

                } else {

                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result
                            .setText(R.string.personscreen_text_readcard);
                }

            }
        });

        mbuttonread.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                bReadOk = false;

                if (MyDialog != null) {

                    MyDialog.dismiss();
                    MyDialog.cancel();
                    MyDialog = null;
                }

                mEditText_surname.setText("");
                mEditText_gender.setText("");
                //mEditText_birthday.setText("");
                //mEditText_occ.setText("");
                mEditText_name.setText("");
                mEditText_vin.setText("");
                mEditText_code.setText("");
                mRusultLayout.setVisibility(View.VISIBLE);
                imagepic.setImageDrawable(getResources().getDrawable(R.drawable.logo));
//                imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
                imagefinger.setBackgroundResource(R.drawable.vote_example);
                imagefinger.setImageResource(R.drawable.fp_null);
                mRusultLayout.setBackgroundColor((0xAA9BC032));
                mTextView_result.setText(R.string.personscreen_text_dialog_readcard);

                try {
                    ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mRFID_open();
            }
        });

        mbuttonback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });


    }

    private void startThread() {
        new Thread(new Runnable() {
            public void run() {
                while (mthreadRun) {
                    Lock lock = new ReentrantLock();
                    mProgresshandler.sendEmptyMessage(1);
                    try {
                        lock.lock();
                        Thread.sleep(comparedata_time);
                        mProgresshandler.sendEmptyMessage(2);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }).start();
    }

    private void setPersoninfo() {

        // 读完卡信息后，先匹配code，
        // String code = "01";
        // if(pucode.equals(code)){
        //
        //
        //
        //
        //
        // }else{
        //
        // mRusultLayout.setVisibility(View.VISIBLE);
        // mRusultLayout.setBackgroundColor((0xAACB1D04));
        // mTextView_result.setText(R.string.personscreen_text_result_fail3);
        // return;
        // }

        // 然后再匹配VIN

        RandomAccessFile file;
        try {

            String mfile1 = "/sdcard";
            String mfile2 = "/data/data/com.example.jy.demo.fingerprint";

            file = new RandomAccessFile(mfile2 + "/pic1.jp2", "r");
            int len = (int) file.length(); // 取得文件长度（字节数）
            Log.v(TAG, "len = " + len);
            byte[] b = new byte[len];
            file.readFully(b);
            file.close();
            Log.v(TAG, "b = " + b);
            CallDecoder cd = new CallDecoder();
            cd.DecodeMj2Data(b, len, mfile2 + "/pic1.bmp");

            FileInputStream fis = new FileInputStream(mfile2 + "/pic1.bmp");
            Bitmap bmp = BitmapFactory.decodeStream(fis);

            Drawable drawable = new BitmapDrawable(bmp);
            imagepic.setImageDrawable(drawable);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mEditText_surname.setText("Adedoyin, Oluwaseun");
        mEditText_name.setText("01-01-1960");
        mEditText_vin.setText("90F5 AFE4 3329 5260 566");
        mEditText_code.setText("34-16-10-003");
        mEditText_gender.setText("Male");
    }


    @Override
    protected void onResume() {
        super.onResume();
        LockVoteStatus = is_LockVoteStatus();
        com.io.io.IoOpen();

    }

    //�ַ�����ת��Ϊ16�����ַ�
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private void reset_content() {
        mEditText_surname.setText("");
        mEditText_gender.setText("");
        mEditText_name.setText("");
        mEditText_vin.setText("");
        mEditText_code.setText("");
        mRusultLayout.setVisibility(View.VISIBLE);
        imagepic.setImageDrawable(getResources().getDrawable(R.drawable.logo));
//        imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
        imagefinger.setBackgroundResource(R.drawable.vote_example);
        imagefinger.setImageResource(R.drawable.fp_null);
        mRusultLayout.setBackgroundColor((0xAA9BC032));

        mTextView_result.setTextColor(0xFF800000);
        mTextView_result.setText(R.string.personscreen_text_dialog_hint);


    }

    private void get_finger_img(Intent data) {
        String sdStatus = Environment.getExternalStorageState();// 获取sd卡路径
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Log.d(TAG, "SD card is not avaiable/writeable right now.");
            return;
        }

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
        Log.v(TAG, "bitmap = " + bitmap);

        String filePath = Environment.getExternalStorageDirectory().toString()
                + "/" + getResources().getString(R.string.app_name) + "/"
                + 111111 + ".bmp";

        Log.v(TAG, "filePath = " + filePath);

        int[] pix = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pix, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        int resut = LibImgFun.mySaveImage(pix, bitmap.getWidth(),
                bitmap.getHeight(), filePath);

        Log.v(TAG, "resut = " + resut);

        FileOutputStream outStream = null;
        File file = new File("/sdcard/myImage/");// 创建照片存放的位置
        if (!file.exists()) {
            file.mkdirs();// 创建文件夹
        }
        String fileName = "/sdcard/myImage/222.bmp";// 创建文件名称
        try {
            outStream = new FileOutputStream(fileName);// 创建路径文件的输出流

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);// 把数据写入文件

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Log.v(TAG, "resultCode == data =  " + data);

            // init thread
            mthreadRun = true;
            // 进度条
            startThread();
            compareFinger();
        }

    }

    // xiong di yugong code
    public static final String KEY_INFO = "key_Info";
    public static final int MSG_TEST = 1;
    public static final int MSG_UPDATE_GEN = 2;
    public static final int MSG_UPDATE_BIRTH = 3;
    public static final int MSG_UPDATE_OCC = 4;
    public static final int MSG_UPDATE_NAME = 5;
    public static final int MSG_UPDATE_VIN = 6;
    public static final int MSG_UPDATE_CODE = 7;
    public static final int MSG_UPDATE_IMAGE = 8;
    public static final int MSG_UPDATE_SURNAME = 9;
    // public static final int MSG_CLOSE_RFID = 9;

    public static final int PLAY_SOUND_PUCODE_ERROR = 1;
    public static final int PLAY_SOUND_PUT_FINGER = 2;
    public static final int PLAY_SOUND_FINGER_SCAN_FAIL = 3;
    public static final int PLAY_SOUND_FINGER_AUTHENTICATION_FAIL = 4;
    public static final int PLAY_SOUND_FINGER_VERIFICATION = 5;

    public static final int HANDLE_VOTER_ACCREDITATION_SUCCESSFUL = 0;
    public static final int HANDLE_FINGER_AUTHENTICATION_FAIL = 1;
    public static final int HANDLE_READCARD_SUCCESSFUL = 2;
    public static final int HANDLE_READCARD_FAIL = 3;
    public static final int HANDLE_READCARD_REPEAT_TEXT = 4;
    private Handler Rifhandler;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String info = bundle.getString(KEY_INFO);
            if (msg.what == MSG_TEST) {
                if (msg.arg1 == HANDLE_VOTER_ACCREDITATION_SUCCESSFUL) {
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result.setText(R.string.personscreen_text_result_success);
                } else if (msg.arg1 == HANDLE_FINGER_AUTHENTICATION_FAIL) {
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result.setText(R.string.personscreen_text_result_fail);
                } else if (msg.arg1 == HANDLE_READCARD_SUCCESSFUL) {// read nfc success
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result.setText(R.string.personscreen_text_readnfc_success);

                    //判断 pu code 不一致 ，语音提示
                    judgePucode();

                    //判断VIN是否已被验证过
                    //12.19
                    android.util.Log.d(TAG, "LockVoteStatus= " + LockVoteStatus);
                    if (!LockVoteStatus) {
                        judgeVin();
                    }
                } else if (msg.arg1 == HANDLE_READCARD_FAIL) {    // read nfc fail
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result.setText(info);
                } else if (msg.arg1 == HANDLE_READCARD_REPEAT_TEXT) {    // read nfc Repeat
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result
                            .setText(R.string.personscreen_text_dialog_hint);
                }

            } else if (msg.what == MSG_UPDATE_GEN) {
                mEditText_gender.setText(info);
            } else if (msg.what == MSG_UPDATE_BIRTH) {
                mEditText_name.setText(info);
            } else if (msg.what == MSG_UPDATE_NAME) {
                mEditText_surname.setText(info);
            } else if (msg.what == MSG_UPDATE_VIN) {
                mEditText_vin.setText(info);
            } else if (msg.what == MSG_UPDATE_CODE) {
                mEditText_code.setText(info);
            } else if (msg.what == MSG_UPDATE_IMAGE) {
                RandomAccessFile file;
                try {
                    String mfile1 = getFilesDir().getParent().toString() + "/";

                    file = new RandomAccessFile(mfile1 + "image.jp2", "r");
                    int len = (int) file.length();
                    Log.e(TAG, "image.jp2 length = " + len);
                    // °üº¬ jp2 ÎÄ¼þÍ· 208 ×Ö½Ú
                    byte[] b = new byte[len];
                    file.readFully(b);
                    file.close();
                    Log.v(TAG, "b = " + b);
                    CallDecoder cd = new CallDecoder();
                    cd.DecodeMj2Data(b, len, mfile1 + "image.bmp");

                    FileInputStream fis = new FileInputStream(mfile1 + "/image.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);

                    Drawable drawable = new BitmapDrawable(bmp);

                    imagepic.setImageDrawable(drawable);//moubiao
                    imagefinger.setBackgroundResource(R.drawable.vote_example);
                    imagefinger.setImageResource(R.drawable.fp_null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private void writeToFile(String fileName, String msg) throws java.io.IOException {
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
        osw.write(msg, 0, msg.length());
        osw.flush();
        osw.close();
        BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(new File(baseDir, fileName), false));
        buf.write(bos.toByteArray());
        buf.close();
    }

    private File File(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    void updateInfo(String info, int arg1) {
        Message msg = new Message();
        msg.arg1 = arg1;
        msg.what = MSG_TEST;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INFO, info);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    void updatePersonInfo(int msgId, String info) {
        Message msg = new Message();
        msg.what = msgId;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INFO, info);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    boolean rfidsucess = false;

    /**
     * 读卡的线程
     */
    private class doTestRfid extends Thread {
        @Override
        public void run() {
            String info;
            if (com.xd.rfid.RFIDInit() != 0) {
                info = "init RFID failed!";
                Log.d(TAG, info);
                updateInfo(info, HANDLE_READCARD_FAIL);

                return;
            }

            if (com.xd.rfid.RFIDTypeSet(0) != 0) {
                Log.d(TAG, "run: RFID type set failed! ");

                return;
            }

            String cardData = readCardData();
            showMessageInfo(cardData);
        }
    }

    /**
     * 读卡
     */
    private String readCardData() {
        byte[] bIdLen = new byte[1];
        byte[] bSNR = new byte[64];
        byte sak;
        short atqa;
        String cardType;
        String cardAllData = null;

        int nRet = com.xd.rfid.RFIDGetSNR(0, bIdLen, bSNR);
        if (nRet != 0) {
            return null;
        }

        mProgresshandler.sendEmptyMessage(1);
        sak = bSNR[bIdLen[0] - 3];
        atqa = (short) (bSNR[bIdLen[0] - 1] * 256 + bSNR[bIdLen[0] - 2]);
        if (((sak & 0x20) == 0x20) || (sak == 0x53)) {
            if (atqa == 0x0344) {
                cardType = ", DesFire card";
            } else {
                cardType = ", CPU card";
            }
        } else if (atqa == 0x0044) {
            cardType = ", UL card";
        } else if ((sak == 0x08) || (sak == 0x18)) {
            if (sak == 0x08) {
                cardType = ", S50 card";
            } else {
                cardType = ", S70 card";
            }
            cardAllData = readM1Card(bSNR, cardType);
        }

        return cardAllData;
    }

    /**
     * 读M1卡
     *
     * @param SNR
     * @param cardType
     * @return
     */
    private String readM1Card(byte[] SNR, String cardType) {
        String M1Para = "SNR=" + com.xd.Converter.printHexLenString(SNR, 7) + cardType;
        byte[] bId = new byte[4];
        bId[0] = SNR[0];
        bId[1] = SNR[1];
        bId[2] = SNR[2];
        bId[3] = SNR[3];
        int nSector = 16;
        int nRet;
        if (M1Para.indexOf("S70 card") > 0) {
            nSector = 40;
        }
        int sectorIndex;
        int blockIndex;
        byte[] bKey = new byte[6];
        byte[] bOutData = new byte[16];
        byte errFlag = 0;
        String updateInfo;
        int nBlock = 4;
        String cardAllData = null;
        StringBuilder sectorData = new StringBuilder();
        long startTime, endTime;

        //验证扇区并读块 S50, 16扇区;  S70, 40扇区
        int cycleIndex = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < nSector; i++) {
            Arrays.fill(bKey, (byte) 0xFF);
            //验证扇区
            if (i > 31) {//前32扇区
                sectorIndex = 32 + (i - 32) * 4;
                nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) sectorIndex, bKey, bId);
            } else {//后8扇区
                if (i == 0) {
                    System.arraycopy(bId, 0, bKey, 0, 4);
                    bKey[4] = 0x20;
                    bKey[5] = 0x12;
                    nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) i, bKey, bId);
                } else {
                    nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) i, bKey, bId);
                }
            }

            if (nRet != 0) {//验证扇区失败
                updateInfo = "Mi authenticate sector(" + i + ") failed, nRet=" + nRet;
                errFlag = 1;
                updateInfo(updateInfo, HANDLE_READCARD_FAIL);

                break;
            }

            //读一个扇区里的所有块
            if (i > 31) {
                nBlock = 16;
            }
            StringBuilder blockData = new StringBuilder();
            for (int j = 0; j < nBlock; j++) {
                blockIndex = (i * nBlock + j);
                if (i > 31) {
                    blockIndex = 32 * 4 + (i - 32) * nBlock + j;
                }

                nRet = com.xd.rfid.MifRead(blockIndex, bOutData);
                if (nRet != 0) {//读块失败
                    updateInfo = "MifRead block(" + blockIndex + ") failed, nRet=" + nRet;
                    errFlag = 2;
                    Log.d(TAG, "readCardData: failed " + updateInfo);
                    updateInfo(updateInfo, HANDLE_READCARD_FAIL);

                    break;
                }
                DecimalFormat df = new DecimalFormat();
                String style = "000";
                df.applyPattern(style);
                String outString = com.xd.Converter.printHexLenString(bOutData, 16);
                blockData.append(outString);
                Log.d(TAG, "block index = " + cycleIndex + " blockData = " + blockData.toString());
                cycleIndex++;
                blockData.append("\n");
            }

            sectorData.append(blockData);
            if (errFlag != 0) {
                break;
            }
            cardAllData = sectorData.toString();
        }

        if (errFlag == 0) {
            endTime = System.currentTimeMillis();
            Log.i(TAG, "read data total Time=" + (endTime - startTime) + "(ms)");
        }

        return cardAllData;
    }

    private void showMessageInfo(String data) {
        if (data == null) {
            rfidsucess = false;
            mProgresshandler.sendEmptyMessage(0);
            Log.d(TAG, "show error");
            return;
        }

        rfidsucess = true;
        try {
            writeToFile(FILE_NAME, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            char[] mac = new char[8];
            char[] mac2 = new char[8];
            char[] vin = new char[32];
            char[] BirthDay = new char[10];
            char[] code = new char[12];
            char[] occ = new char[32];
            char[] gender = new char[16];
            char[] name = new char[32];
            String info = "Read Card Successfully !";

            DataParser.OpenCardFile("/sdcard/card.bin");

            char[] csn = new char[8];
            DataParser.GetCSN(csn);
            DataParser.GetMAC(mac);
            DataParser.GetMAC(mac2);
            int vinL = DataParser.GetVIN(vin);
            vin[vinL] = 0;
            strvin = String.valueOf(vin).toUpperCase();
            DataParser.GetBirthDay(BirthDay);
            DataParser.GetCODE(code);
            int occLen = DataParser.GetOCCUPATION(occ);
            occ[occLen] = 0;
            int genLen = DataParser.GetGENDER(gender);
            gender[genLen] = 0;
            int nameLen = DataParser.GetNAME(name);
            name[nameLen] = 0;

            String jp2Path = getFilesDir().getParent().toString() + "/";
            if (DataParser.GetJP2IMAGE(jp2Path + "image.jp2") != 0) {
                Log.e(TAG, "Get JP2 file error");
            }
            if (DataParser.GetFingerXYT(jp2Path) != 0) {
                Log.e(TAG, "Get FingerXYT file error");
            }
            DataParser.CloseCardFile();

            mProgresshandler.sendEmptyMessage(0);
            updatePersonInfo(MSG_UPDATE_GEN, String.valueOf(gender));
            updatePersonInfo(MSG_UPDATE_BIRTH, String.valueOf(BirthDay));
            updatePersonInfo(MSG_UPDATE_OCC, String.valueOf(occ));
            updatePersonInfo(MSG_UPDATE_NAME, String.valueOf(name));
            updatePersonInfo(MSG_UPDATE_VIN, String.valueOf(vin).toUpperCase());
            updatePersonInfo(MSG_UPDATE_CODE, String.valueOf(code));
            updatePersonInfo(MSG_UPDATE_IMAGE, "image");
            dataPuCode = String.valueOf(code);
            dataVin = String.valueOf(vin).toUpperCase();
            updateInfo(info, HANDLE_READCARD_SUCCESSFUL);

            //验证指纹
            String activityName = getCurrActivity();
            if (activityName.equals("com.example.jy.demo.fingerprint.person")) {
                Intent it = new Intent(person.this, CameraOpen_Automatic.class);
                startActivityForResult(it, 1);
            }
            bReadOk = true;
        } catch (Exception ex) {
            mProgresshandler.sendEmptyMessage(0);
            ex.printStackTrace();
        }
    }

    private String getCurrActivity() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

        return cn.getClassName();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == 251 || keyCode == 252) && (event.getRepeatCount() == 0)) {     //扫描指纹，entry camera    level  up
            if (bReadOk) {
                if (pucode_is_ok) {
                    if (!LockVoteStatus) {
                        if (true) {
                            if (ap != null) {
                                try {
                                    ap.PlayAsset(PLAY_SOUND_PUT_FINGER, am);    // 1
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            mRusultLayout.setVisibility(View.VISIBLE);
                            mRusultLayout.setBackgroundColor((0xAACB1D04));
                            mTextView_result.setText(R.string.personscreen_text_result_fail2);
                        }
                    } else {
                        mRusultLayout.setVisibility(View.VISIBLE);
                        mRusultLayout.setBackgroundColor((0xAACB1D04));
                        mTextView_result.setText(R.string.personscreen_text_lockvote_fail);
                    }
                } else {
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result.setText(R.string.personscreen_text_pucode_fail);
                }

            } else {
                mRusultLayout.setVisibility(View.VISIBLE);
                mRusultLayout.setBackgroundColor((0xAACB1D04));
                mTextView_result.setText(R.string.personscreen_text_readcard);
            }

            return true;
        } else if ((keyCode == 250 || keyCode == 249) && (event.getRepeatCount() == 0)) {//moubiao 从这里开始读卡
            bReadOk = false;
            if (MyDialog != null) {
                MyDialog.dismiss();
                MyDialog.cancel();
                MyDialog = null;
            }
            mEditText_surname.setText("");
            mEditText_gender.setText("");
            mEditText_name.setText("");
            mEditText_vin.setText("");
            mEditText_code.setText("");
            mRusultLayout.setVisibility(View.VISIBLE);
            imagepic.setImageDrawable(getResources().getDrawable(R.drawable.logo));
            imagefinger.setBackgroundResource(R.drawable.vote_example);
            imagefinger.setImageResource(R.drawable.fp_null);
            mRusultLayout.setBackgroundColor((0xAA9BC032));
            mTextView_result.setText(R.string.personscreen_text_dialog_readcard);

            try {
                ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRFID_open();//读卡

            return true;
        } else if (keyCode == 134 || keyCode == 135 || keyCode == 136) {
            if (keyCode == 134) {
            } else if (keyCode == 135) {
                bVinCode[0] = 0x00;
                return super.onKeyDown(keyCode, event);
            }

            Log.i(TAG, "CLick READ button");
            mEditText_gender.setText("");
            mEditText_birthday.setText("");
            mEditText_occ.setText("");
            mEditText_name.setText("");
            mEditText_vin.setText("");
            mEditText_code.setText("");

            try {
                ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    protected void onStop() {
        mHandler.removeCallbacks(mopenrfid);
        super.onStop();
        mRFID_close();

        mHandler.removeCallbacks(mopenrfid);
        opencount = 0;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ap != null)
            ap.PlayRelease();

        if (mCursor != null)
            mCursor.close();
        opencount = 0;
        mRFID_close();

        rfidsucess = true;
        mHandler.removeCallbacks(mopenrfid);
        if (MyDialog != null) {

            mProgresshandler.removeMessages(2);
            mProgresshandler.removeMessages(1);
            mProgresshandler.removeMessages(0);

            MyDialog.dismiss();
            MyDialog.cancel();
            MyDialog = null;
        }
    }

    private void insert_db(boolean status) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm");
        String date = format.format(new Date());
        String time = format2.format(new Date());
        if (status) {
            mVoteDB.insert_vintable(dataPuCode, dataVin, "Y", date, time, preferences.getString("CURRENT_ELECTION_TYPE", "President"));
        } else {
            mVoteDB.insert_vintable(dataPuCode, dataVin, "N", date, time, preferences.getString("CURRENT_ELECTION_TYPE", "President"));
        }
    }

    private void judgePucode() {
        if (!configPucode.equals(dataPuCode)) {
            Toast.makeText(person.this, R.string.personscreen_text_pucode_fail, Toast.LENGTH_SHORT).show();
            pucode_is_ok = false;
        } else {
            pucode_is_ok = true;
        }
    }

    private boolean judgeVin_status() {
        mCursor = mVoteDB.query(VIN_TABLE_NAME,
                null, "VIN=?",
                new String[]{dataVin}, null,
                null, null);

        if (mCursor.moveToFirst()) {
            if (mCursor.getString(3).equals("N")) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


    private void judgeVin() {
        if (pucode_is_ok == false)
            return;

        mCursor = mVoteDB.query(VIN_TABLE_NAME,
                null, "VIN=?",
                new String[]{dataVin}, null,
                null, null);

        mCursor.moveToFirst();

        //未验证
        if (mCursor.getCount() == 0) {
            Log.v(TAG, "judgeVin insert = ");
            insert_db(false);
        } else {//已存在
            Log.v(TAG, "judgeVin11111 ");
            //判断状态
            if (mCursor.getString(3).equals("N")) {
//				bExist = false;
                Log.v(TAG, "judgeVin2222222222 ");
            } else {
                Log.v(TAG, "judgeVin33333333333333 ");
                mRusultLayout.setVisibility(View.VISIBLE);
                mRusultLayout.setBackgroundColor((0xAAECD00D));
                mTextView_result.setText(R.string.personscreen_text_result_fail2);
            }
        }
    }

    private void Databackups_pgm() {
        try {
            File oldfile;
            String newPath;

            if (Environment.isExternalStorageRemovable()) {
                oldfile = new File("/storage/sdcard1/Vote");
                newPath = "/storage/sdcard1/Vote/vote.pgm";
            } else {
                oldfile = new File("/storage/sdcard0/Vote");
                newPath = "/storage/sdcard0/Vote/vote.pgm";
            }

            if (!oldfile.exists()) {
                oldfile.mkdirs();
            }

            String votefilePath = getFilesDir().getParent().toString() + "/"
                    + getResources().getString(R.string.app_name) + ".pgm";

            InputStream mInputStream = new FileInputStream(votefilePath);

            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = mInputStream.read(buffer)) != -1) {
                bytesum += byteread; // 字节数 文件大小
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            mInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // backups
    private void Databackups_xyt() {
        try {
            File oldfile;
            String newPath;

            if (Environment.isExternalStorageRemovable()) {
                oldfile = new File("/storage/sdcard1/Vote");
                newPath = "/storage/sdcard1/Vote/vote.xyt";
            } else {
                oldfile = new File("/storage/sdcard0/Vote");
                newPath = "/storage/sdcard0/Vote/vote.xyt";
            }

            if (!oldfile.exists()) {
                oldfile.mkdirs();
            }

            String votefilePath = getFilesDir().getParent().toString() + "/"
                    + getResources().getString(R.string.app_name) + ".xyt";

            InputStream mInputStream = new FileInputStream(votefilePath);

            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = mInputStream.read(buffer)) != -1) {
                bytesum += byteread; // 字节数 文件大小
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            mInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Runnable mopenrfid = new Runnable() {
        @Override
        public void run() {
            if (rfidsucess) {
                rfidsucess = false;
                opencount = 0;
                mHandler.removeCallbacks(mopenrfid);
                return;
            }
            int nRet = com.xd.rfid.RFIDModuleOpen();
            if (nRet == 0) {
                new doTestRfid().start();
            } else {
                String strOut = "Open failed, nRet=" + nRet;
                updateInfo(strOut, HANDLE_READCARD_FAIL);
            }
            Rifhandler.postDelayed(mopenrfid, 500);
        }
    };
    int opencount = 0;

    public void mRFID_open() {
        opencount++;
        if (opencount == 1) {
            Thread thread = new Thread(mopenrfid);
            thread.start();
        }
    }

    public void mRFID_close() {
        com.xd.rfid.RFIDRfClose();
        com.xd.rfid.RFIDMoudleClose();
    }

    public void compareFinger() {
        is_fp_back = true;
        String filePath = getFilesDir().getParent().toString() + "/";
        String votefilePath = getFilesDir().getParent().toString() + "/" + getResources().getString(R.string.app_name);

        CallDecoder cd = new CallDecoder();
        cd.Bmp2Pgm(votefilePath + ".bmp", votefilePath + ".pgm");
        CallFprint cf = new CallFprint();
        cf.pgmChangeToXyt(votefilePath + ".pgm", votefilePath + ".xyt");

        //crj add 10.23
        String Extension = "xyt";
        File[] files = new File(filePath).listFiles();
        List<String> lstFile = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().substring(files[i].getName().length() - Extension.length()).equals(Extension)) //判断
            {
                if (!files[i].getName().equals("Vote.xyt")) {
                    lstFile.add(files[i].getName());
                }
            }
        }

        String[] xytFiles;
        xytFiles = new String[lstFile.size()];
        for (int i = 0; i < lstFile.size(); i++) {
            xytFiles[i] = filePath + lstFile.get(i).toString();
        }

        int nRet = 0;
        int i = 0;
        int fMatch = 0;
        String cmpResult = null;
        for (i = 0; i < xytFiles.length; i++) {
            nRet = cf.fprintCompare(filePath + "/Vote.xyt", xytFiles[i]);
            Log.i(TAG, "fprintCompare " + xytFiles[i] + "[" + i + "] nRet= " + nRet);
            cmpResult += ("(" + i + ") value=" + nRet + "\n");
            if (nRet >= fp_matchvalue) break;
        }

        int value = nRet + fp_matchvalue;
        if (i == xytFiles.length) {
            fMatch = 0;    // 四个指纹均不匹配
            fp_is_ok = false;
            Log.v(TAG, "fMatch = 000000 = ");

            mRusultLayout.setVisibility(View.VISIBLE);
            mRusultLayout.setBackgroundColor((0xAACB1D04));

            String failtext = getResources().getString(R.string.personscreen_text_result_fail) + " " + "(" + nRet + "/" + value + ")";
            mTextView_result.setText(failtext);

            if (ap != null) {
                try {
                    ap.PlayAsset(PLAY_SOUND_FINGER_AUTHENTICATION_FAIL, am);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            fMatch = 1;    //匹配
            fp_is_ok = true;
            Log.v(TAG, "fMatch =111111 = ");

            if (!judgeVin_status() && pucode_is_ok == true) {
                mVoteDB.update_vintable(dataVin, "Y");
            }
            mRusultLayout.setVisibility(View.VISIBLE);
            mRusultLayout.setBackgroundColor((0xAA9BC032));
            String stext = getResources().getString(R.string.personscreen_text_result_success) + " " + "(" + nRet + "/" + value + ")";
            mTextView_result.setText(stext);

            if (ap != null) {
                try {
                    ap.PlayAsset(PLAY_SOUND_FINGER_VERIFICATION, am);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        //save xyt to device
        Databackups_xyt();

        //save PGM to device
        Databackups_pgm();
    }

    //12.19
    private Boolean is_LockVoteStatus() {
        Boolean is_login = preferences.getBoolean("is_lockVote", false);
        return is_login;
    }
}
