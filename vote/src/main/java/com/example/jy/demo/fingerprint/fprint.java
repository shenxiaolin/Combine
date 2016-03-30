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

			String sdStatus = Environment.getExternalStorageState();// ��ȡsd��·��
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����
				Log.v("crjlog", "SD card is not avaiable/writeable right now.");
				return;
			}

			Bundle bundle = data.getExtras();
			Bitmap bitmap = (Bitmap) bundle.get("data");// ��ȡ������ص����ݣ���ת��ΪBitmapͼƬ��ʽ
			FileOutputStream outStream = null;
			File file = new File("/sdcard/myImage/");// ������Ƭ��ŵ�λ��
			if (!file.exists()) {
				file.mkdirs();// �����ļ���
			}
			String fileName = "/sdcard/myImage/33333.bmp";// �����ļ�����

			try {
				outStream = new FileOutputStream(fileName);// ����·���ļ��������
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);// ������д���ļ�

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

			logoview.setImageBitmap(bitmap);// ��ͼƬ��ʾ��ImageView��

		}

	}

}
