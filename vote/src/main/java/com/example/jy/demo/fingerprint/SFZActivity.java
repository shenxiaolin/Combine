package com.example.jy.demo.fingerprint;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cr30a.asynctask.AsyncParseSFZ;
import com.cr30a.asynctask.AsyncParseSFZ.OnReadSFZListener;
import com.cr30a.logic.ParseSFZAPI;
import com.cr30a.logic.ParseSFZAPI.People;
import com.cr30a.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SFZActivity extends Activity implements OnClickListener {
	private TextView sfz_name;
	private TextView sfz_sex;
	private TextView sfz_nation;
	private TextView sfz_year;
	private TextView sfz_mouth;
	private TextView sfz_day;
	private TextView sfz_address;
	private TextView sfz_id;
	private ImageView sfz_photo;

	private Button read_button;
	private Button clear_button;
	private Button sequential_read;
	private Button stop;
	private TextView resultInfo;

	private ProgressDialog progressDialog;

	private MyApplication application;

	private People people;

	private AsyncParseSFZ asyncParseSFZ;

	private Bitmap bitmap;

	private static final int FIND_SUCCESS = 0;
	private static final int FIND_FAIL = 1;

	private int readTime = 0;
	private int readFailTime = 0;
	private int readSuccessTime = 0;
	/**
	 * 是否是连续读取
	 */
	private boolean isSequentialRead = false;

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sfz_activity);
		initView();
		initData();
	}

	private void initView() {
		sfz_name = ((TextView) findViewById(R.id.sfz_name));
		sfz_nation = ((TextView) findViewById(R.id.sfz_nation));
		sfz_sex = ((TextView) findViewById(R.id.sfz_sex));
		sfz_year = ((TextView) findViewById(R.id.sfz_year));
		sfz_mouth = ((TextView) findViewById(R.id.sfz_mouth));
		sfz_day = ((TextView) findViewById(R.id.sfz_day));
		sfz_address = ((TextView) findViewById(R.id.sfz_address));
		sfz_id = ((TextView) findViewById(R.id.sfz_id));
		sfz_photo = ((ImageView) findViewById(R.id.sfz_photo));

		read_button = ((Button) findViewById(R.id.read_sfz));
		clear_button = ((Button) findViewById(R.id.clear_sfz));
		sequential_read = ((Button) findViewById(R.id.sequential_read));
		stop = ((Button) findViewById(R.id.stop));
		resultInfo = ((TextView) findViewById(R.id.resultInfo));

		read_button.setOnClickListener(this);
		clear_button.setOnClickListener(this);
		sequential_read.setOnClickListener(this);
		stop.setOnClickListener(this);
	}

	private void initData() {
		application = (MyApplication) this.getApplicationContext();
		asyncParseSFZ = new AsyncParseSFZ(application.getHandlerThread()
				.getLooper(), application.getChatService());

		asyncParseSFZ.setOnReadSFZListener(new OnReadSFZListener() {
			@Override
			public void onReadSuccess(People people) {
				cancleProgressDialog();
				updateInfo(people);
				readSuccessTime++;
				SFZActivity.this.people = people;
				refresh(isSequentialRead);
			}

			@Override
			public void onReadFail(int confirmationCode) {
				cancleProgressDialog();
				readFailTime++;
				if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.no_found_card_no_data));
				} else if (confirmationCode == ParseSFZAPI.Result.TIME_OUT) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.no_card_time_out));
				} else if (confirmationCode == ParseSFZAPI.Result.OTHER_EXCEPTION) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.other_exceptions));
				}
				refresh(isSequentialRead);
			}

		});
		asyncParseSFZ.send2();

	}


	private void refresh(boolean isSequentialRead) {
		if (!isSequentialRead) {
			return;
		}
		mHandler.postDelayed(task, 1000);
		String result =getResources().getString(R.string.total) + readTime + getResources().getString(R.string.success) + readSuccessTime
				+ getResources().getString(R.string.fail) + readFailTime;
		Log.i("whw", "result=" + result);
		resultInfo.setText(result);
	}

	@Override
	public void onClick(View v) {
		if (!application.isConnect()) {
			ToastUtil.showToast(this, getResources().getString(R.string.bluetooth_no_connetion));
			return;
		}
		int id = v.getId();
		switch (id) {
			case R.id.read_sfz:
				resultInfo.setText("");
				isSequentialRead = false;
				showProgressDialog(getResources().getString(R.string.reading));
				asyncParseSFZ.readSFZ();
				Log.i("whw", "read_sfz");
				break;
			case R.id.clear_sfz:
				clear();
				// int temp =
				// application.getmChatService().getNewAsyncVersion().getMachineVersion();
				// Log.i("whw", "temp="+temp);
				break;
			case R.id.sequential_read:
				isSequentialRead = true;
				readTime = 0;
				readFailTime = 0;
				readSuccessTime = 0;
				mHandler.post(task);
				break;
			case R.id.stop:
				mHandler.removeCallbacks(task);
				break;
			default:
				break;
		}

	}

	private Runnable task = new Runnable() {
		@Override
		public void run() {
			if (!application.isConnect()) {
				ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.bluetooth_no_connetion));
				mHandler.removeCallbacks(this);
				return;
			}
			readTime++;
			showProgressDialog(getResources().getString(R.string.reading));
			Log.i("whw", "asyncParseSFZ.readSFZ()！");
			asyncParseSFZ.readSFZ();
		}
	};

	@Override
	protected void onDestroy() {
		Log.i("whw", "SFZActivity onDestroy");
		mHandler.removeCallbacks(task);
		super.onDestroy();
	}

	private void updateInfo(People people) {
		sfz_address.setText(people.getPeopleAddress());
		sfz_day.setText(people.getPeopleBirthday().substring(6));
		sfz_id.setText(people.getPeopleIDCode());
		sfz_mouth.setText(people.getPeopleBirthday().substring(4, 6));
		sfz_name.setText(people.getPeopleName());
		sfz_nation.setText(people.getPeopleNation());
		sfz_sex.setText(people.getPeopleSex());
		sfz_year.setText(people.getPeopleBirthday().substring(0, 4));
		Bitmap photo = BitmapFactory.decodeByteArray(people.getPhoto(), 0,
				people.getPhoto().length);
		if (bitmap != null && bitmap.isRecycled()) {
			bitmap.recycle();
		}
		bitmap = photo;
		sfz_photo.setBackgroundDrawable(new BitmapDrawable(photo));
	}

	private void clear() {
		sfz_address.setText("");
		sfz_day.setText("");
		sfz_id.setText("");
		sfz_mouth.setText("");
		sfz_name.setText("");
		sfz_nation.setText("");
		sfz_sex.setText("");
		sfz_year.setText("");
		sfz_photo.setBackgroundColor(0);
		this.people = null;
	}

	private void showProgressDialog(String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(message);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

}
