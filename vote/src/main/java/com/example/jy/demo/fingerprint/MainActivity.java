package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.gsm.GsmCellLocation;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.io.io;

public class MainActivity extends Activity {

	private Button mbuttonPerson, mbuttonDataEntry, mbuttonDataQuery,
			mbuttonDataTransmit, mbuttonAbout, mbuttonLockVote,mbuttonExit;
	private SharedPreferences preferences;

	// dialog view
	private AlertDialog dlg;
	private Button dialog_bt_cancel, dialog_bt_ok;
	private TextView dialog_title, dialog_tv;
	private EditText dialog_et;

	private VoteVin_DBHelper mVoteDB;
	private Cursor mCursor;
	private String VIN_TABLE_NAME;

	private Vote_DBHelper mVoteDB_log;

	public static final String filePath = "/databases/Vote.db";

	private SimpleDateFormat SystemDateTimeformat, VoteDateTimeformat;
	private String SYSTEMCONFIG_DATE_FORMAT = "yyyy-MM-dd,HH:mm";
	private String SYSTEMCONFIG_VOTE_DATETIME_FORMAT = "yyyyMMddHHmm";

	private String ET, pucode;
	private int ENTRY_NAME_NUM;
	private String changePwd_username;
	private Long changePwd_pwd;

	private Cursor mCursor_user;

	private String USER_TABLE_NAME, ADMIN_USER_NAME;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mVoteDB_log = new Vote_DBHelper(this);
		mVoteDB = new VoteVin_DBHelper(this);
		mCursor = mVoteDB.Query_Vin_table();

		VIN_TABLE_NAME = mVoteDB.VIN_TABLE_NAME;
		ENTRY_NAME_NUM = mVoteDB_log.ENTRY_NAME_NUM;
		ADMIN_USER_NAME = mVoteDB_log.ADMIN_USER_NAME;
		USER_TABLE_NAME = mVoteDB_log.USER_TABLE_NAME;

		mbuttonPerson = (Button) findViewById(R.id.button_mPerson);
		//mbuttonDataEntry = (Button) findViewById(R.id.button_mDataEntry);
		mbuttonAbout = (Button) findViewById(R.id.button_mAbout);
		
		mbuttonLockVote = (Button) findViewById(R.id.button_lockvote);

		mbuttonDataQuery = (Button) findViewById(R.id.button_mQuery);
		mbuttonDataTransmit = (Button) findViewById(R.id.button_mDataTransmit);

		mbuttonExit = (Button) findViewById(R.id.button_mExit);
		preferences = this.getSharedPreferences(
				getResources().getString(R.string.SystemConfig_sp),
				MODE_PRIVATE);

		SystemDateTimeformat = new SimpleDateFormat(SYSTEMCONFIG_DATE_FORMAT);
		VoteDateTimeformat = new SimpleDateFormat(
				SYSTEMCONFIG_VOTE_DATETIME_FORMAT);

		ET = preferences.getString("CURRENT_ELECTION_TYPE", "President");
		pucode = preferences.getString("PU_CODE", "34-16-10-003");
		
		changePwd_username = preferences.getString("last_login_username", "President");
		
		
		mbuttonPerson.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// 判断时间
//				try {
//
//					long dateBegin = VoteDateTimeformat.parse(
//							preferences.getString("VOTE_BEGIN_DATETIME",
//									"201001010000")).getTime();
//					long dateEnd = VoteDateTimeformat.parse(
//							preferences.getString("VOTE_END_DATETIME",
//									"201601010000")).getTime();
//					long NowTime = System.currentTimeMillis();
//
//					if (NowTime - dateBegin >= 0 && dateEnd - NowTime >= 0) {
//
//						Intent it = new Intent(MainActivity.this, person.class);
//						startActivity(it);
//
//					} else {
//
//						Toast.makeText(
//								MainActivity.this,
//								getResources().getString(
//										R.string.mainmenu_comparedate_toast),
//								Toast.LENGTH_SHORT).show();
//
//					}
//
//				} catch (ParseException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				
				
				//Gather
				Intent it = new Intent(MainActivity.this, gatherMain.class);
				startActivity(it);

			}
		});

		mbuttonDataQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Intent it = new Intent(MainActivity.this, VinQuery.class);
//				startActivity(it);
				
				Intent it = new Intent(MainActivity.this, DataSend.class);
				startActivity(it);

			}
		});

		
		//12.19
		mbuttonLockVote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				//sacn
				
	              Intent intent = new Intent();    
	              intent.setClassName("com.google.zxing.client.android",    
	                      "com.google.zxing.client.android.CaptureActivity");      
	              startActivityForResult(intent, 1);
				
//				Boolean is_login = preferences.getBoolean("is_lockVote", false);
//				
//				if(is_login){
//					Toast.makeText(MainActivity.this,getResources().getString(R.string.personscreen_text_lockvote_fail),Toast.LENGTH_SHORT).show();
//				}else{
//					LockVoteDialogView();				
//				}
			}
		});
		
		
//		mbuttonDataEntry.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//
//				Intent it = new Intent(MainActivity.this, DataEntry.class);
//				startActivity(it);
//
//			}
//		});

		mbuttonDataTransmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				//query gather
				
				Intent it = new Intent(MainActivity.this, GatherQuery.class);
				startActivity(it);

				//mrz
				
//				Intent it = new Intent(MainActivity.this, CameraActivity.class); 
//				startActivity(it);				
				
//				
//				new AlertDialog.Builder(MainActivity.this) 
//				.setTitle("请选择")  
//				.setIcon(android.R.drawable.ic_dialog_info)                  
//				.setSingleChoiceItems(type, 0,   
//				  new DialogInterface.OnClickListener() {  
//				                              
//				     public void onClick(DialogInterface dialog, int which) {  
//				    	 
//				        switch (which) {
//				        case 0:
//						case 1:	// 一代身份证
//						case 2:
//						case 3:
//						case 4:
//						case 5:
//						case 6:
//						case 7:
//						case 8:
//						case 9:
//						case 10:
//						case 11:
//						case 12:
//						case 13:
//						case 14:
//						case 15:
//							nMainID = which + 1;
//							break;
//							
//						case 16:
//						case 17:
//							nMainID = which + 984;
//							break;
//							
//						case 18:
//						case 19:
//						case 20:
//						case 21:
//						case 22:
//						case 23:
//						case 24:
//							
//							nMainID = which + 985;
//							break;
//							
//						default:
//							break; 
//						}
//				        Log.v("crjlog","which = " + which);
//				        Log.v("crjlog","nMainID = " + nMainID);
//				        
//						Intent mintent = new Intent(MainActivity.this,CameraActivity.class);
//						mintent.putExtra("nMainID", nMainID);
//						startActivity(mintent); 
//						
//				        dialog.dismiss();  
//				        
//				     }  
//				  }  
//				)  
//				.setNegativeButton("取消", null)  
//				.create()
//				.show();  
				
				
//				 Intent it = new Intent(MainActivity.this, DataSend.class);
//				 startActivity(it);

//				initEdittextDialogView();
//				
//				dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
//				// dialog_tv.setText(R.string.entry_username_modify_toast);
//				dialog_tv.setText(R.string.entry_user_new_pwd_toast);
//
//				dialog_bt_ok.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//
//						if (dialog_et.getText().length() > 0) {
//							
////							String aa = dialog_et.getText().toString();
//							changePwd_pwd = Long.parseLong(dialog_et.getText().toString());
//
//							if (dlg != null) {
//								dlg.dismiss();
//							}
//							
//							initEdittextDialogView();
//							dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
//							dialog_tv.setText(R.string.entry_user_new_pwd_again_toast);
//
//							dialog_bt_ok
//									.setOnClickListener(new OnClickListener() {
//										@Override
//										public void onClick(View v) {
//											// TODO
//											// Auto-generated
//											// method
//											// stub
//
//											if (dialog_et.getText().length() > 0) {
//												
//												if(changePwd_pwd == Long.parseLong(dialog_et.getText().toString())){
//													
//													queryUser(changePwd_username);
//													
//													try {
//														mVoteDB_log.update_usertable(
//																mCursor_user
//																		.getInt(0),
//																dialog_et.getText()
//																		.toString(),
//																false);
//														Toast.makeText(
//																MainActivity.this,
//																R.string.userlist_success_modifypwd,
//																Toast.LENGTH_SHORT)
//																.show();
//														if (dlg != null) {
//															dlg.dismiss();
//														}
//
//														// 系统日志 修改密码
//														mVoteDB_log
//																.insert_syslogtable(
//																		changePwd_username,
//																		getResources()
//																				.getString(
//																						R.string.System_Log_event_changepwd));
//
//													} catch (Exception e) {
//														// TODO:
//														// handle
//														// exception
//														Toast.makeText(
//																MainActivity.this,
//																R.string.userlist_fail_modifypwd,
//																Toast.LENGTH_SHORT)
//																.show();
//													}
//													
//												}else{
//													
//													dialog_et.setText("");
//													
//													Toast.makeText(
//															MainActivity.this,
//															R.string.entry_user_pwd_inconformity_toast,
//															Toast.LENGTH_SHORT)
//															.show();
//												}
//
//											} else {
//												Toast.makeText(
//														MainActivity.this,
//														R.string.add_userpwd_empty_toast,
//														Toast.LENGTH_SHORT)
//														.show();
//											}
//										}
//									});
//
//						}else{
//							
//							Toast.makeText(
//									MainActivity.this,
//									R.string.add_userpwd_empty_toast,
//									Toast.LENGTH_SHORT)
//									.show();
//							
//						}
//					}
//				});
			}
		});

		mbuttonAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Intent it = new Intent(MainActivity.this, SystemAbout.class);
//				startActivity(it);
				
				
				
			  //BT printer	
              Intent intent = new Intent();    
              intent.setClassName("com.example.btpdemo",    
                      "com.example.btpdemo.MainActivity");      
              startActivity(intent);
				
			}
		});

		mbuttonExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				exitDialogView();

			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		com.io.io.IoClose();
		//LockVoteStatus();
	}
	
	private void initDialogView() {

		dlg = new AlertDialog.Builder(MainActivity.this).create();
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
				dlg.dismiss();
			}
		});
	}
	
	//12.19
	private void LockVoteStatus() {
		
		Boolean is_login = preferences.getBoolean("is_lockVote", false);
		
		if(is_login){
			mbuttonLockVote.setBackgroundResource(R.drawable.mainbutton_voteclose);
		}else{
			mbuttonLockVote.setBackgroundResource(R.drawable.mainbutton_voteopen);
		}
	}
	
	private void LockVoteDialogView() {

		initDialogView();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String nowTime = format.format(new java.util.Date());
		Log.v("crjlog", "date = " + nowTime);

		// get Sum
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "date=?",
				new String[] { nowTime }, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int Sum = mCursor.getCount();
		mCursor.close();

		// get success num
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null,
				"date=?" + "and status=?", new String[] { nowTime, "Y" }, null,
				null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int num_s = mCursor.getCount();
		mCursor.close();

		// get success num
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null,
				"date=?" + "and status=?", new String[] { nowTime, "N" }, null,
				null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int num_f = mCursor.getCount();
		mCursor.close();

		dialog_title.setText(R.string.screen_login_LockVote_dialog_close); 
		
		String text = (getResources()
				.getString(R.string.screen_login_exit_text)
				+ Sum
				+ "\n"
				+ getResources().getString(R.string.screen_login_exit_text_s)
				+ num_s
				+ "\n"
				+ getResources().getString(R.string.screen_login_exit_text_f) + num_f);

		dialog_tv.setText(text);

		dialog_bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Editor editor = preferences.edit();
				editor.putBoolean("is_lockVote", true);
				// 提交更改
				editor.commit();

				mbuttonLockVote.setBackgroundResource(R.drawable.mainbutton_voteclose);
				
				if (dlg != null)
					dlg.dismiss();

			}
		});
	}
	

	private void exitDialogView() {

		initDialogView();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String nowTime = format.format(new java.util.Date());
		Log.v("crjlog", "date = " + nowTime);

		// get Sum
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "date=?",
				new String[] { nowTime }, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int Sum = mCursor.getCount();
		mCursor.close();

		// get success num
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null,
				"date=?" + "and status=?", new String[] { nowTime, "Y" }, null,
				null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int num_s = mCursor.getCount();
		mCursor.close();

		// get success num
		mCursor = mVoteDB.query(VIN_TABLE_NAME, null,
				"date=?" + "and status=?", new String[] { nowTime, "N" }, null,
				null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		int num_f = mCursor.getCount();
		mCursor.close();

		dialog_title.setText(R.string.screen_login_exit_dialog);

		String text = (getResources()
				.getString(R.string.screen_login_exit_text)
				+ Sum
				+ "\n"
				+ getResources().getString(R.string.screen_login_exit_text_s)
				+ num_s
				+ "\n"
				+ getResources().getString(R.string.screen_login_exit_text_f) + num_f);

		dialog_tv.setText(text);

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
				// backups
				if (Environment.isExternalStorageRemovable()) {

					Databackups();

					Databackups_intern();
				} else {

					Databackups();
				}

				// 系统 日志 登出
				mVoteDB_log.insert_syslogtable(
						preferences.getString("last_login_username", "Admin"),
						getResources().getString(
								R.string.System_Log_event_logout));

			}
		});

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

			String newPath = "/storage/sdcard1/Vote/" + pucode + "_" + ET + "_"
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
					MainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup),
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					MainActivity.this,
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
					+ pucode
					+ "_"
					+ ET
					+ "_"
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
					MainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup),
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					MainActivity.this,
					getResources().getString(
							R.string.screen_login_exit_toast_backup_fail),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	// 字节流加解密
	public static int dataEncDec(byte[] b, int v) {
		int nRet = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i] != 0 && b[i] != (byte) v) {
				b[i] ^= v;
			}
		}
		return nRet;
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

		if (mCursor != null)
			mCursor.close();

	}

	// 判断用户是否存在
	private int queryUser(String name) {
		mCursor_user = mVoteDB_log.query(USER_TABLE_NAME, null, "user_name = '"
				+ name + "'", null, null, null, null);
		mCursor_user.moveToFirst();
		return mCursor_user.getCount();
	}

	private void initEdittextDialogView() {

		dlg = new AlertDialog.Builder(MainActivity.this).create();
		dlg.show();
		Window window = dlg.getWindow();
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
							MainActivity.this,
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
				//
				try {
					String temp = arg0.toString();
					String tem = temp.substring(temp.length() - 1,
							temp.length());
					char[] temC = tem.toCharArray();
					int mid = temC[0];

					if (mid >= 48 && mid <= 57) {
						return;
					}
					if (mid >= 65 && mid <= 90) {
						return;
					}
					if (mid >= 97 && mid <= 122) {
						return;
					}
					arg0.delete(temp.length() - 1, temp.length());
				} catch (Exception e) {
					// TODO: handle exception
				}

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
				dlg.dismiss();
			}
		});
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			String scan_txt = data.getStringExtra("scan");
			Toast.makeText(this, scan_txt, Toast.LENGTH_SHORT).show();
		}
	}

}
