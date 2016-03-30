package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdminMainActivity extends Activity {

	private Button mAdminbutton_User, mAdminbutton_Setting,
			mAdminbutton_DataSend,mAdminbutton_LockVote, mAdminbutton_Exit;
	private Button mAdminbutton_about, mAdminbutton_reset;

	private SharedPreferences preferences;

	// dialog view
	private AlertDialog dlg,DexttextDlg;
	private Button dialog_bt_cancel, dialog_bt_ok;
	private TextView dialog_title, dialog_tv;
	private EditText dialog_et;

	private VoteVin_DBHelper mVoteDB;
	private Cursor mCursor;
	private String VIN_TABLE_NAME;

	private Vote_DBHelper mVoteDB_log;

	public  String filePath = "/databases/Vote.db";
	private int ENTRY_NAME_NUM;
	private String ET, pucode;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_activity_main);

		mVoteDB_log = new Vote_DBHelper(this);
		ENTRY_NAME_NUM = mVoteDB_log.ENTRY_NAME_NUM;


		mVoteDB = new VoteVin_DBHelper(this);
		mCursor = mVoteDB.Query_Vin_table();
		VIN_TABLE_NAME = mVoteDB.VIN_TABLE_NAME;

		mAdminbutton_User = (Button) findViewById(R.id.button_adminUser);
		mAdminbutton_Setting = (Button) findViewById(R.id.button_adminSetting);
		//mAdminbutton_DataSend = (Button) findViewById(R.id.button_adminDataSend);
		mAdminbutton_reset = (Button) findViewById(R.id.button_adminreset);
		mAdminbutton_about = (Button) findViewById(R.id.button_adminabout);
		mAdminbutton_Exit = (Button) findViewById(R.id.button_adminExit);
		mAdminbutton_LockVote = (Button) findViewById(R.id.button_adminLockVote);

		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);

		ET = preferences.getString("CURRENT_ELECTION_TYPE", "President");
		pucode = preferences.getString("PU_CODE", "37-06-01-007");

		mAdminbutton_about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Intent it = new Intent(AdminMainActivity.this,SystemAbout.class);
				Intent it = new Intent(AdminMainActivity.this,Submain.class);
				startActivity(it);
			}
		});

		//12.19
		mAdminbutton_LockVote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				//lockVoteDialogView();
			}
		});


		mAdminbutton_reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				initDialogView();

				dialog_title.setText(R.string.adminmain_text_reset_title);
				dialog_tv.setText(R.string.adminmain_text_reset);

				dialog_bt_ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						if(dlg != null)
							dlg.dismiss();

						initEdittextDialogView();
						dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
						dialog_tv.setText(R.string.adminmain_dialog_title);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {


//								if (dialog_et.getText().length() > 0) {

								if(queryAdminPwd(dialog_et.getText().toString())){

									try {
										dismissEditTextDialogView();
										deleteDate();
										finish();
										Toast.makeText(AdminMainActivity.this,getResources().getString(R.string.adminmain_text_reset_success),Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
										// TODO: handle exception
										Toast.makeText(AdminMainActivity.this,getResources().getString(R.string.adminmain_text_reset_fail),Toast.LENGTH_SHORT).show();
									}

								}else{
									dialog_et.setText("");
									Toast.makeText(AdminMainActivity.this,getResources().getString(R.string.adminmain_dialog_pwd_error),Toast.LENGTH_SHORT).show();

								}


//								}else{
//									
//									Toast.makeText(AdminMainActivity.this,getResources().getString(R.string.adminmain_dialog_title),Toast.LENGTH_SHORT).show();
//								}
							}
						});
					}
				});
			}
		});

		mAdminbutton_User.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent it = new Intent(AdminMainActivity.this, UserQuery.class);
				startActivity(it);

			}
		});

		mAdminbutton_Setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// Intent it = new Intent(AdminMainActivity.this,
				// VinQuery.class);
				// startActivity(it);

				Intent it = new Intent(AdminMainActivity.this,SystemConfig.class);
				startActivity(it);

				// Intent it = new Intent(AdminMainActivity.this, person.class);
				// startActivity(it);
			}
		});

//		mAdminbutton_DataSend.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//
//				// Intent it = new Intent(AdminMainActivity.this,
//				// DataEntry.class);
//				// startActivity(it);
//
//				Intent it = new Intent(AdminMainActivity.this, DataSend.class);
//				startActivity(it);
//
//			}
//		});

		mAdminbutton_Exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				exitDialogView();
			}
		});

	}

	// 判断Admin密码
	private boolean queryAdminPwd(String pwd) {

		mCursor = mVoteDB_log.query(mVoteDB_log.USER_TABLE_NAME, null, mVoteDB_log.USER_NAME+"=?",
				new String[] {mVoteDB_log.ADMIN_USER_NAME}, null, null, null);

		if (mCursor != null)
			mCursor.moveToFirst();

		if (pwd.equals(mCursor.getString(2))) {

			return true;
		}

		return false;

	}

	private void initEdittextDialogView() {

		DexttextDlg = new AlertDialog.Builder(AdminMainActivity.this).create();
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				ENTRY_NAME_NUM) });
		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() >= ENTRY_NAME_NUM) {
					Toast.makeText(
							AdminMainActivity.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_NAME_NUM, Toast.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				try {
					String temp = arg0.toString();
					String tem = temp.substring(temp.length()-1, temp.length());
					char[] temC = tem.toCharArray();
					int mid = temC[0];

					if(mid>=48&&mid<=57){//数字
						return;
					}
					if(mid>=65&&mid<=90){//大写字母
						return;
					}
					if(mid>=97&&mid<=122){//小写字母
						return;
					}
					arg0.delete(temp.length()-1, temp.length());
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});

		dialog_et.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

		dialog_bt_ok = (Button) window
				.findViewById(R.id.edittext_dialog_button_ok);
		dialog_bt_cancel = (Button) window
				.findViewById(R.id.edittext_dialog_button_cancel);

		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissEditTextDialogView();
			}
		});

	}

	private void dismissEditTextDialogView() {

		if(DexttextDlg != null)
			DexttextDlg.dismiss();

	}


	private void initDialogView() {

		dlg = new AlertDialog.Builder(AdminMainActivity.this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.theme_dialog_text);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window.findViewById(R.id.text_dialog_title);
		dialog_tv = (TextView) window.findViewById(R.id.text_dialog_tv);

		dialog_bt_ok = (Button) window.findViewById(R.id.text_dialog_button_ok);
		dialog_bt_cancel = (Button) window
				.findViewById(R.id.text_dialog_button_cancel);

		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(dlg != null)
					dlg.dismiss();
			}
		});
	}

	//12.19
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		is_LockVoteStatus();
	}

	private void is_LockVoteStatus() {

		Boolean is_login = preferences.getBoolean("is_lockVote", false);

		if(is_login){
			mAdminbutton_LockVote.setBackgroundResource(R.drawable.mainbutton_voteclose);
		}else{
			mAdminbutton_LockVote.setBackgroundResource(R.drawable.mainbutton_voteopen);
		}
	}

	private void LockVoteStatus(boolean status) {

		Editor editor = preferences.edit();
		editor.putBoolean("is_lockVote", status);
		// 提交更改
		editor.commit();

	}

	private void lockVoteDialogView() {

		initDialogView();

		try {

			dialog_title.setText(R.string.screen_mainmenu_dialog_title);

			Boolean is_login = preferences.getBoolean("is_lockVote", false);

			if(is_login){

				dialog_tv.setText(R.string.screen_login_LockVote_dialog_open);

				dialog_bt_ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						mAdminbutton_LockVote.setBackgroundResource(R.drawable.mainbutton_voteopen);

						LockVoteStatus(false);

						if(dlg != null)
							dlg.dismiss();
					}
				});

			}else{
				dialog_tv.setText(R.string.screen_login_LockVote_dialog_close);

				dialog_bt_ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mAdminbutton_LockVote.setBackgroundResource(R.drawable.mainbutton_voteclose);

						LockVoteStatus(true);

						if(dlg != null)
							dlg.dismiss();
					}
				});
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void exitDialogView() {

		initDialogView();
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//		String nowTime = format.format(new java.util.Date());
//		Log.v("crjlog", "date = " + nowTime);

		try {
//			// get Sum
//			mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "date=?",
//					new String[] { nowTime }, null, null, null);
//
//			if (mCursor != null)
//				mCursor.moveToFirst();
//			int Sum = mCursor.getCount();
//			mCursor.close();
//
//			// get success num
//			mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "date=?"
//					+ "and status=?", new String[] { nowTime, "Y" }, null,
//					null, null);
//			if (mCursor != null)
//				mCursor.moveToFirst();
//			int num_s = mCursor.getCount();
//			mCursor.close();
//
//			// get success num
//			mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "date=?"
//					+ "and status=?", new String[] { nowTime, "N" }, null,
//					null, null);
//			if (mCursor != null)
//				mCursor.moveToFirst();
//			int num_f = mCursor.getCount();
//			mCursor.close();

//			dialog_title.setText(R.string.screen_login_exit_dialog);
			dialog_title.setText(R.string.screen_mainmenu_dialog_title);

//			String aa = (getResources().getString(
//					R.string.screen_login_exit_text)
//					+ Sum
//					+ "\n"
//					+ getResources().getString(
//							R.string.screen_login_exit_text_s)
//					+ num_s
//					+ "\n"
//					+ getResources().getString(
//							R.string.screen_login_exit_text_f) + num_f);

//			dialog_tv.setText(aa);
			dialog_tv.setText(R.string.screen_login_exit_dialog);

			dialog_bt_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					finish();
					Editor editor = preferences.edit();
					editor.putBoolean("is_login", false);
					// 提交更改
					editor.commit();

					// backups
					if(Environment.isExternalStorageRemovable()){

						Databackups();

						Databackups_intern();
					}else{

						Databackups();
					}

					//系统 日志 登出
					mVoteDB_log.insert_syslogtable(preferences.getString("last_login_username","Admin"),getResources().getString(R.string.System_Log_event_logout));

				}
			});
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(
					AdminMainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup_fail),
					Toast.LENGTH_SHORT).show();
		}
	}

	//字节流加解密
	public static int dataEncDec(byte[] b, int v) {
		int nRet = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i] != 0 && b[i] != (byte) v) {
				b[i] ^= v;
			}
		}
		return nRet;
	}

	// backups to neizhi
	private void Databackups_intern() {
		try {
			File oldfile = new File("/storage/sdcard1/Vote");

			if (!oldfile.exists()) {
				oldfile.mkdirs();
			}

			InputStream mInputStream = new FileInputStream(getFilesDir()
					.getParent().toString() + filePath);

			String newPath =  "/storage/sdcard1/Vote/"
					+ pucode + "_" + ET + "_"
					+ "Vote.db";


			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1024];
			int bytesum = 0;
			int byteread = 0;
			while ((byteread = mInputStream.read(buffer)) != -1) {

				dataEncDec(buffer, 3);
				bytesum += byteread; // 字节数 文件大小
				System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
			mInputStream.close();
			Toast.makeText(
					AdminMainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup),
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					AdminMainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup_fail),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	// backups
	private void Databackups() {
		try {
			File oldfile = new File(Environment.getExternalStorageDirectory()
					.toString()
					+ "/"
					+ getResources().getString(R.string.app_name));

			if (!oldfile.exists()) {
				oldfile.mkdirs();
			}

			InputStream mInputStream = new FileInputStream(getFilesDir()
					.getParent().toString() + filePath);

			String newPath = Environment.getExternalStorageDirectory()
					.toString()
					+ "/"
					+ getResources().getString(R.string.app_name)
					+ "/"
					+ pucode + "_" + ET + "_"
					+ getResources().getString(R.string.app_name) + ".db";

			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1024];
			int bytesum = 0;
			int byteread = 0;
			while ((byteread = mInputStream.read(buffer)) != -1) {

				dataEncDec(buffer, 3);
				bytesum += byteread; // 字节数 文件大小
				System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
			mInputStream.close();
			Toast.makeText(
					AdminMainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup),
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					AdminMainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup_fail),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void deleteDate() {
		String Path1 = getFilesDir().getParent().toString();
		File oldfile2 = new File(Path1);
		delete(oldfile2);
		Log.v("crjlog", "oldfile2 = " + oldfile2);

		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();


		if(Environment.isExternalStorageRemovable()){

			String fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
			File fileText = new File(fileName);
			delete(fileText);

			String fileName2 = "/storage/sdcard1/Vote";
			File fileText2 = new File(fileName2);
			delete(fileText2);


		}else{

			String fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
			File fileText = new File(fileName);
			delete(fileText);

		}

	}

	public void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		exitDialogView();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (dlg != null)
			dlg.dismiss();

		if(mCursor != null)
			mCursor.close();


	}

}
