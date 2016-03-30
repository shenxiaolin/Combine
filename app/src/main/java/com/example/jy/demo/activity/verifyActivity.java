package com.example.jy.demo.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.os.Build;
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

import com.example.jy.demo.db.VoteVin_DBHelper;
import com.example.jy.demo.fingerprint.CallDecoder;
import com.example.jy.demo.fingerprint.CallFprint;
import com.example.jy.demo.fingerprint.audioPlay;
import com.example.jy.demo.passport.CameraOpen;
import com.example.jy.demo.passport.R;
import com.opencv.LibImgFun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//zanshi 963 delete
//import com.xd.PsamMoudle;

public class verifyActivity extends Activity {

    String LOG_TAG = "vote";
    String TAG = "vote";

    private Button mbuttonback, mbuttonquery, mbuttonread, mbuttonfp;

    private TextView mTextView_result;
    private ImageButton mbutton_closeresult;
    private LinearLayout mRusultLayout;

    private EditText mEditText_gender, mEditText_birthday, mEditText_occ,
            mEditText_name, mEditText_vin, mEditText_code, mEditText_surname;

    private ImageView imagepic, imagefinger;

    ProgressDialog MyDialog;

    private final int comparedata_time = 3000;

    private Handler mProgresshandler;
    Message myMessage = new Message();// 消息对象

    private SharedPreferences preferences;
    private Editor editor;
    // private final int fprint_num = 2; // 可扫描的次数
    private VoteVin_DBHelper mVoteDB;
    // private int mfprint;
    private Cursor mCursor;
    private String VIN_TABLE_NAME;
    private Boolean mthreadRun = true;

    private String configPucode;

    private String dataPuCode, dataVin, dataVtype, dataStatus;


    // xiongdi

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

//	public boolean bExist = false;

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
        setContentView(R.layout.verify);
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

        //NfcManager manager = (NfcManager) this
        //		.getSystemService(Context.NFC_SERVICE);

        //mAdapter = manager.getDefaultAdapter();
        //nfc

        mbuttonread = (Button) findViewById(R.id.button_rc);
        mbuttonfp = (Button) findViewById(R.id.button_fp);
        //mbuttonquery = (Button) findViewById(R.id.button_qu);
        mbuttonback = (Button) findViewById(R.id.button4_back);

        // mEditText_birthday = (EditText) findViewById(R.id.editText_birthday);
        // mEditText_occ = (EditText) findViewById(R.id.editText_occ);

        mEditText_surname = (EditText) findViewById(R.id.editText_surname);
        mEditText_name = (EditText) findViewById(R.id.editText_name);
        mEditText_vin = (EditText) findViewById(R.id.editText_vin);
        mEditText_code = (EditText) findViewById(R.id.editText_code);
        mEditText_gender = (EditText) findViewById(R.id.editText_gender);

        //
        imagepic = (ImageView) findViewById(R.id.imageView_avater);
        imagefinger = (ImageView) findViewById(R.id.imageView_finger);

        // result layout
        mTextView_result = (TextView) findViewById(R.id.textView_result);
        mRusultLayout = (LinearLayout) findViewById(R.id.linearLayout_result);
        // mRusultLayout.setVisibility(View.GONE);
        mbutton_closeresult = (ImageButton) findViewById(R.id.button_result_close);

        mbutton_closeresult.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mRusultLayout.setVisibility(View.GONE);
            }
        });

        am = getAssets();

        preferences = this.getSharedPreferences(
                getResources().getString(R.string.SystemConfig_sp),
                MODE_PRIVATE);
        editor = preferences.edit();

        configPucode = preferences.getString("PU_CODE", "34-16-10-003");
        dataVtype = preferences.getString("CURRENT_ELECTION_TYPE", "President");
        fp_matchvalue = Integer.parseInt(preferences.getString("CURRENT_FINGERPRINT_NUM", "16"));

        Rifhandler = new Handler();
        mVoteDB = new VoteVin_DBHelper(this);
        mCursor = mVoteDB.Query_Vin_table();
        VIN_TABLE_NAME = mVoteDB.VIN_TABLE_NAME;

        mProgresshandler = new Handler() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                                //Log.i("wy","1111111111111111111");
                                MyDialog.dismiss();
                                mProgresshandler.removeMessages(1);
                                mProgresshandler.removeMessages(0);
                                MyDialog.cancel();
                                MyDialog = null;
                            }

                            //	Log.i("wy","12222222222222222");
                            MyDialog = ProgressDialog.show(verifyActivity.this, "", getResources().getString(R.string.handle_text_wait), true);
                            MyDialog.setCanceledOnTouchOutside(false);
                            MyDialog.setCancelable(true);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
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
//								String filePath = Environment.getExternalStorageDirectory().toString() + "/"
//										+ getResources().getString(R.string.app_name) + "/"
//										+ getResources().getString(R.string.app_name) + "2" + ".bmp";

                                    String filePath = getFilesDir().getParent().toString() + "/"
                                            + getResources().getString(R.string.app_name) + "2" + ".bmp";

                                    File mfile2 = new File(filePath);

                                    FileInputStream fis;
                                    fis = new FileInputStream(mfile2);
                                    Bitmap bmp = BitmapFactory.decodeStream(fis);

                                    Matrix m = new Matrix();
                                    m.postRotate(90);

                                    Bitmap bm = Bitmap.createBitmap(bmp, 50, 50, bmp.getWidth() - 100,
                                            bmp.getHeight() - 100, m, true);

                                    Drawable drawable = new BitmapDrawable(bm);
//								imagefinger.setImageDrawable(drawable);
                                    imagefinger.setBackground(drawable);

                                    if (fp_is_ok) {

                                        imagefinger.setImageResource(R.drawable.fp_ok);

                                    } else {

                                        imagefinger.setImageResource(R.drawable.fp_fail);

                                    }

//								mRusultLayout.setVisibility(View.VISIBLE);
//								mRusultLayout.setBackgroundColor((0xAA9BC032));
//								mTextView_result
//								.setText(R.string.personscreen_text_dialog_getfinger);

                                } catch (FileNotFoundException e) {
                                    // TODO Auto-generated catch block
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
                // TODO Auto-generated method stub

                // Intent it = new Intent(verifyActivity.this, fprint.class);
                // startActivity(it);

                // 判断是否超出扫描次数
                // mfprint = preferences.getInt("fprint_Num", 0);
                // if (mfprint < fprint_num) {

                // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // startActivityForResult(intent, 1);
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

                                Intent it = new Intent(verifyActivity.this, CameraOpen.class);
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

                //
                // AlertDialog.Builder builder = new AlertDialog.Builder(
                // this);
                // builder.setTitle("fingerprint matching failed");
                // builder.setMessage("Do you want to input fingerprint again ?");
                //
                // builder.setPositiveButton("Yes",
                // new DialogInterface.OnClickListener() {
                // public void onClick(DialogInterface dialog,
                // int which) {
                //
                // Intent intent = new Intent(
                // MediaStore.ACTION_IMAGE_CAPTURE);
                // startActivityForResult(intent, 1);
                //
                // // 向数据库更新flag
                // Editor editor = preferences.edit();
                // editor.putBoolean("is_second_fprint",
                // true);
                // // 提交更改
                // editor.commit();
                //
                // }
                // });
                // builder.setNegativeButton("No", null);
                //
                // AlertDialog dialog = builder.create();
                // dialog.show();

                // } else {
                //
                // new AlertDialog.Builder(arg0.getContext())
                // .setMessage("指纹录入已超过指定次数")
                // .setPositiveButton("OK", null).show();
                //
                // }
            }
        });

        mbuttonread.setOnClickListener(new OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                //imagefinger.setImageDrawable(getResources().getDrawable(R.drawable.vote_example));
                imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
                imagefinger.setImageResource(R.drawable.fp_null);
                mRusultLayout.setBackgroundColor((0xAA9BC032));
                mTextView_result
                        .setText(R.string.personscreen_text_dialog_readcard);

//				//语音读卡提示
//				if(ap != null){
//					try {
//						ap.PlayAsset(PLAY_SOUND_FINGER_VERIFICATION,am);	// 1
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalStateException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}

                try {
                    ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
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

                mRFID_open();


//				RandomAccessFile file;
//				try {
//
//					String mfile1 = "/sdcard";
//					String mfile2 = "/data/data/com.example.jy.demo.fingerprint";
//
//					// file = new RandomAccessFile("/sdcard/pic1.jp2", "r");
//					file = new RandomAccessFile(mfile2 + "/pic1.jp2", "r");
//					int len = (int) file.length(); // 取得文件长度（字节数）
//					// Log.v("crjlog", "len = " + len);
//					// // file.seek(208); //跳过 jp2 文件头
//					// byte[] b = new byte[len];
//					// file.readFully(b);
//					// file.close();
//					// Log.v("crjlog", "b = " + b);
//					// CallDecoder cd = new CallDecoder();
//					// // cd.DecodeMj2Data(b, len, "/sdcard/pic1.bmp");
//					// cd.DecodeMj2Data(b, len, mfile2 + "/pic1.bmp");
//					//
//					// // FileInputStream fis = new
//					// // FileInputStream("/sdcard/pic1.bmp");
//					// FileInputStream fis = new FileInputStream(mfile2
//					// + "/pic1.bmp");
//					// Bitmap bmp = BitmapFactory.decodeStream(fis);
//					//
//					// Drawable drawable = new BitmapDrawable(bmp);
//					//
//					// imagepic.setImageDrawable(drawable);
//
//					// 打开NFC 读取卡内数据
//
//					// //若读取成功，则将信息填存到此界面
//					// if(true){
//					// mEditText_gender.setText("MALE");
//					// mEditText_birthday.setText("01-01-1978");
//					// mEditText_occ.setText("OTHER");
//					// mEditText_name.setText("Thong, Frank");
//					// mEditText_vin.setText("238B 9605 F036 6854 937");
//					// mEditText_code.setText("37-06-01-012");
//
//					// //打开camera，录取指纹
//					// Intent intent = new
//					// Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//					// startActivityForResult(intent, 1);
//
//					// }else{
//					// 若读取失败，则弹出提示
//					// new AlertDialog.Builder(arg0.getContext())
//					// .setTitle("Loading auto-save data failed")
//					// .setMessage("Please try again")
//					// .setPositiveButton("OK", null)
//					// .show();
//					// }
//
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// mEditText_birthday.setText("01-01-1978");
//				// mEditText_occ.setText("OTHER");
//				mEditText_surname.setText("Adedoyin, Oluwaseun");
//				mEditText_name.setText("01-01-1960");
//				mEditText_vin.setText("90F5 AFE4 3329 5260 566");
//				mEditText_code.setText("34-16-10-003");
//				mEditText_gender.setText("Male");
//
//				mRusultLayout.setVisibility(View.VISIBLE);
//				mRusultLayout.setBackgroundColor((0xAA9BC032));
//				mTextView_result
//						.setText(R.string.personscreen_text_result_success);

            }
        });

        mbuttonback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });

		/*mbuttonquery.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

//				Intent it = new Intent(verifyActivity.this, VinQuery.class);
//				startActivity(it);

				//wt dcs
				Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName com = new ComponentName("com.wintone.passportreader.sdk","com.wintone.passportreader.sdk.MainActivity");
                if (com != null) {
                    intent.setComponent(com);
                    getApplicationContext().startActivity(intent);
                }

				/*new AlertDialog.Builder(verifyActivity.this)
				.setTitle("请选择")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(type, 0,
				  new DialogInterface.OnClickListener() {

				     public void onClick(DialogInterface dialog, int which) {

				        switch (which) {
				        case 0:
						case 1:	// 一代身份证
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
						case 9:
						case 10:
						case 11:
						case 12:
						case 13:
						case 14:
						case 15:
							nMainID = which + 1;
							break;

						case 16:
						case 17:
							nMainID = which + 984;
							break;

						case 18:
						case 19:
						case 20:
						case 21:
						case 22:
						case 23:
						case 24:

							nMainID = which + 985;
							break;

						default:
							break;
						}
				        Log.v("crjlog","which = " + which);
				        Log.v("crjlog","nMainID = " + nMainID);

						Intent mintent = new Intent(verifyActivity.this,CameraActivity.class);
						mintent.putExtra("nMainID", nMainID);
						startActivity(mintent);

				        dialog.dismiss();

				     }
				  }
				)
				.setNegativeButton("取消", null)
				.create()
				.show();  */


        //}
        //});

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

            // file = new RandomAccessFile("/sdcard/pic1.jp2", "r");
            file = new RandomAccessFile(mfile2 + "/pic1.jp2", "r");
            int len = (int) file.length(); // 取得文件长度（字节数）
            Log.v("crjlog", "len = " + len);
            // file.seek(208); //跳过 jp2 文件头
            byte[] b = new byte[len];
            file.readFully(b);
            file.close();
            Log.v("crjlog", "b = " + b);
            CallDecoder cd = new CallDecoder();
            // cd.DecodeMj2Data(b, len, "/sdcard/pic1.bmp");
            cd.DecodeMj2Data(b, len, mfile2 + "/pic1.bmp");

            // FileInputStream fis = new FileInputStream("/sdcard/pic1.bmp");
            FileInputStream fis = new FileInputStream(mfile2 + "/pic1.bmp");
            Bitmap bmp = BitmapFactory.decodeStream(fis);

            Drawable drawable = new BitmapDrawable(bmp);
//			imagepic = (ImageView) findViewById(R.id.imageView1);
            imagepic.setImageDrawable(drawable);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
        Log.v("crjlog", "onResume = ");

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
            //System.out.println(buffer);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
        }
        imagefinger.setImageResource(R.drawable.fp_null);
        mRusultLayout.setBackgroundColor((0xAA9BC032));

        mTextView_result.setTextColor(0xFF800000);
        mTextView_result
                .setText(R.string.personscreen_text_dialog_hint);


    }

    private void get_finger_img(Intent data) {

        String sdStatus = Environment.getExternalStorageState();// 获取sd卡路径
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Log.v("crjlog", "SD card is not avaiable/writeable right now.");
            return;
        }

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
        Log.v("crjlog", "bitmap = " + bitmap);

        String filePath = Environment.getExternalStorageDirectory().toString()
                + "/" + getResources().getString(R.string.app_name) + "/"
                + 111111 + ".bmp";

        Log.v("crjlog", "filePath = " + filePath);

        int[] pix = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pix, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        int resut = LibImgFun.mySaveImage(pix, bitmap.getWidth(),
                bitmap.getHeight(), filePath);

        Log.v("crjlog", "resut = " + resut);

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

            Log.v("crjlog", "resultCode == data =  " + data);

            // init thread
            mthreadRun = true;
            // 进度条
            startThread();

            compareFinger();

//			bReadOk = false;
            // 得到指纹图片
            // get_finger_img(data);

            // show fingerprint
            // imagefinger.setImageDrawable(drawable);

//			// 匹配图片
//			if (true) {// 匹配成功，则保存数据到数据库Vote.db
//
//				mRusultLayout.setVisibility(View.VISIBLE);
//				mRusultLayout.setBackgroundColor((0xAA9BC032));
//				mTextView_result
//						.setText(R.string.personscreen_text_result_success);
//
//				// 像表内写
//				// insert_db();
//
//			} else {// 匹配失败
//
//				mRusultLayout.setVisibility(View.VISIBLE);
//				mRusultLayout.setBackgroundColor((0xAACB1D04));
//				mTextView_result
//						.setText(R.string.personscreen_text_result_fail);
//
//				new AlertDialog.Builder(this).setTitle("指纹匹配失败")
//						.setMessage("请重新指纹录入").setPositiveButton("OK", null)
//						.show();
//			}

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
                // mEditText_code.setText(info);
                if (msg.arg1 == HANDLE_VOTER_ACCREDITATION_SUCCESSFUL) {
                    // mHint.setTextColor(Color.GREEN);

                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result
                            .setText(R.string.personscreen_text_result_success);
                } else if (msg.arg1 == HANDLE_FINGER_AUTHENTICATION_FAIL) {
                    // mHint.setTextColor(Color.RED);

                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result
                            .setText(R.string.personscreen_text_result_fail);
                } else if (msg.arg1 == HANDLE_READCARD_SUCCESSFUL) {// read nfc success
                    // mHint.setTextColor(Color.BLUE);

                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result
                            .setText(R.string.personscreen_text_readnfc_success);

                    //判断 pucode 不一致 ，语音提示
                    judgePucode();

                    //判断VIN是否已被验证过
                    //12.19
                    Log.d("huangmin", "LockVoteStatus= " + LockVoteStatus);
                    if (!LockVoteStatus) {
                        judgeVin();
                    }

                } else if (msg.arg1 == HANDLE_READCARD_FAIL) {    // read nfc fail
                    // mHint.setTextColor(Color.BLUE);

                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAACB1D04));
                    mTextView_result.setText(info);
                    //.setText(R.string.personscreen_text_readnfc_fail);
                } else if (msg.arg1 == HANDLE_READCARD_REPEAT_TEXT) {    // read nfc Repeat
                    // mHint.setTextColor(Color.BLUE);
                    mRusultLayout.setVisibility(View.VISIBLE);
                    mRusultLayout.setBackgroundColor((0xAA9BC032));
                    mTextView_result
                            .setText(R.string.personscreen_text_dialog_hint);
                }

                // mHint.setText(info);
            } else if (msg.what == MSG_UPDATE_GEN) {
                mEditText_gender.setText(info);
            } else if (msg.what == MSG_UPDATE_BIRTH) {
//				mEditText_birthday.setText(info);
                mEditText_name.setText(info);
            } else if (msg.what == MSG_UPDATE_OCC) {
                //mEditText_occ.setText(info);
            } else if (msg.what == MSG_UPDATE_NAME) {
                //mEditText_name.setText(info);
                mEditText_surname.setText(info);
            } else if (msg.what == MSG_UPDATE_VIN) {
                mEditText_vin.setText(info);
            } else if (msg.what == MSG_UPDATE_CODE) {
                mEditText_code.setText(info);
            } else if (msg.what == MSG_UPDATE_IMAGE) {
                RandomAccessFile file;
                try {

//					String mfile1 = "/sdcard";
                    String mfile1 = getFilesDir().getParent().toString() + "/";

                    file = new RandomAccessFile(mfile1 + "image.jp2", "r");
                    int len = (int) file.length();
                    Log.v("crjlog", "len = " + len);
                    // °üº¬ jp2 ÎÄ¼þÍ· 208 ×Ö½Ú
                    byte[] b = new byte[len];
                    file.readFully(b);
                    file.close();
                    Log.v("crjlog", "b = " + b);
                    CallDecoder cd = new CallDecoder();
                    cd.DecodeMj2Data(b, len, mfile1 + "image.bmp");

                    FileInputStream fis = new FileInputStream(mfile1
                            + "/image.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);

                    Drawable drawable = new BitmapDrawable(bmp);
//					imagepic = (ImageView) findViewById(R.id.imageView1);
                    // imagepic.setImageAlpha(0xFF);
                    // ÈÃÍ¼Ïñ¿É¼û //yjh
                    // imagepic.setVisibility(imagepic.VISIBLE);

                    imagepic.setImageDrawable(drawable);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
                    }
                    imagefinger.setImageResource(R.drawable.fp_null);

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            /*
             * else if (msg.what == MSG_CLOSE_RFID) {
			 *
			 * }
			 */

        }
    };


    void updateInfo(String info, int arg1) {
        Message msg = new Message();
        msg.arg1 = arg1;
        msg.what = MSG_TEST;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INFO, info);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

		/*
		 * info = info + "\r\n"; try { writeFile(LOG_FILE_NAME,info); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
    }


    boolean rfidsucess = false;

    private class doTestRfid extends Thread {

        byte[] bIdLen = new byte[1];
        byte[] bSNR = new byte[64];

        private int nSector = 16;
        private int nBlock = 4;
        private long t0, t1;

        private String readblockdata() {
            byte sak = 0;
            short atqa = 0;
            String cardType = null;
            String m1para = null;
            byte[] bId = new byte[4];
            String blockdata = null;
            StringBuffer strAllBlockData = new StringBuffer();
            String strOut = "SNR=" + com.xd.Converter.printHexLenString(bSNR, bIdLen[0]) + ",  len=" + com.xd.Converter.printHexLenString(bIdLen, 1) +
                    ", time=";

            t0 = System.currentTimeMillis();
            int nRet = com.xd.rfid.RFIDGetSNR(0, bIdLen, bSNR);
            Log.d("huangmin", "readblockdata nRet= " + nRet);
            if (nRet == 0) {
                mProgresshandler.sendEmptyMessage(1);
                sak = bSNR[bIdLen[0] - 3];
                atqa = (short) (bSNR[bIdLen[0] - 1] * 256 + bSNR[bIdLen[0] - 2]);
                if (((sak & 0x20) == 0x20) || (sak == 0x53)) {
                    if (atqa == 0x0344) {
                        //Desfire
                        cardType = ", Desfire card";
                    } else {
                        //CPU
                        cardType = ", CPU card";
                    }
                } else if (atqa == 0x0044) {
                    cardType = ", UL card";
                } else if ((sak == 0x08) || (sak == 0x18)) {
                    //M1 S50 / S70
                    if (sak == 0x08) {
                        cardType = ", S50 card";
                    } else {
                        cardType = ", S70 card";
                    }
                    m1para = "SNR=" + com.xd.Converter.printHexLenString(bSNR, 7) + cardType;
                    bId[0] = bSNR[0];
                    bId[1] = bSNR[1];
                    bId[2] = bSNR[2];
                    bId[3] = bSNR[3];
                    if (m1para.indexOf("S70 card") > 0) {
                        nSector = 40;
                    }
                    byte[] bKey = new byte[6];
                    byte[] bOutData = new byte[16];
                    byte errflag = 0;
                    int blockIndex = 0, sectorNumber = 0;


                    // S50 �Ŀ�, 16 ����;  S70�Ŀ�, 40����
                    for (int i = 0; i < nSector; i++) {
                        Arrays.fill(bKey, (byte) 0xFF);
                        if (i > 31) {
                            sectorNumber = 32 + (i - 32) * 4;
                            nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) sectorNumber, bKey, bId);
                        } else {
                            if (i == 0) {
                                System.arraycopy(bId, 0, bKey, 0, 4);
                                bKey[4] = 0x20;
                                bKey[5] = 0x12;
                                nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) i, bKey, bId);
                            } else {
                                nRet = com.xd.rfid.MifAuthen((byte) 0x0A, (byte) i, bKey, bId);
                            }
                        }

                        long t4 = System.currentTimeMillis();

                        if (nRet != 0) {
                            strOut = "MifAuthen sector(" + i + ") failed, nRet=" + nRet;
                            errflag = 1;

                            updateInfo(strOut, HANDLE_READCARD_FAIL);
                            break;
                        } else {
                            if (i > 31) {
                                nBlock = 16;
                            }

                            StringBuffer sbBlockData = new StringBuffer();
                            for (int j = 0; j < nBlock; j++) {
                                blockIndex = (i * nBlock + j);
                                if (i > 31) {
                                    blockIndex = 32 * 4 + (i - 32) * nBlock + j;
                                }

                                long t5 = System.currentTimeMillis();
                                nRet = com.xd.rfid.MifRead(blockIndex, bOutData);
                                long t6 = System.currentTimeMillis();
                                Log.i("huangmin", "MifRead time=" + (t6 - t5) + "(ms)");

                                if (nRet != 0) {
                                    strOut = "MifRead block(" + blockIndex + ") failed, nRet=" + nRet;
                                    errflag = 2;

                                    updateInfo(strOut, HANDLE_READCARD_FAIL);
                                    break;
                                }
                                DecimalFormat df = new DecimalFormat();
                                String style = "000";
                                df.applyPattern(style);
                                String outString = com.xd.Converter.printHexLenString(bOutData, 16);
                                sbBlockData.append(outString);
                                Log.d("huangmin", "sbBlockData.toString()= " + sbBlockData.toString());
                                sbBlockData.append("\n");

                            }//for j

                            strAllBlockData.append(sbBlockData);
                            if (errflag != 0) break;
                            blockdata = strAllBlockData.toString();

                        }

                    }//for i

                    if (errflag == 0) {
                        t1 = System.currentTimeMillis();
                        Log.i(TAG, "readdata Total Time=" + (t1 - t0) + "(ms)");
                    }
                    ///////
                } else {
                    cardType = ", Unknown card";
                }
                Log.d("huangmin", "cardType= " + cardType);
            }
            return blockdata;
        }

        private String getcurractvity() {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            return cn.getClassName();

        }


        public void run() {
            String info = null;
            int nRet = com.xd.rfid.RFIDInit();

            if (nRet == 0) {
                Log.d("huangmin", "init ok !");
                int nset = com.xd.rfid.RFIDTypeSet(0);
                if (nset != 0) {
                    info = "Init failed, nRet=" + nRet;
                    //updateInfo(info, HANDLE_READCARD_FAIL);
                } else {
                    String blockdata = readblockdata();
//                    showmessageinfo(blockdata);
                }
            } else {
                info = "Init failed, nRet=" + nRet;
                updateInfo(info, HANDLE_READCARD_FAIL);
            }

        }//doTestRfid.run()

    }//doTestRfid


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String info = null;
        Log.d("huangmin", "keyCode= " + keyCode);

        if (keyCode == 57) {
            //do what you want1
            info = "keyCode: read card";
//				updateInfo(info, 2);
        } else if ((keyCode == 251 || keyCode == 252) && (event.getRepeatCount() == 0)) {     //扫描指纹，entry camera    level  up
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

                            // Intent it = new Intent(verifyActivity.this, CameraOpen_Automatic.class);
                            //startActivityForResult(it, 1);
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


            return true;
        } else if ((keyCode == 250 || keyCode == 249) && (event.getRepeatCount() == 0)) {    // 读卡     level down

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
            //imagefinger.setImageDrawable(getResources().getDrawable(R.drawable.vote_example));
            imagefinger.setBackground(getResources().getDrawable(R.drawable.vote_example));
            imagefinger.setImageResource(R.drawable.fp_null);
            mRusultLayout.setBackgroundColor((0xAA9BC032));
            mTextView_result
                    .setText(R.string.personscreen_text_dialog_readcard);

//				//语音读卡提示
//				if(ap != null){
//					try {
//						ap.PlayAsset(PLAY_SOUND_FINGER_VERIFICATION,am);	// 1
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalStateException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}

            try {
                ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
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
            Log.d("huangmin", "mRFID_open");
            mRFID_open();

            return true;
        } else if (keyCode == 134 || keyCode == 135 || keyCode == 136) {
            info = "keyCode=" + keyCode;
            if (keyCode == 134) {
//					new doTestRfid(1).start();
            } else if (keyCode == 135) {//�˴�����, �Ա�������(�� Samuel ����)ʹ��ʱ VinCode ��ƥ��
                bVinCode[0] = 0x00;
//					info += ("Code=" + bVinCode[0]);
//					updateInfo(info, 2);
                return super.onKeyDown(keyCode, event);
            }

            Log.i(TAG, "CLick READ button");
            mEditText_gender.setText("");
            mEditText_birthday.setText("");
            mEditText_occ.setText("");
            mEditText_name.setText("");
            mEditText_vin.setText("");
            mEditText_code.setText("");
//				mHint.setText("");

//				//��ͼ������		//yjh
//				try {
//					FileInputStream fis = new FileInputStream("/sdcard/blank.bmp");
//					Bitmap bmp = BitmapFactory.decodeStream(fis);
//					Drawable drawable = new BitmapDrawable(bmp);
////					imagepic = (ImageView) findViewById(R.id.imageView1);
//					imagepic.setImageDrawable(drawable);
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//					return super.onKeyDown(keyCode, event);
//				}


            try {
                ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
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

            try {
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//			
//			else {
//				info = "keyCode=" + keyCode;
//				updateInfo(info, 2);
//			}

        return super.onKeyDown(keyCode, event);
    }


    protected void onStop() {
        // TODO Auto-generated method stub

        mHandler.removeCallbacks(mopenrfid);
        super.onStop();
        mRFID_close();

        mHandler.removeCallbacks(mopenrfid);
        opencount = 0;
//		com.io.io.IoClose();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.v("crjlog", "onDestroy ");
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
//		com.io.io.IoClose();

    }

    // insert vote.db
    private void insert_db(boolean status) {

        // int Vin = (int) (Math.random() * 1000000000);
//		String code = "34-16-10-003";
//		String Vin = "1111 AFBB E729 444";
        // String Vin = "11 22";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm");
        String date = format.format(new Date());
        String time = format2.format(new Date());

//		String[] readomWord = { "Y", "N" };
//		String Status = readomWord[(int) (Math.random() * 2)];
//		String vin_ext = dataVin.replaceAll("\\s","");
//		Log.v("crjlog","vin_ext22" +  vin_ext); 

        if (status) {

            mVoteDB.insert_vintable(dataPuCode, dataVin, "Y", date, time,
                    preferences.getString("CURRENT_ELECTION_TYPE", "President"));
        } else {
            mVoteDB.insert_vintable(dataPuCode, dataVin, "N", date, time,
                    preferences.getString("CURRENT_ELECTION_TYPE", "President"));
        }

    }


    private void judgePucode() {
        if (!configPucode.equals(dataPuCode)) {
            Toast.makeText(verifyActivity.this, R.string.personscreen_text_pucode_fail,
                    Toast.LENGTH_SHORT).show();
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

//		String vin_ext = dataVin.replaceAll("\\s","");
//		Log.v("crjlog","vin_ext111" +  vin_ext);

        if (pucode_is_ok == false)
            return;

        mCursor = mVoteDB.query(VIN_TABLE_NAME,
                null, "VIN=?",
                new String[]{dataVin}, null,
                null, null);

        mCursor.moveToFirst();

        //未验证
        if (mCursor.getCount() == 0) {

//			bExist = false;
            Log.v("crjlog", "judgeVin insert = ");
            insert_db(false);

        } else {//已存在

            Log.v("crjlog", "judgeVin11111 ");
            //判断状态
            if (mCursor.getString(3).equals("N")) {
//				bExist = false;
                Log.v("crjlog", "judgeVin2222222222 ");

            } else {
                Log.v("crjlog", "judgeVin33333333333333 ");
                //已成功投票的VIN
//				Toast.makeText(verifyActivity.this, R.string.personscreen_text_result_fail2,
//						Toast.LENGTH_SHORT).show();

//				bReadOk = false;
//				bExist = true;

                mRusultLayout.setVisibility(View.VISIBLE);
                mRusultLayout.setBackgroundColor((0xAAECD00D));
                mTextView_result
                        .setText(R.string.personscreen_text_result_fail2);

//				if(ap != null){
//					
//					try {
//						ap.PlayAsset(PLAY_SOUND_FINGER_VERIFICATION,am);	
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalStateException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
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
            // TODO Auto-generated catch block
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
            // TODO Auto-generated catch block
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
            Rifhandler.postDelayed(mopenrfid, 500);//���ʱ��Ҫ���ڶ���ʱ��
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

        String votefilePath = getFilesDir().getParent().toString() + "/"
                + getResources().getString(R.string.app_name);

        CallDecoder cd = new CallDecoder();
        cd.Bmp2Pgm(votefilePath + ".bmp", votefilePath + ".pgm");
        //cd.Bmp2Bmp("/sdcard/myImage/myImage.bmp", "/sdcard/myImage/B256_360.bmp");

        CallFprint cf = new CallFprint();
//		cf.pgmChangeToXyt("/sdcard/myImage/Vote.pgm", "/sdcard/myImage/Vote.xyt");
        cf.pgmChangeToXyt(votefilePath + ".pgm", votefilePath + ".xyt");

        //crj add 10.23
        String Extension = "xyt";
        File[] files = new File(filePath).listFiles();
        List<String> lstFile = new ArrayList<String>();

        for (int i = 0; i < files.length; i++) {

            if (files[i].getName().substring(files[i].getName().length() - Extension.length()).equals(Extension)) //判断
            {
                if (!files[i].getName().equals("Vote.xyt"))
                    lstFile.add(files[i].getName());
            }
        }

        String[] xytFiles;
        xytFiles = new String[lstFile.size()];

        for (int i = 0; i < lstFile.size(); i++) {

            xytFiles[i] = filePath + lstFile.get(i).toString();
        }

//		String[] xytFiles = {filePath +"1.xyt", filePath +"2.xyt", filePath +"6.xyt", filePath +"7.xyt"};

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

//        Toast toast = Toast.makeText(getApplicationContext(), cmpResult, Toast.LENGTH_LONG);
//       	toast.setGravity(Gravity.CENTER, 0, 0);
//       	toast.show();				
//		Toast.makeText(this, cmpResult, Toast.LENGTH_SHORT).show(); 

        if (i == xytFiles.length) {
            fMatch = 0;    // 四个指纹均不匹配
            fp_is_ok = false;
            Log.v("crjlog", "fMatch = 000000 = ");
//			updatePersonInfo(MSG_UPDATE_IMAGE, "NoMatch");
//			updateInfo("NoMatch", HANDLE_FINGER_AUTHENTICATION_FAIL);
//			insert_db(true);
//			String vin_ext = dataVin.replaceAll("\\s","");
//			Log.v("crjlog","vin_ext333" +  vin_ext); 

            mRusultLayout.setVisibility(View.VISIBLE);
            mRusultLayout.setBackgroundColor((0xAACB1D04));

            String failtext = getResources().getString(R.string.personscreen_text_result_fail) + " " + "(" + nRet + "/" + value + ")";
            mTextView_result.setText(failtext);

            if (ap != null) {

                try {
                    ap.PlayAsset(PLAY_SOUND_FINGER_AUTHENTICATION_FAIL, am);
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

        } else {
            fMatch = 1;    //匹配
            fp_is_ok = true;
            Log.v("crjlog", "fMatch =111111 = ");
//			updatePersonInfo(MSG_UPDATE_IMAGE, "Match");
//			updateInfo("Match", HANDLE_VOTER_ACCREDITATION_SUCCESSFUL);

            //向数据库写入数据
//			String vin_ext = dataVin.replaceAll("\\s","");
//			Log.v("crjlog","vin_ext333" +  vin_ext); 
//			bReadOk = false;

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
