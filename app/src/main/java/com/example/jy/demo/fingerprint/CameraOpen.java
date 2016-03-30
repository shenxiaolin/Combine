package com.example.jy.demo.fingerprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
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

import com.example.jy.demo.passport.R;
import com.opencv.LibImgFun;

import java.io.IOException;

public class CameraOpen extends Activity implements SurfaceHolder.Callback {
	/** Called when the activity is first created. */
	private Camera mCamera;
	private ImageButton mButton;
	// private Button returnTomainmenu;
	private SurfaceView mSurfaceView;
	private SurfaceHolder holder;
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private Bitmap bmp = null;
	private Bitmap bmp2 = null;
	private int count;
	private String filePath,filePath2;
	
	private boolean is_camaeraperview = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gather_fingerprint);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
		mButton = (ImageButton) findViewById(R.id.myButton);

		mButton.setOnClickListener(new Button.OnClickListener() {
			// @Override
			public void onClick(View arg0) {
				/* �ر�����Ʋ����� */
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

	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,int h) {
		
		try {
			initCamera();
			count++;
			
		} catch (Exception e) {
			// TODO: handle exception
			finish();
			Toast.makeText(CameraOpen.this, "Open Camera fail !",
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
		
		stopCamera();
		
		if (mCamera != null) {
			
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			
			surfaceDestroyed(holder);
		}
	}

	// @Override
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
				int resut = LibImgFun.mySaveImage(pix, w, h, filePath);
				Log.v("crjlog", "resut = " + resut + "filePath = " + filePath);
				setResult(Activity.RESULT_OK);
				finish();
			} catch (Exception e) {
				e.printStackTrace();
				

			}
		}
	};

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

}