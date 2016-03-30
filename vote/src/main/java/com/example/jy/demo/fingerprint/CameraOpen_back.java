package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

//import com.mediatek.factorymode.AppDefine;
//import com.mediatek.factorymode.CallDecoder;
//import com.mediatek.factorymode.CallFprint;
//import com.mediatek.factorymode.FactoryModeActivity;
//import com.mediatek.factorymode.R;
//import com.mediatek.factorymode.Utils;
//import com.opencv.LibImgFun;
//
//// add or modify the following code by crj2014/12/19 18:11:48
//import com.mediatek.factorymode.io;
//end by crj

public class CameraOpen_back extends Activity implements SurfaceHolder.Callback {
	/** Called when the activity is first created. */
	private Camera mCamera;
	private ImageButton mButton;
	// private Button returnTomainmenu;
	private SurfaceView mSurfaceView;
	private SurfaceHolder holder;
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private String filePath;
	private boolean is_camaeraperview = false;

	Bundle bundle = null;
	SharedPreferences mSp;

	ProgressDialog MyDialog;
	String TAG = "crjlog";

	private static final String IMAGE_NAME = "Photo";
	private SharedPreferences preferences;
	private String gather_id, gather_name;
	private String filePath3;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.camera_test_main);

		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		//12.19
//		io.IoOpen();


//		filePath = getFilesDir().getParent().toString() + "/"+ getResources().getString(R.string.app_name) + ".bmp";

		mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
		holder = mSurfaceView.getHolder();
		holder.addCallback(CameraOpen_back.this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mButton = (ImageButton) findViewById(R.id.myButton);

		mButton.setOnClickListener(new Button.OnClickListener() {
			// @Override
			public void onClick(View arg0) {
				if(!is_camaeraperview){
					is_camaeraperview = true;
					mCamera.autoFocus(mAutoFocusCallback);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		gather_id = preferences.getString("id", "000001");
		gather_name = preferences.getString("name", "card");
	}
	@SuppressLint("NewApi")
	private int FindFrontCamera(){
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
			if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}

	@SuppressLint("NewApi")
	private int FindBackCamera(){
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
			if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}


	@SuppressLint("NewApi")
	public void surfaceCreated(SurfaceHolder surfaceholder) {
		try {
			//mCamera = mCamera.open();

			int CammeraIndex=FindBackCamera();
			if(CammeraIndex==-1){
				CammeraIndex=FindFrontCamera();
			}
			mCamera = Camera.open(CammeraIndex);


			if (mCamera != null)
				mCamera.setPreviewDisplay(holder);
			else {
				finish();
				Toast.makeText(CameraOpen_back.this, "Open Camera fail !",
						Toast.LENGTH_SHORT).show();
			}

		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,int h) {

		try {
			initCamera();

		} catch (Exception e) {
			// TODO: handle exception
			finish();
			Toast.makeText(CameraOpen_back.this, "Open Camera fail !",
					Toast.LENGTH_SHORT).show();

			if(mCamera != null){
				mCamera.release();
				mCamera = null;
			}
		}
	}

	// @Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {

		Log.v("crjlog", "surfaceDestroyed = ");

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

				bundle = new Bundle();
				bundle.putByteArray("bytes", _data); //将图片字节数据保存在bundle当中，实现数据交换
				saveToSDCard(_data); // 保存图片到sd卡中

				setResult(Activity.RESULT_OK);
				setRequestedOrientation(1);
				finish();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	//	 public static void saveToSDCard(byte[] data) throws IOException {
//	        Date date = new Date();  
//	        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间   
//	        String filename = format.format(date) + ".jpg";  
//	        File fileFolder = new File(Environment.getExternalStorageDirectory()  
//	                + "/Dcs/");  
	public void saveToSDCard(byte[] data) throws IOException {
		String filename = IMAGE_NAME + ".jpg";
		filePath3 = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/";
		File fileFolder = new File(filePath3);
		if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
			fileFolder.mkdir();
		}
		File jpgFile = new File(fileFolder, filename);
		FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
		outputStream.write(data); // 写入sd卡中
		outputStream.close(); // 关闭输出流
	}


	public final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, Camera camera) {
			if (focused) {
				takePicture();
			} else
				mButton.setVisibility(View.VISIBLE);
		}
	};

	@SuppressLint("NewApi")
	private void initCamera() {
		if (mCamera != null) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
//				parameters.setPictureFormat(PixelFormat.RGB_565);
//
				mCamera.setDisplayOrientation(90);
				parameters.setRotation(90);
//
//				parameters.setPictureSize(640, 480);
//				parameters.setPreviewSize(640, 480);

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
		// TODO Auto-generated method stub
		if ((keyCode == 251 || keyCode == 252) && (event.getRepeatCount() == 0) && is_camaeraperview == false) {

			if(!is_camaeraperview){

				is_camaeraperview = true;
				if(mCamera != null)
					mCamera.autoFocus(mAutoFocusCallback);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}


	//12.19
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		io.IoClose(); 
	}

}
