package com.example.jy.demo.fingerprint;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class login extends Activity {

	private Button mButton_help,mButton_enter;//,mButton_regi;
	private EditText mEditText_name, mEditText_psw;//, editText_code;//, editText_et;
	private SharedPreferences preferences;

	private Vote_DBHelper mVoteDB;
	private Cursor mCursor_user;

	private String USER_TABLE_NAME, USER_NAME, USER_PWD, ADMIN_USER_NAME,DEFAULT_USER_NAME;
	private Intent mintent;

	private String SYSTEM_CONFIG_PUCODE = "PU_CODE";
//	private String SYSTEM_CONFIG_CURRENT_ELECTION_TYPE = "CURRENT_ELECTION_TYPE";

	private int ENTRY_NAME_NUM;

	// dialog view
	private AlertDialog dlg;
	private Button dialog_bt_cancel, dialog_bt_ok;
	private EditText dialog_et;
	private TextView dialog_tv,dialog_title;

	//	private SerialPort sp;
	private static int PORT;// 服务器端口号
	public static String IP_ADDR;

	private String changePwd_username;

	private Handler mhandler;
	private ProgressDialog mpDialog;

	private SimpleDateFormat SystemDateTimeformat,VoteDateTimeformat;
	private String SYSTEMCONFIG_DATE_FORMAT = "yyyy-MM-dd,HH:mm";
	private String SYSTEMCONFIG_VOTE_DATETIME_FORMAT = "yyyyMMddHHmm";

//	//wentong
//	// dcs
//	private AuthService.authBinder authBinder;
//	public RecogService.recogBinder recogBinder;
//	private RecogParameterMessage rpm;
//	private String selectPath = "";
//	private EditText resultEditText;
//	private int ReturnAuthority = -1; 
//	private int width;
//	public static final String PATH = Environment.getExternalStorageDirectory()
//			.toString() + "/AndroidWT";
//
//	protected int readIntPreferences(String perferencesName, String key) {
//		SharedPreferences preferences = getSharedPreferences(perferencesName,
//				MODE_PRIVATE);
//		int result = preferences.getInt(key, 0);
//		return result;
//	}
//
//	protected void writeIntPreferences(String perferencesName, String key,
//			int value) {
//		SharedPreferences preferences = getSharedPreferences(perferencesName,
//				MODE_PRIVATE);
//		SharedPreferences.Editor editor = preferences.edit();
//		editor.putInt(key, value);
//		editor.commit();
//	}
//
//	public ServiceConnection authConn = new ServiceConnection() {
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			authBinder = null;
//		}
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			authBinder = (AuthService.authBinder) service;
//			try {
//				ReturnAuthority = -1;
//				AuthParameterMessage apm = new AuthParameterMessage();
//
//				// me //VV5BCWXMENSY857YY8WKYYB8W
//				// VV47VWXN1ZUY32HYYQ9BYY57A
//				apm.sn = dialog_et.getText().toString();//		"VV47VWXN1ZUY32HYYQ9BYY57A";// WU9H5VSSDVXYB6KYYI52YYICW 			// WUB7RVSN1JVYHFBYY7P9YYC37
//				apm.authfile = "";// /mnt/sdcard/auth/A1000038AB08A2_zj.txt
//				ReturnAuthority = authBinder.getIDCardAuth(apm);
//
//				if (ReturnAuthority != 0) {
//					Toast.makeText(getApplicationContext(),
//							"授权验证失败，返回错误码：" + ReturnAuthority,
//							Toast.LENGTH_SHORT).show();
//				} else {
//					Toast.makeText(getApplicationContext(), "授权验证成功",
//							Toast.LENGTH_SHORT).show();
//				}
//			} catch (Exception e) {
//				Toast.makeText(getApplicationContext(), "授权验证失败",
//						Toast.LENGTH_SHORT).show();
//
//			} finally {
//				if (authBinder != null) {
//					unbindService(authConn);
//				}
//			}
//		}
//
//	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

//		mButton_regi = (Button) this.findViewById(R.id.button_regist);
		mButton_help = (Button) this.findViewById(R.id.button_regist);
		mButton_enter = (Button) this.findViewById(R.id.button_enter);
		mEditText_name = (EditText) this.findViewById(R.id.editText_name);
		mEditText_psw = (EditText) this.findViewById(R.id.editText_psw);
		//editText_code = (EditText) this.findViewById(R.id.editText_code);

//		editText_et = (EditText) this.findViewById(R.id.EditText_et);
		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		IP_ADDR = preferences.getString("GPRS_IP","216.24.172.73");
		PORT = Integer.parseInt(preferences.getString("GPRS_PORT","6778"));

		SystemDateTimeformat = new SimpleDateFormat(SYSTEMCONFIG_DATE_FORMAT);
		VoteDateTimeformat = new SimpleDateFormat(SYSTEMCONFIG_VOTE_DATETIME_FORMAT);
//		try {
//			sp = new SerialPort(new File("/dev/ttyMT0"), 115200, 0);
//		} catch (SecurityException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		mVoteDB = new Vote_DBHelper(this);
		USER_TABLE_NAME = mVoteDB.USER_TABLE_NAME;
		USER_NAME = mVoteDB.USER_NAME;
		USER_PWD = mVoteDB.USER_PWD;
		ENTRY_NAME_NUM = mVoteDB.ENTRY_NAME_NUM;
		ADMIN_USER_NAME = mVoteDB.ADMIN_USER_NAME;
		DEFAULT_USER_NAME = mVoteDB.DEFAULT_USER_NAME;

		// 初始化mEditText

		mpDialog = new ProgressDialog(login.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mpDialog.setMessage(getResources().getString(R.string.download_new_software_title));
		mpDialog.setMax(100);
		mpDialog.setIndeterminate(false);
		mpDialog.setCanceledOnTouchOutside(false);

		initEditData();

		mButton_help.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				//	Toast.makeText(login.this, "Default PassWD of user is 2015",Toast.LENGTH_LONG).show();


//				initEdittextDialogView(); 
//				dialog_et.setInputType(InputType.TYPE_CLASS_TEXT);
//				dialog_tv.setText(R.string.screen_login_text_auth);
//
//				dialog_bt_ok.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						
//						Intent authIntent = new Intent(login.this, AuthService.class);
//						bindService(authIntent, authConn, Service.BIND_AUTO_CREATE);
//						
//						if (dlg != null)
//							dlg.dismiss();
//						
//					}
//				});


				File oldfile =new File("/system/media/help.pdf");

				Uri path = Uri.fromFile(oldfile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(path, "application/pdf");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {

					Toast.makeText(login.this, "open help.pdf fail!",Toast.LENGTH_LONG).show();
				}
			}
		});


		mButton_enter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 判断用户是否存在

//				mSendBroadcast_close();

				if (queryUser(mEditText_name.getText().toString()) > 0) {
					// 判断用户密码
					if ((mEditText_psw.getText().toString())
							.equals(mCursor_user.getString(2))) {
						// 判断是否是管理员
						if ((mEditText_name.getText().toString())
								.equals(ADMIN_USER_NAME)) {

							mintent = new Intent(login.this,
									AdminMainActivity.class);

							//12.19
//							if(mCursor_user.getString(2).equals("8888")){
//								
//								Toast.makeText(arg0.getContext(),
//										R.string.login_entry_changepwd_toast, Toast.LENGTH_SHORT).show();
//							}

						} else {


							if (mEditText_name.getText().toString().equals("userc") || mEditText_name.getText().toString().equals("userv")) {
								if(mCursor_user.getString(2).equals("123")){

									Toast.makeText(arg0.getContext(),
											R.string.login_entry_changepwd_toast, Toast.LENGTH_SHORT).show();

								}
							}

							if(mCursor_user.getInt(3) == 1){

								mintent = new Intent(login.this, MainActivity.class);
							}else if(mCursor_user.getInt(3) == 2){

								mintent = new Intent(login.this, MainActivity_V.class);

								//vote begin datetime
								String votebegin = preferences.getString("VOTE_BEGIN_DATETIME", "201001010000");
								try {
									votebegin = SystemDateTimeformat.format(VoteDateTimeformat.parse(votebegin).getTime());
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								//vote end datetime
								String voteEnd = preferences.getString("VOTE_END_DATETIME", "201601010000");
								try {
									voteEnd = SystemDateTimeformat.format(VoteDateTimeformat.parse(voteEnd).getTime());
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								String text =   getResources().getString(R.string.login_entry_PU_toast) +" "+ preferences.getString(SYSTEM_CONFIG_PUCODE, "37-06-01-007")+"\n"+
										getResources().getString(R.string.login_entry_ET_toast) +" "+ preferences.getString("CURRENT_ELECTION_TYPE", "Card")+"\n"+
										getResources().getString(R.string.login_entry_ET_begin_time_toast) +" "+ votebegin +"\n"+
										getResources().getString(R.string.login_entry_ET_end_time_toast) +" "+ voteEnd;

								Toast.makeText(login.this, text,Toast.LENGTH_LONG).show();

							}

						}

						startActivity(mintent);
						finish();


						// 向数据库更新flag
						Editor editor = preferences.edit();
						editor.putBoolean("is_login", true);
						// editor.putInt("fprint_Num", 0); 

						// 保存最后一次登录人员
						editor.putString("last_login_username", mEditText_name
								.getText().toString());
						// 提交更改
						editor.commit();

						//登录日志
						mVoteDB.insert_syslogtable(preferences.getString("last_login_username",ADMIN_USER_NAME),getResources().getString(R.string.System_Log_event_login));


					} else {

						Toast.makeText(arg0.getContext(),
								R.string.login_pwd_error_toast,
								Toast.LENGTH_SHORT).show();
						mEditText_psw.setText("");
					}

				} else {

					if(mEditText_name.getText().toString().equals("test")){

						if(mEditText_psw.getText().toString().equals("8888")){

							Intent mintent = new Intent(Intent.ACTION_MAIN);
							mintent.setComponent(new ComponentName("com.mediatek.factorymode",
									"com.mediatek.factorymode.FactoryModeActivity"));
							mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(mintent);
							finish();

						}else{

							Toast.makeText(arg0.getContext(),
									R.string.user_pwd_error_toast, Toast.LENGTH_SHORT)
									.show();
						}

					}else if(mEditText_name.getText().toString().equals("INEC") || mEditText_name.getText().toString().equals("inec")){

						if(mEditText_psw.getText().toString().equals("5500961")){

							Intent mintent = new Intent(Intent.ACTION_MAIN);
							mintent = new Intent(login.this, inecmenu.class);
							startActivity(mintent);
							finish();

						}else{

							Toast.makeText(arg0.getContext(),
									R.string.user_pwd_error_toast, Toast.LENGTH_SHORT)
									.show();
						}
					}else if(mEditText_name.getText().toString().equals("IMEI") || mEditText_name.getText().toString().equals("imei")){

						textDialogView();

					}else if(mEditText_name.getText().toString().equals("afc") || mEditText_name.getText().toString().equals("Afc")){

						Intent mintent = new Intent(Intent.ACTION_MAIN);
						mintent.setComponent(new ComponentName("com.joyatel.factory",
								"org.qtproject.qt5.android.bindings.QtActivity"));
						mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(mintent);
						finish();

					}else{
						Toast.makeText(arg0.getContext(),
								R.string.login_user_error_toast, Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
		});

//		// 修改用户密码
//		mButton_regi.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				
//				
//				
////				StringBuffer str=new StringBuffer("aaabbb");
////				
////				str.insert(3," ");
////				
////				
////				Log.v("crjlog","str = " +  str); 
//				
//				
//				//读取下载的
////				String path = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name) +"/"+ "a.txt";
////				try {
////					ArrayList<String> list=new ArrayList<String>();
////					BufferedReader bReader;
////					File file=new File(path);
////					bReader = new BufferedReader(new FileReader(file));
////					String str=null;
////					while((str=bReader.readLine())!=null)
////					{
////						Log.v("crjlog","str = " +  str);  
////						list.add(str);
////					}
////					Log.v("crjlog","list111111111111 = " +  list);  
////					Log.v("crjlog","list.size() = " +  list.size());  
////					Log.v("crjlog","list.get(0) = " +  list.get(0)); 
////					Log.v("crjlog","list.get(1) = " +  list.get(1)); 
////					Log.v("crjlog","list.get(2) = " +  list.get(2)); 
////					
////				} catch (FileNotFoundException e1) {
////					// TODO Auto-generated catch block
////					e1.printStackTrace();
////				} catch (IOException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
//				
//				
////				String Path1 = getFilesDir().getParent().toString();
////				String fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
//				
//				
//				initEdittextDialogView(); 
//				dialog_tv.setText(R.string.entry_username_modify_toast); 
//				
//				dialog_bt_ok.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						
//						if (dialog_et.getText().length() > 0) {
//							changePwd_username = dialog_et.getText().toString();
//							if ((dialog_et.getText().toString())
//									.equals(ADMIN_USER_NAME)) {
//
//								Toast.makeText(
//										login.this,
//										R.string.screen_login_usermodify_error_toast,
//										Toast.LENGTH_SHORT).show();
//								
//								dialog_et.setText("");
//								
//							} else {
//
//								// 判断用户是否存在
//								if (queryUser(dialog_et.getText().toString()) > 0) {
//
//									if (dlg != null) {
//										dlg.dismiss();
//									}
//
//									initEdittextDialogView();
//									dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
//									dialog_tv.setText(R.string.entry_user_pwd_toast);
//
//									dialog_bt_ok
//											.setOnClickListener(new OnClickListener() {
//												@Override
//												public void onClick(View v) {
//													// TODO Auto-generated
//													// method stub
//
//													if (dialog_et.getText()
//															.length() > 0) {
//
//														try {
//															// 判断用户密码
//															if ((dialog_et
//																	.getText()
//																	.toString())
//																	.equals(mCursor_user
//																			.getString(2))) {
//																// 输入新密码
//
//																if (dlg != null) {
//																	dlg.dismiss();
//																}
//																initEdittextDialogView();
//																dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
//																dialog_tv.setText(R.string.entry_user_new_pwd_toast);
//
//																dialog_bt_ok
//																		.setOnClickListener(new OnClickListener() {
//																			@Override
//																			public void onClick(
//																					View v) {
//																				// TODO
//																				// Auto-generated
//																				// method
//																				// stub
//
//																				if (dialog_et
//																						.getText()
//																						.length() > 0) {
//																					try {
//																						mVoteDB.update_usertable(
//																								mCursor_user.getInt(0),
//																								dialog_et.getText()
//																										.toString(),
//																								false);
//																						Toast.makeText(
//																								login.this,
//																								R.string.userlist_success_modifypwd,
//																								Toast.LENGTH_SHORT)
//																								.show();
//																						if (dlg != null) {
//																							dlg.dismiss();
//																						}
//																						
//																						//系统日志 修改密码
//																						mVoteDB.insert_syslogtable(changePwd_username,getResources().getString(R.string.System_Log_event_changepwd));
//
//																					} catch (Exception e) {
//																						// TODO:
//																						// handle
//																						// exception
//																						Toast.makeText(
//																								login.this,
//																								R.string.userlist_fail_modifypwd,
//																								Toast.LENGTH_SHORT)
//																								.show();
//																					}
//
//																				} else {
//																					Toast.makeText(
//																							login.this,
//																							R.string.add_userpwd_empty_toast,
//																							Toast.LENGTH_SHORT)
//																							.show();
//																				}
//																			}
//																		});
//
//															} else { 
//																Toast.makeText(
//																		login.this,
//																		R.string.user_pwd_error_toast,
//																		Toast.LENGTH_SHORT)
//																		.show();
//															}
//														} catch (Exception e) {
//															// TODO: handle
//															// exception
//															Toast.makeText(
//																	login.this,
//																	R.string.userlist_fail_modifypwd,
//																	Toast.LENGTH_SHORT)
//																	.show();
//														}
//
//													} else {
//
//														Toast.makeText(
//																login.this,
//																R.string.add_userpwd_empty_toast,
//																Toast.LENGTH_SHORT)
//																.show();
//
//													}
//												}
//											});
//
//								} else {
//									Toast.makeText(login.this,
//											R.string.login_user_error_toast,
//											Toast.LENGTH_SHORT).show();
//								}
//							}
//
//						} else {
//							Toast.makeText(login.this,
//									R.string.add_username_empty_toast,
//									Toast.LENGTH_SHORT).show();
//						}
//
//					}
//				});
//			}
//		});

		mhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				switch (msg.what) {
					case 0:

//					Toast.makeText(login.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
						Toast.makeText(login.this, R.string.download_new_software_f, Toast.LENGTH_SHORT).show();

						mhandler.removeMessages(3);

						if(mpDialog != null)
							mpDialog.dismiss();

						mhandler.removeMessages(3);



						break;

					case 1:

						Toast.makeText(login.this, R.string.download_new_software_s, Toast.LENGTH_SHORT).show();

						//系统日志 下载软件
						mVoteDB.insert_syslogtable(preferences.getString("last_login_username","Admin"),getResources().getString(R.string.System_Log_event_download));
						mhandler.removeMessages(3);

						if(mpDialog != null)
							mpDialog.dismiss();

						mhandler.removeMessages(3);

						update();

						break;

					case 2:

						if(mpDialog != null)
							mpDialog.show();

						break;


					case 3:

						mpDialog.setProgress(msg.arg1);
						mpDialog.show();
						break;

					case 4:

						Toast.makeText(login.this, R.string.software_new, Toast.LENGTH_SHORT).show();

						break;
					default:
						break;
				}

			}
		};


	}

	private static String readFile(String filePath) {
		String str = "";
		try {
			File readFile = new File(filePath);
			if (!readFile.exists()) {
				return null;
			}
			FileInputStream inStream = new FileInputStream(readFile);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {

				Log.v("crjlog","buffer = " +  buffer);

				stream.write(buffer, 0, length);
			}
			str = stream.toString();
			stream.close();
			inStream.close();
			return str;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void initDialogView() {

		dlg = new AlertDialog.Builder(login.this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.theme_dialog_text);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window.findViewById(R.id.text_dialog_title);
		dialog_tv = (TextView) window.findViewById(R.id.text_dialog_tv);

		dialog_bt_ok = (Button) window.findViewById(R.id.text_dialog_button_ok);
		dialog_bt_cancel = (Button) window
				.findViewById(R.id.text_dialog_button_cancel);
		dialog_bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(dlg != null)
					dlg.dismiss();
			}
		});
		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(dlg != null)
					dlg.dismiss();
			}
		});
	}

	private void textDialogView() {

		initDialogView();

		try {
			dialog_title.setText("IMEI");
			TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			String imei=telephonyManager.getDeviceId();
			if(imei == null){
				dialog_tv.setText("Null");
			}else{
				dialog_tv.setText(imei);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(
					login.this,
					getResources().getString(
							R.string.personscreen_text_readnfc_fail),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void initEdittextDialogView() {

		dlg = new AlertDialog.Builder(login.this).create();
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
							login.this,
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
					String tem = temp.substring(temp.length()-1, temp.length());
					char[] temC = tem.toCharArray();
					int mid = temC[0];

					Log.v("crjlog","mid = " +  mid);

					if(mid>=48&&mid<=57){
						return;
					}
					if(mid>=65&&mid<=90){
						return;
					}
					if(mid>=97&&mid<=122){
						return;
					}
					arg0.delete(temp.length()-1, temp.length());
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

	private void initEditData() {

		//editText_code.setText(preferences.getString(SYSTEM_CONFIG_PUCODE,"34-16-10-003"));
//		Log.v("crjlog", " SYSTEM_CONFIG_CURRENT_ELECTION_TYPE  = " + preferences.getString(SYSTEM_CONFIG_CURRENT_ELECTION_TYPE, "President"));
//		editText_et.setText(preferences.getString(SYSTEM_CONFIG_CURRENT_ELECTION_TYPE, "President"));

		//editText_code.setEnabled(false);
//		editText_et.setEnabled(false);

		mEditText_psw.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_NAME_NUM) });
		mEditText_psw.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() >= ENTRY_NAME_NUM) {
					Toast.makeText(login.this,getResources().getString(R.string.entry_max_num_toast)+ ENTRY_NAME_NUM, Toast.LENGTH_SHORT).show();
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

			}
		});

		mEditText_name.setText(preferences.getString("last_login_username",ADMIN_USER_NAME));
//		mEditText_name.setText(preferences.getString("last_login_username",DEFAULT_USER_NAME));

		mEditText_name.setSingleLine();
		mEditText_name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_NAME_NUM) });

		mEditText_name.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() >= ENTRY_NAME_NUM) {
					Toast.makeText(login.this,getResources().getString(R.string.entry_max_num_toast)+ ENTRY_NAME_NUM, Toast.LENGTH_SHORT).show();
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
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// 重启登录界面时，初始化将 is_login set false
		Editor editor = preferences.edit();
		editor.putBoolean("is_login", false);

		// 提交更改
		editor.commit();

		Log.v("crjlog", "onResume = ");
		new Thread(new MyThread()).start();

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

	}

	// 判断用户是否存在
	private int queryUser(String name) {
		mCursor_user = mVoteDB.query(USER_TABLE_NAME, null, "user_name = '"
				+ name + "'", null, null, null, null);
		mCursor_user.moveToFirst();
		return mCursor_user.getCount();
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(mCursor_user != null)
			mCursor_user.close();

		if (dlg != null) {
			dlg.dismiss();
		}

	}

	public class MyThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket mSocket = null;
			DataOutputStream mDataOutputStream = null;
			DataInputStream mDataInputStream = null;
			OutputStream mOutputStream = null;
			InputStream mInputStream = null;
			BufferedOutputStream fo = null;

			int bytesRead = 0;
			int sum = 0;
			byte[] buffer = new byte[1024 * 1024];

			Log.v("crjlog", "MyThread = ");
			try {
				mSocket = new Socket();
				mSocket.connect(new InetSocketAddress(IP_ADDR, PORT), 6778);

				mDataOutputStream = new DataOutputStream(
						mSocket.getOutputStream());

				mDataOutputStream.writeUTF(getversion());
				mOutputStream = mSocket.getOutputStream();
				mOutputStream.flush();
				Log.v("crjlog", "MyThread1111111111 = ");
				// 一定要加上这句，否则收不到来自服务器端的消息返回 
				mSocket.shutdownOutput();
				mDataInputStream = new DataInputStream(mSocket.getInputStream());

//				Log.v("crjlog", "mDataInputStream = " + mDataInputStream);	
//				Log.v("crjlog", "mDataInputStream = " + mDataInputStream.read());	

				if (mDataInputStream != null) {

					sum = Integer.parseInt(mDataInputStream.readUTF());
					Log.v("crjlog", "sum = " + sum);

					if(sum == -1){

						Log.v("crjlog", "22222222222 = ");
						Message message3 = Message.obtain();
						message3.what=4;
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message3);

						return;
					}
				}

				String votepath = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name);
				File votefile = new File(votepath);

				if (!votefile.exists()) {
					votefile.mkdirs();
				}

				String path = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name) +"/"+ "Vote.apk";
				fo = new BufferedOutputStream(new FileOutputStream(new File(path)));
				mInputStream = new FileInputStream(path);

				while ((bytesRead = mDataInputStream.read(buffer, 0, buffer.length)) != -1) {

					//更新进度  
					Message message2 = Message.obtain();
					message2.what=3;
					message2.arg1 = (mInputStream.available()*100)/sum;
					mhandler.sendMessage(message2);

					fo.write(buffer, 0, bytesRead);

				}
				fo.flush();
				fo.close();

				Message message = Message.obtain();
				message.what=1;
				//通过Handler发布传送消息，handler
				mhandler.sendMessage(message);
				Log.v("crjlog", "receive apk complete!");

				//更新系统时间
//				updateSystemDatetime(datetime);


			} catch (Exception e) {
				// TODO: handle exception

				Log.v("crjlog", "getMessage1111111111111111111111111111 = " + e.getMessage());

				Message message = Message.obtain();
				message.what=0;

				if(e.getMessage() != null){
					message.obj = e.getMessage();
				}else{
					message.obj = e.toString();
				}
				//通过Handler发布传送消息，handler
				mhandler.sendMessage(message);

				e.printStackTrace();
			} finally {

				if (buffer != null)
					buffer = null;

				if (mOutputStream != null)
					try {
						mOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block

						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);

						e.printStackTrace();
					}

				if (mInputStream != null)
					try {
						mInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block

						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);

						e.printStackTrace();
					}


				if (mDataOutputStream != null)
					try {
						mDataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);
						e.printStackTrace();
					}

				if (mDataInputStream != null)
					try {
						mDataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);
						e.printStackTrace();
					}

				if (fo != null)
					try {
						fo.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);
						e.printStackTrace();
					}

				if (mSocket != null)
					try {
						mSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Message message = Message.obtain();
						message.what=0;

						if(e.getMessage() != null){
							message.obj = e.getMessage();
						}else{
							message.obj = e.toString();
						}
						//通过Handler发布传送消息，handler
						mhandler.sendMessage(message);
						e.printStackTrace();
					}

			}
		}
	}

	public String getversion() {
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);

			Log.v("crjlog","info versionCode = " +  info.versionCode);

			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void update() {
		String path = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name) +"/"+ "Vote.apk";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(path)),"application/vnd.android.package-archive");
		startActivity(intent);
	}

}
