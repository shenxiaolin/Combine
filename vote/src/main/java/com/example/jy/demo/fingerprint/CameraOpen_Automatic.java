package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.jy.demo.fingerprint.R;
import com.opencv.LibImgFun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.view.MotionEvent;
import android.os.Handler;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.TextView;

public class CameraOpen_Automatic extends Activity implements SurfaceHolder.Callback {
	/** Called when the activity is first created. */
	private Camera mCamera;
	private ImageButton mButton;

	private TextView mtextview;
	// private Button returnTomainmenu;
	private SurfaceView mSurfaceView;
	private SurfaceHolder holder;
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private Bitmap bmp = null;
	private Bitmap bmp2 = null;
	private int count;
	private String filePath,filePath2;

	private boolean is_camaeraperview = false;
	Handler mhandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		/* 隐藏标题栏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/* 设定屏幕显示为横向 */
		// this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.camera_automatic_main);
		mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
		mSurfaceView.setBackgroundColor(Color.WHITE);
		mButton = (ImageButton) findViewById(R.id.myButton);
		mtextview = (TextView) findViewById(R.id.mytextview);
		mhandler = new Handler();
		/* 拍照Button的事件处理 */
		mButton.setOnClickListener(new Button.OnClickListener() {
			// @Override
			public void onClick(View arg0) {
				/* 关闭闪光灯并拍照 */
				if(!is_camaeraperview){
					is_camaeraperview = true;
					mCamera.autoFocus(mAutoFocusCallback);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		filePath = getFilesDir().getParent().toString() + "/"
				+ getResources().getString(R.string.app_name) + ".bmp";

//		filePath2 = getFilesDir().getParent().toString() + "/"
//				+ getResources().getString(R.string.app_name) + "2" + ".bmp"; 

		holder = mSurfaceView.getHolder();




		holder.addCallback(CameraOpen_Automatic.this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		/* 打开相机 */
		try {
			mCamera = mCamera.open(1);
		} catch (Exception e) {
			// TODO: handle exception
			finish();
			Toast.makeText(CameraOpen_Automatic.this, "Open Camera fail !",
					Toast.LENGTH_SHORT).show();
		}
		if(mtextview != null){
			mtextview.setText(R.string.press_fingerprint);
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
				Toast.makeText(CameraOpen_Automatic.this, "Open Camera fail !",
						Toast.LENGTH_SHORT).show();
			}

		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,int h) {

		try {
			/* 相机初始化 */
			initCamera();
			count++;

		} catch (Exception e) {
			// TODO: handle exception
			finish();
			Toast.makeText(CameraOpen_Automatic.this, "Open Camera fail !",
					Toast.LENGTH_SHORT).show();

			if(mCamera != null){
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		//mhandler.removeCallbacks(capture);
		stopCamera();
		surfaceDestroyed(holder);
		if (mCamera != null) {

			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;

		}
	}

	// @Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		if (surfaceholder != null) {
			surfaceholder.getSurface().release();

			surfaceholder.removeCallback(CameraOpen_Automatic.this);
			surfaceholder = null;
		}
	}

	private void takePicture() {
		if (mCamera != null) {
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			/* 按兀快门瞬间会呼?这里的程序 */
		}
	};

	private PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			/* 要处理raw data?写?否 */
		}
	};

	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			/* 取得相片 */
			try {

				int w = _camera.getParameters().getPictureSize().width;
				int h = _camera.getParameters().getPictureSize().height;

				bmp = BitmapFactory.decodeByteArray(_data, 0, _data.length);
				bmp2 = BitmapFactory.decodeByteArray(_data, 0, _data.length);

				int[] pix = new int[w * h];
				bmp.getPixels(pix, 0, w, 0, 0, w, h);
				bmp2.getPixels(pix, 0, w, 0, 0, w, h);
				// int resut = LibImgFun.Eyes(pix, w, h);
				int resut = LibImgFun.mySaveImage(pix, w, h, filePath);
//				int resut2 = LibImgFun.mySaveImage2(pix, w, h, filePath2);
				Log.v("crjlog", "resut = " + resut + "filePath = " + filePath);
				setResult(Activity.RESULT_OK);
				finish();
				/* 取得相片Bitmap对象 */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, Camera camera) {
			/* 对到焦点拍照 */

			is_camaeraperview = true;
			if (focused) {
				takePicture();
			} else
				mButton.setVisibility(View.VISIBLE);
		}
	};

	/* 相机初始化的method */
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

				// parameters.set("rotation",180);
				// parameters.setPictureSize(256, 360);
				// parameters.setPreviewSize(256, 360);

				// parameters.setPictureSize(360, 256);
				// parameters.setPreviewSize(360, 256);

				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* 停止相机的method */
	private void stopCamera() {

		if (mCamera != null) {
			try {
				/* 停止预览 */
				mCamera.stopPreview();
				is_camaeraperview = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    /*private final Runnable capture = new Runnable() {
	    @Override
	    public void run() {
	    
			if(mCamera != null)
			mCamera.autoFocus(mAutoFocusCallback);
	    }
 	};*/

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if(keyCode == 27){
			mSurfaceView.setBackgroundColor(Color.WHITE);
			if(mtextview != null){
				mtextview.setText(R.string.press_fingerprint);
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == 27 ) {
			Log.d("huangmin","is_camaeraperview= "+is_camaeraperview);
			if(mtextview != null){
				mtextview.setText("");
			}
			mSurfaceView.setBackgroundColor(Color.TRANSPARENT);
			if(event.getRepeatCount() == 25){
				if(mCamera != null)
					mCamera.autoFocus(mAutoFocusCallback);
				//mhandler.postDelayed(capture,1000);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

}
