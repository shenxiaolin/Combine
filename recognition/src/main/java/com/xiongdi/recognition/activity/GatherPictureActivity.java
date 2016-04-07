package com.xiongdi.recognition.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.xiongdi.recognition.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by moubiao on 2016/3/23.
 * 拍照的activity
 */
public class GatherPictureActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private SurfaceView previewSFV;
    private ImageButton takeBT;

    private Camera mCamera;
    private SurfaceHolder holder;

    private String gatherID;
    private String pictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gather_picture_layout);

        initView();
        setListener();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        previewSFV = (SurfaceView) findViewById(R.id.picture_SurfaceView);
        holder = previewSFV.getHolder();
        holder.addCallback(GatherPictureActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        takeBT = (ImageButton) findViewById(R.id.take_picture_bt);
    }

    private void setListener() {
        takeBT.setOnClickListener(this);
    }

    private void initData() {
        Intent data = getIntent();
        gatherID = data.getStringExtra("pictureName");
    }


    private boolean focus = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_picture_bt:
                if (focus) {
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        focus = success;
                        if (success) {
                            mCamera.cancelAutoFocus();
                            mCamera.takePicture(new Camera.ShutterCallback() {
                                @Override
                                public void onShutter() {
                                }
                            }, null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    savePicture(data);
                                    Intent intentData = new Intent();
                                    intentData.putExtra("pictureUrl", pictureUrl);
                                    setResult(Activity.RESULT_OK, intentData);
                                    finish();
                                }
                            });
                        }
                    }
                });


                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
        setCameraParams();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void initCamera() {
        try {
            mCamera = Camera.open(0);//1:采集指纹的摄像头. 0:拍照的摄像头.
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            Toast.makeText(this, "camera open failed!", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
    }

    private void setCameraParams() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            mCamera.setDisplayOrientation(180);
            parameters.setRotation(180);

            parameters.setPictureSize(480, 320);
            parameters.setPreviewSize(480, 320);

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void savePicture(byte[] data) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String pictureName = gatherID + ".jpg";
            String savePath = getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/" + gatherID + "/";
            pictureUrl = savePath + pictureName;
            File saveFolder = new File(savePath);
            if (!saveFolder.exists()) {
                saveFolder.mkdirs();
            }
            File pictureFile = new File(saveFolder, pictureName);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
