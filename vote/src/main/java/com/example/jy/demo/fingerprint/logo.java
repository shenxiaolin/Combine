package com.example.jy.demo.fingerprint;


import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.io.io;
public class logo extends Activity {

	private ImageView logoview;

	private Vote_DBHelper mVoteDB;

	private Cursor mCursor_user;
	private String ADMIN_USER_NAME;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);

		com.io.io.IoClose();
		logoview = (ImageView) this.findViewById(R.id.logo_bg);
		AlphaAnimation logoani = new AlphaAnimation(0.1f, 1.0f);
		logoani.setDuration(4000);
		logoview.setAnimation(logoani);

		logoani.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

//				try {
//					// create vote.db
//					createVoteDB();
//
//					String LogoFilePath = "/data/data/com.example.jy.demo.fingerprint/pic1.jp2";
//					InputStream is = getApplicationContext().getResources()
//							.openRawResource(R.drawable.pic1);
//
//					FileOutputStream fos = new FileOutputStream(LogoFilePath);
//
//					byte[] buffer = new byte[8192];
//					System.out.println("3");
//					int count = 0;
//
//					// 开始复制Logo图片文件
//					while ((count = is.read(buffer)) > 0) {
//						fos.write(buffer, 0, count);
//						System.out.println("4");
//					}
//					fos.close();
//					is.close();
//
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				// 三秒之后跳出
				Intent it = new Intent(logo.this, login.class);
				startActivity(it);

//				Intent it = new Intent(logo.this, login_ext.class);
//				startActivity(it);

				// 三秒之后 这个窗口就没用了 应该finish
				finish();
			}
		});
	}

	// create vote.db
	private void createVoteDB() {
		mVoteDB = new Vote_DBHelper(this);
		ADMIN_USER_NAME = mVoteDB.ADMIN_USER_NAME;
		mCursor_user = mVoteDB.Query_User_table();
		if (mCursor_user.getCount() == 0) {// 默认第一次写入一条admin的记录
			mVoteDB.insert_usertable(ADMIN_USER_NAME,"8888",0);
			mVoteDB.insert_usertable("userc","123",1);
			mVoteDB.insert_usertable("userv","123",2);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		createVoteDB();
		//close nfc
		mSendBroadcast_close();
		String filePath3 = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
		File fileFolder = new File(filePath3);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(mCursor_user != null)
			mCursor_user.close();
	}

	public void mSendBroadcast_close() {

		Intent intent = new Intent();
		intent.setAction("nfc.action.close");
		//发送 一个无序广播
		logo.this.sendBroadcast(intent);
	}

}
