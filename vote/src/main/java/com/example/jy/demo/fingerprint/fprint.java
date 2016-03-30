package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;

public class fprint extends Activity {

	private ImageView logoview;
	private Button mbutton, mbuttonback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fprint);

		logoview = (ImageView) this.findViewById(R.id.fprintimage);

		mbutton = (Button) this.findViewById(R.id.buttonfprint);

		mbuttonback = (Button) this.findViewById(R.id.buttonback);

		mbuttonback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 1);

			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {

			String sdStatus = Environment.getExternalStorageState();// 获取sd卡路径
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
				Log.v("crjlog", "SD card is not avaiable/writeable right now.");
				return;
			}

			Bundle bundle = data.getExtras();
			Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
			FileOutputStream outStream = null;
			File file = new File("/sdcard/myImage/");// 创建照片存放的位置
			if (!file.exists()) {
				file.mkdirs();// 创建文件夹
			}
			String fileName = "/sdcard/myImage/33333.bmp";// 创建文件名称

			try {
				outStream = new FileOutputStream(fileName);// 创建路径文件的输出流
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);// 把数据写入文件

				Log.v("crjlog", "onActivityResult");

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

			logoview.setImageBitmap(bitmap);// 将图片显示在ImageView里

		}

	}

}
