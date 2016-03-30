package com.example.jy.demo.passport;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.jy.demo.fingerprint.CallDecoder;
import com.example.jy.demo.fingerprint.CallFprint;
import com.example.jy.demo.fingerprint.audioPlay;
import com.opencv.LibImgFun;

import java.io.IOException;

public class CameraOpen extends Activity implements SurfaceHolder.Callback {
    public static final int PLAY_SOUND_PUCODE_ERROR = 1;
    public static final int PLAY_SOUND_FINGER_SCAN_FAIL = 3;
    public static final int PLAY_SOUND_FINGER_AUTHENTICATION_FAIL = 4;
    public static final int PLAY_SOUND_FINGER_VERIFICATION = 5;


    /**
     * Called when the activity is first created.
     */
    private Camera mCamera;
    private ImageButton mButton;
    // private Button returnTomainmenu;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private Bitmap bmp = null;
    private Bitmap bmp2 = null;
    private int count;
    private String filePath, filePath2;

    private boolean is_camaeraperview = false;

    public AssetManager am;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* ���ر����� */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /* �趨��Ļ��ʾΪ���� */
        setContentView(R.layout.gather_fingerprint);

        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mButton = (ImageButton) findViewById(R.id.myButton);

		/* ����Button���¼����� */
        mButton.setOnClickListener(new Button.OnClickListener() {
            // @Override
            public void onClick(View arg0) {
                /* �ر�����Ʋ����� */
                if (!is_camaeraperview) {
                    is_camaeraperview = true;
                    mCamera.autoFocus(mAutoFocusCallback);
                }
            }
        });

        am = getAssets();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

        filePath = getFilesDir().getParent().toString() + "/"
                + getResources().getString(R.string.app_name) + ".bmp";

        holder = mSurfaceView.getHolder();
        holder.addCallback(CameraOpen.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        try {
            mCamera = mCamera.open(1);
        } catch (Exception e) {
            // TODO: handle exception
            finish();
            Toast.makeText(CameraOpen.this, "Open Camera fail !",
                    Toast.LENGTH_SHORT).show();
        }

        surfaceCreated(holder);
        initCamera();
    }


    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
            if (mCamera != null)
                mCamera.setPreviewDisplay(holder);
            else {
                finish();
                Toast.makeText(CameraOpen.this, "Open Camera fail !",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
        try {
            initCamera();
            count++;
        } catch (Exception e) {
            // TODO: handle exception
            finish();
            Toast.makeText(CameraOpen.this, "Open Camera fail !",
                    Toast.LENGTH_SHORT).show();
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopCamera();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            surfaceDestroyed(holder);
        }
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        stopCamera();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void takePicture() {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {

        }
    };

    private PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            try {
                int w = _camera.getParameters().getPictureSize().width;
                int h = _camera.getParameters().getPictureSize().height;

                bmp = BitmapFactory.decodeByteArray(_data, 0, _data.length);
                bmp2 = BitmapFactory.decodeByteArray(_data, 0, _data.length);

                int[] pix = new int[w * h];
                bmp.getPixels(pix, 0, w, 0, 0, w, h);
                bmp2.getPixels(pix, 0, w, 0, 0, w, h);
                int result = LibImgFun.mySaveImage(pix, w, h, filePath);
                Log.v("crjlog", "resut = " + result + "filePath = " + filePath);
                verifyResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
            if (focused) {
                takePicture();
            } else
                mButton.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NewApi")
    private void initCamera() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.RGB_565);

                mCamera.setDisplayOrientation(180);
                parameters.setRotation(180);

                parameters.setPictureSize(640, 480);
                parameters.setPreviewSize(640, 480);

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopCamera() {

        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                is_camaeraperview = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == 251 || keyCode == 252) && (event.getRepeatCount() == 0) && is_camaeraperview == false) {
            if (!is_camaeraperview) {
                is_camaeraperview = true;
                if (mCamera != null) {
                    mCamera.autoFocus(mAutoFocusCallback);
                }
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 验证指纹后的结果
     */
    private void verifyResult() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.v("TestFile", "SD card is not avaiable/writeable right now.");
            return;
        }
        String bmpFile = getFilesDir().getParent().toString() + "/" + getResources().getString(R.string.app_name) + ".bmp";
        String pgmFile = getFilesDir().getParent().toString() + "/" + getResources().getString(R.string.app_name) + ".pgm";
        String xytFile = getFilesDir().getParent().toString() + "/"
                + getResources().getString(R.string.app_name) + ".xyt";

        long t0 = System.currentTimeMillis();
        long t1 = System.currentTimeMillis();

        CallDecoder cd = new CallDecoder();
        cd.Bmp2Pgm(bmpFile, pgmFile);


        CallFprint cf = new CallFprint();
        cf.pgmChangeToXyt(pgmFile, xytFile);


        audioPlay ap = new audioPlay();
        final int audioType;
        Intent intent = new Intent(CameraOpen.this, MatchResultActivity.class);
        if (null != gatherFinger.fingerPrint_pic_path && !gatherFinger.fingerPrint_pic_path.equals("")) {
            int ret = cf.fprintCompare(xytFile, gatherFinger.fingerPrint_pic_path);
            if (ret >= 16) {
                Log.e("姣旇緝鎸囩汗", "鎸囩汗鍖归厤鎴愬姛!");
                audioType = PLAY_SOUND_FINGER_VERIFICATION;
                intent.putExtra("haveData", true);
            } else {
                Log.e("姣旇緝鎸囩汗", "鎸囩汗鍖归厤澶辫触!");
                Toast.makeText(this, "Verify the fingerprint failed!", Toast.LENGTH_SHORT).show();
                audioType = PLAY_SOUND_FINGER_AUTHENTICATION_FAIL;
                intent.putExtra("haveData", false);
            }
        } else {
            Toast.makeText(this, "No fingerprint data!", Toast.LENGTH_SHORT).show();
            intent.putExtra("haveData", false);
            audioType = PLAY_SOUND_FINGER_AUTHENTICATION_FAIL;
        }

        try {
            ap.PlayAsset(audioType, am);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(CameraOpen.this, MatchResultActivity.class);
        startActivity(intent);
        finish();
    }
}