package com.example.jy.demo.fingerprint;

//import com.example.rftest.R;

import com.example.jy.demo.fingerprint.R;
import com.example.jy.demo.fingerprint.R.id;
import com.example.jy.demo.fingerprint.R.layout;
import com.example.jy.demo.fingerprint.R.menu;
import com.example.jy.demo.fingerprint.R.string;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity_rftest extends Activity {
	private final String TAG = "MainMenu activity";
	private Button btn_simtest, btn_rftest, btn_fprinttest,btn_back;
	private Intent mintent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_rftest);

		btn_simtest = (Button) this.findViewById(R.id.button_simtest);
		btn_rftest = (Button) this.findViewById(R.id.button_rftest);
		btn_fprinttest = (Button) this.findViewById(R.id.button_fprinttest);
		btn_back = (Button) this.findViewById(R.id.button_back);

		btn_simtest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				mintent = new Intent(MainActivity_rftest.this, SimTestActivity.class);
				Toast.makeText(arg0.getContext(), "Begin sim test !", Toast.LENGTH_SHORT).show();
				startActivity(mintent);
//				finish();					
			}
		});


		btn_rftest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mintent = new Intent(MainActivity_rftest.this, RfidTestActivity.class);
				Toast.makeText(arg0.getContext(), "Begin rfid test !", Toast.LENGTH_SHORT).show();
				startActivity(mintent);
			}
		});


		btn_fprinttest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//自定义相机		yjh 2014.04.22
				Intent it = new Intent(MainActivity_rftest.this, CameraOpen.class);
				startActivityForResult(it, 1);

			}
		});

		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}


	// 相机确认返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String info = null;
		int fMatch = 0;

//		EMgpio.SetGpioOutput(85);	//yjh 2014.03.10PM
//		EMgpio.SetGpioDataLow(85);
//		EMgpio.GPIOUnInit();

		info = "fprint Not match !";
		if (resultCode == Activity.RESULT_OK) {	//系统相机按了 "勾" 确认返回

			String sdStatus = Environment.getExternalStorageState();// 获取sd卡路径
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
				Log.v("TestFile",
						"SD card is not avaiable/writeable right now.");
				return;
			}


			//自定义相机返回		yjh 2014.04.22
//			String filePath = Environment.getExternalStorageDirectory().toString() + "/"
//					+ getResources().getString(R.string.app_name) + "/"
//					+ getResources().getString(R.string.app_name) + ".bmp";
//			
//			String fileName = Environment.getExternalStorageDirectory().toString() + "/"
//					+ getResources().getString(R.string.app_name) + "/"
//					+ getResources().getString(R.string.app_name) + ".bmp";

			String bmpFile = getFilesDir().getParent().toString() + "/"
					+ getResources().getString(R.string.app_name) + ".bmp";
			String pgmFile = getFilesDir().getParent().toString() + "/"
					+ getResources().getString(R.string.app_name) + ".pgm";
			String xytFile = getFilesDir().getParent().toString() + "/"
					+ getResources().getString(R.string.app_name) + ".xyt";
//			FileInputStream fis;

			long t0 = System.currentTimeMillis();
			long t1 = System.currentTimeMillis();

			CallDecoder cd = new CallDecoder();
//			cd.Bmp2Pgm("/sdcard/myImage/myImage.bmp", "/sdcard/myImage/Vote.pgm");
			cd.Bmp2Pgm(bmpFile, pgmFile);


			CallFprint cf = new CallFprint();
//			cf.pgmChangeToXyt("/sdcard/myImage/Vote.pgm", "/sdcard/myImage/Vote.xyt");
			cf.pgmChangeToXyt(pgmFile, xytFile);

			Toast.makeText(MainActivity_rftest.this, "Finger .xyt date saved in [" + xytFile + "]", Toast.LENGTH_SHORT).show();
		}
		else
		{
			;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
