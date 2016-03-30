package com.example.jy.demo.fingerprint;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SystemConfig extends Activity implements OnCancelListener {

	private ListView mlist;
	private String[] strs_name, strs_detail;
	private String[] strs_sp;
	
	private String[] mElectionType;
	private String mElectionType_old,cuttent_mElectionType;
	private int cuttent_mElectionType_id;
	
	private Button mDownload,mDatetime,mBack;
	
	private ListViewAdapter mAdapter;

	private SharedPreferences preferences;
	private Editor editor;

	private String nowDate,noset;
	private static String voteBeginDate,voteBeginTime,voteEndDate,voteEndTime;
	
	private EditText mEditText_dialog,mEditText_dialog_et;
	
	private static final int SYSTEMCONFIG_DIALOG_PU_CODE = 0;
	private static final int SYSTEMCONFIG_DIALOG_SC_DATETIME = 1;
	private static final int SYSTEMCONFIG_DIALOG_VOTE_TIME_BEGIN = 2;
	private static final int SYSTEMCONFIG_DIALOG_VOTE_TIME_END = 3;
	private static final int SYSTEMCONFIG_DIALOG_GPRS_IP = 4;
	private static final int SYSTEMCONFIG_DIALOG_GPRS_PORT = 5;
	private static final int SYSTEMCONFIG_DIALOG_ELECTION_TYPE = 6;
//	private static final int SYSTEMCONFIG_DIALOG_LIGHT_LEVEL = 7;
//	private static final int SYSTEMCONFIG_DIALOG_SOUND_LEVEL = 8;

	private   int ENTRY_EDITTEXT_COUNT = 9;
	private int ENTRY_EDITTEXT_COUNT_IP = 15;
	
	private   String SYSTEMCONFIG_DATE_FORMAT = "yyyy-MM-dd,HH:mm";
	private   String SYSTEMCONFIG_VOTE_DATETIME_FORMAT = "yyyyMMddHHmm";
	
	private SimpleDateFormat SystemDateTimeformat,VoteDateTimeformat;
	
	// dialog view
	private AlertDialog DexttextDlg,textviewDlg,listviewDlg;
	private Button dialog_bt_cancel, dialog_bt_ok,listDialog_bt1,listDialog_bt2; 
	private EditText dialog_et;
	private TextView dialog_tv,dialog_title;
	
	private static int PORT;// 服务器端口号
	public static String IP_ADDR;
	private Handler mhandler;
	private Vote_DBHelper mVoteDB_log;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemconfig);
		
		mVoteDB_log = new Vote_DBHelper(this);
		preferences = this.getSharedPreferences(
				getResources().getString(R.string.SystemConfig_sp),
				MODE_PRIVATE);
		editor = preferences.edit(); 
		
		SystemDateTimeformat = new SimpleDateFormat(SYSTEMCONFIG_DATE_FORMAT);
		VoteDateTimeformat = new SimpleDateFormat(SYSTEMCONFIG_VOTE_DATETIME_FORMAT);
		
		noset = getResources().getString(R.string.systemconfig_sp_noset);
		
		strs_sp = getResources().getStringArray(R.array.system_config_sp);
		strs_name = getResources().getStringArray(R.array.system_config);
		strs_detail = getResources().getStringArray(R.array.system_config);
		mlist = (ListView) findViewById(R.id.sc_list);
		
		mDownload = (Button) findViewById(R.id.SC_button_download);
		mDatetime = (Button) findViewById(R.id.SC_button_datetime);
		mBack = (Button) findViewById(R.id.SC_button_restore);
		
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mDatetime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				new Thread(new MyThread()).start();
			}
		});
		
		mDownload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Toast.makeText(SystemConfig.this, R.string.systemconfig_toast_updateconfig_wait, Toast.LENGTH_SHORT).show(); 
				//crj modify 10.28
				mDownload.setEnabled(false);
				mDatetime.setEnabled(false);
				
				new Thread(new MyThread2()).start();
				
			}
		});
		
		
		getSystemTime();
		// init strs2
		updatalist();
		
		mlist.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case SYSTEMCONFIG_DIALOG_SC_DATETIME:
				case SYSTEMCONFIG_DIALOG_VOTE_TIME_BEGIN:// vote time seting
				case SYSTEMCONFIG_DIALOG_VOTE_TIME_END:// vote time seting
				case SYSTEMCONFIG_DIALOG_GPRS_IP:// gprs ip
				case SYSTEMCONFIG_DIALOG_GPRS_PORT:// gprs port
				case SYSTEMCONFIG_DIALOG_PU_CODE:// PU code
//				case SYSTEMCONFIG_DIALOG_ELECTION_TYPE:// PU code
					 showDialog(arg2);
					break;
//
//				case SYSTEMCONFIG_DIALOG_LIGHT_LEVEL:// light level
//
//					break;
//
//				case SYSTEMCONFIG_DIALOG_SOUND_LEVEL:// sound level
//
//					break;

				}
			}
		});
		
		mhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch (msg.what) {
				case 0:
					
					mhandler.removeMessages(3); 
					
					//12.17
					if(configTxtIsExists()){
						
						Message message = Message.obtain(); 
						message.what=3;
						mhandler.sendMessageDelayed(message, 3000);
						
					}else{
						
						//crj modify 10.28
						mDownload.setEnabled(true); 
						mDatetime.setEnabled(true);
						
//						Toast.makeText(SystemConfig.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
						Toast.makeText(SystemConfig.this, R.string.download_new_software_f, Toast.LENGTH_SHORT).show(); 
					}
					break;
					
				case 1:
					
					Toast.makeText(SystemConfig.this, R.string.systemconfig_toast_updatetime_s, Toast.LENGTH_SHORT).show();
					//系统日志 更新时钟
					mVoteDB_log.insert_syslogtable(preferences.getString("last_login_username","Admin"),getResources().getString(R.string.System_Log_event_updatetime));
					mhandler.removeMessages(3);
					
					getSystemTime();
					updatalist();
					break;

				case 2:
					
					//系统日志 更新设置
					mVoteDB_log.insert_syslogtable(preferences.getString("last_login_username","Admin"),getResources().getString(R.string.System_Log_event_updateconfig));
					mhandler.removeMessages(3);
					updatalist();
					
					Message message = Message.obtain(); 
					message.what=3;
					mhandler.sendMessageDelayed(message, 3000);
					
					break;
					
				case 3:
					
					updateSystemConfig();
					changeSystemConfigstatus();
					
					//crj modify 10.28
					mDownload.setEnabled(true);
					mDatetime.setEnabled(true);
					
					break;
					
				default:
					break;
				}
				
			}
		};
 
	}
	
	@Override
	public Dialog onCreateDialog(int id, Bundle args) {

		final int mNum = id;
		String title = getResources().getString(R.string.systemconfig_sp_set) + strs_name[mNum];
		
		mEditText_dialog = new EditText(SystemConfig.this);
		mEditText_dialog.setSingleLine();
		mEditText_dialog.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_EDITTEXT_COUNT) });
		mEditText_dialog.addTextChangedListener(mTextWatcher);
		
		switch (id) {
		case SYSTEMCONFIG_DIALOG_SC_DATETIME:// system date seting
			
			TimePickerFragment timeDialog = new TimePickerFragment();
			timeDialog.show(getFragmentManager(), "time");
			
			DatePickerFragment dateDialog = new DatePickerFragment();
			dateDialog.show(getFragmentManager(), "date");
			
			break;

		case SYSTEMCONFIG_DIALOG_VOTE_TIME_BEGIN:// vote time seting
			
			TimePickerFragment_begin timeDialog_begin = new TimePickerFragment_begin();
			timeDialog_begin.show(getFragmentManager(), "time");
			
			DatePickerFragment_begin dateDialog_begin = new DatePickerFragment_begin();
			dateDialog_begin.show(getFragmentManager(), "date");

			break;
			
		case SYSTEMCONFIG_DIALOG_VOTE_TIME_END:// vote time seting
			
			TimePickerFragment_end timeDialog_end = new TimePickerFragment_end();
			timeDialog_end.show(getFragmentManager(), "time");
			
			DatePickerFragment_end dateDialog_end = new DatePickerFragment_end();
			dateDialog_end.show(getFragmentManager(), "date");
			
			break;
			
			
//		case SYSTEMCONFIG_DIALOG_ELECTION_TYPE:// ELECTION type
//			
//			getElectionType();
//			
//			new AlertDialog.Builder(this)  
//			.setTitle("请选择")  
//			.setIcon(android.R.drawable.ic_dialog_info)                  
//			.setSingleChoiceItems(mElectionType, cuttent_mElectionType_id,   
//			  new DialogInterface.OnClickListener() {  
//			                              
//			     public void onClick(DialogInterface dialog, int which) {  
//			    	 
//					//保存现在选择的类型														
//					editor.putString("CURRENT_ELECTION_TYPE", mElectionType[which]);
//					editor.commit();
//					
//					updatalist();
//			        dialog.dismiss();  
//			     }  
//			  }  
//			) 
//			.setPositiveButton("新增",new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog,	int which) {
//							
//							mEditText_dialog_et = new EditText(SystemConfig.this);
//							mEditText_dialog_et.setSingleLine();
//							mEditText_dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_EDITTEXT_COUNT) });
//							mEditText_dialog_et.addTextChangedListener(mTextWatcher);
//							 
//							 new AlertDialog.Builder(SystemConfig.this)
//							.setTitle(getResources().getString(R.string.systemconfig_dialog_set_election_type_title))
//							.setIcon(android.R.drawable.ic_dialog_info)
//							.setView(mEditText_dialog_et)
//							.setPositiveButton("Cancel",null)
//							.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
//									public void onClick(DialogInterface dialog,	int which) {
//										if(mEditText_dialog_et.getText().length() > 0){
//											try {
//													String ElectionTypeNew =  mElectionType_old + "-" + mEditText_dialog_et.getText().toString();
//													if(!ElectionType_exist(mEditText_dialog_et.getText().toString())){
//														
//														//保存所有的类型
//														editor.putString("ELECTION_TYPE", ElectionTypeNew);
//														
//														//保存现在选择的类型														
//														editor.putString("CURRENT_ELECTION_TYPE", mEditText_dialog_et.getText().toString());
//														editor.commit();		
//														
//														updatalist();
//														Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_success_toast, Toast.LENGTH_SHORT).show();
//													}else{
//														Toast.makeText(SystemConfig.this, R.string.add_electiontype_error_toast, Toast.LENGTH_SHORT).show();
//													}
//												
//											} catch (Exception e) {
//												// TODO: handle exception
//												Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
//											}
//											
//										}else{
//											Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_edittext_empty, Toast.LENGTH_SHORT).show();
//										}
//									}
//								})					
//							.create().show();
//						}
//					})	
//			.setNegativeButton("取消", null)  
//			.show(); 
//			
//			
//			break;
		case SYSTEMCONFIG_DIALOG_GPRS_IP:// gprs ip 
			
			
			initEdittextDialogView_IP();
			dialog_tv.setText(title);
			dialog_et.setInputType(InputType.TYPE_CLASS_PHONE);
			dialog_bt_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(dialog_et.getText().length() > 0){
						try {
							
							if(isIpAddress(dialog_et.getText().toString())){
							editor.putString(strs_sp[mNum],dialog_et.getText().toString());
							// 提交更改
							editor.commit();						
							updatalist();
							Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_success_toast, Toast.LENGTH_SHORT).show();
							}else{
								dialog_et.setText("");
								Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
						}
						
					}else{
						Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_edittext_empty, Toast.LENGTH_SHORT).show();
					}
					
					dismissEditTextDialogView(); 
				} 
			});
			
			
			
			
			break;
			
		case SYSTEMCONFIG_DIALOG_PU_CODE:// PU code
			
			initEdittextDialogView();
			dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
			dialog_tv.setText(title);
			dialog_bt_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(dialog_et.getText().length() > 0){
						
						if(dialog_et.getText().length() == 9){
							try {
								
								if(mNum == SYSTEMCONFIG_DIALOG_GPRS_PORT){
									if(Integer.valueOf(dialog_et.getText().toString()) > 65535){
										Toast.makeText(SystemConfig.this, R.string.systemconfig_set_port_error, Toast.LENGTH_SHORT).show();
										return;
									}
								}
								
								if(mNum == SYSTEMCONFIG_DIALOG_PU_CODE){
									editor.putString(strs_sp[mNum],pucodeFormat(dialog_et.getText().toString()));
								}else{
									editor.putString(strs_sp[mNum],dialog_et.getText().toString());
								}
								// 提交更改
								editor.commit();						
								updatalist();
								Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_success_toast, Toast.LENGTH_SHORT).show();
								
							} catch (Exception e) {
								// TODO: handle exception
								Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
							}
							
						}else{
							
							Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_pucode_fail, Toast.LENGTH_SHORT).show();
							
						}
						
					}else{
						Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_edittext_empty, Toast.LENGTH_SHORT).show();
					}
					
					dismissEditTextDialogView(); 
				} 
			});
			
			break;
			
		case SYSTEMCONFIG_DIALOG_GPRS_PORT:// gprs port

			
			initEdittextDialogView_port();
			dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
			dialog_tv.setText(title);
			dialog_bt_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(dialog_et.getText().length() > 0){
						
						try {
							
							if(Integer.valueOf(dialog_et.getText().toString()) > 65535 || Integer.valueOf(dialog_et.getText().toString()) < 1){
								Toast.makeText(SystemConfig.this, R.string.systemconfig_set_port_error, Toast.LENGTH_SHORT).show();
								return;
							}
						
							for(int i=0;i < dialog_et.getText().length();i++){ 
								if(dialog_et.getText().toString().indexOf("0") == 0){
									dialog_et.setText(dialog_et.getText().toString().substring(1));
									i--;
								}
							}
							editor.putString(strs_sp[mNum],dialog_et.getText().toString());
							// 提交更改
							editor.commit();						
							updatalist();
							Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_success_toast, Toast.LENGTH_SHORT).show();
							
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
						}
						
					}else{
						Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_edittext_empty, Toast.LENGTH_SHORT).show();
					}
					
					dismissEditTextDialogView(); 
				} 
			});
			
			break;
			
		}

		return null;
	}
	
	
	private void getElectionType(){
		
		mElectionType_old = preferences.getString("ELECTION_TYPE","President");
		mElectionType = mElectionType_old.split("-");
		
		cuttent_mElectionType = preferences.getString("CURRENT_ELECTION_TYPE","President");
		cuttent_mElectionType_id = get_Current_ElectionType(cuttent_mElectionType);
//		Log.v("crjlog", "cuttent_mElectionType_id = " + cuttent_mElectionType_id);
		
	}
	
	//判断类型是否存在
	private int get_Current_ElectionType(String name){
		
		for(int i=0;i< mElectionType.length; i++){
			
			if(mElectionType[i].equals(name)){
				
				return i;
			}
		}
		
		return 0;
	}
	
	//判断类型是否存在
	private boolean ElectionType_exist(String name){
		
		for(int i=0;i< mElectionType.length; i++){
			
			if(mElectionType[i].equals(name)){
				
				return true;
			}
		}
		
		return false;
	}
	
	
	private void getSystemTime(){
		
		nowDate = SystemDateTimeformat.format(new Date());
	}
	

	private void getSystemConfig(){
		
		IP_ADDR = preferences.getString("GPRS_IP","216.24.172.73");
		PORT = Integer.parseInt(preferences.getString("GPRS_PORT","6778"));
	}

	
	public void setVoteBeginDateTime(String string) {
		// TODO Auto-generated method stub
		
			//判断
			String nowDate = VoteDateTimeformat.format(new Date());
			
			try {
				
				if(VoteDateTimeformat.parse(nowDate).getTime() < VoteDateTimeformat.parse(string).getTime()){
					
					String voteEnd = preferences.getString(strs_sp[3], "noset");
					if(!voteEnd.equals("noset")){
						
						try {
							if(VoteDateTimeformat.parse(voteEnd).getTime() < VoteDateTimeformat.parse(string).getTime()){
								
								Toast.makeText(SystemConfig.this,getResources().getString(R.string.systemconfig_set_begindatetime_error_ext), Toast.LENGTH_SHORT).show();
								
							}else{
								
								editor.putString(strs_sp[2], string);
								// 提交更改
								editor.commit();
								updatalist();
							}
							
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						
						editor.putString(strs_sp[2], string);
						// 提交更改
						editor.commit();
						updatalist();
					}
					
				}else{
					
					Toast.makeText(SystemConfig.this,getResources().getString(R.string.systemconfig_set_begindatetime_error), Toast.LENGTH_SHORT).show();
					
				}
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
	}
	
	public void setVoteEndDateTime(String string) {
		// TODO Auto-generated method stub
		
		//判断
		String nowDate = VoteDateTimeformat.format(new Date());
		
		try {
			
			if(VoteDateTimeformat.parse(nowDate).getTime() < VoteDateTimeformat.parse(string).getTime()){
				
				String voteBegin = preferences.getString(strs_sp[2], "noset");
				if(!voteBegin.equals("noset")){
					
					try {
						if(VoteDateTimeformat.parse(voteBegin).getTime() < VoteDateTimeformat.parse(string).getTime()){
							
							editor.putString(strs_sp[3], string);
							// 提交更改
							editor.commit();
							updatalist();
							
						}else{
							
							Toast.makeText(SystemConfig.this,getResources().getString(R.string.systemconfig_set_enddatetime_error_ext), Toast.LENGTH_SHORT).show();
						}
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					editor.putString(strs_sp[3], string);
					// 提交更改
					editor.commit();
					updatalist();
				}
				
			}else{
				
				Toast.makeText(SystemConfig.this,getResources().getString(R.string.systemconfig_set_begindatetime_error), Toast.LENGTH_SHORT).show();
				
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	 
	// updata list
	private void updatalist() {
		// TODO Auto-generated method stub

		initData();
		mAdapter = new ListViewAdapter(strs_detail);
		mlist.setAdapter(mAdapter);
	}

	// init strs2[]
	private void initData() {

		
		strs_detail[0] = preferences.getString(strs_sp[0], "37-06-01-007"); 
		
		strs_detail[1] = nowDate;
		
		//vote begin datetime
		String votebegin = preferences.getString(strs_sp[2], "201001010000");
		try {
			strs_detail[2] = SystemDateTimeformat.format(VoteDateTimeformat.parse(votebegin).getTime());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//vote end datetime  
		String voteEnd = preferences.getString(strs_sp[3], "201712310000");
		try {
			strs_detail[3] = SystemDateTimeformat.format(VoteDateTimeformat.parse(voteEnd).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		strs_detail[4] = preferences.getString(strs_sp[4], "216.24.172.73");
		strs_detail[5] = preferences.getString(strs_sp[5], "6778");
		strs_detail[6] = preferences.getString("CURRENT_ELECTION_TYPE", "Card");
		
		IP_ADDR = preferences.getString("GPRS_IP","216.24.172.73");
		PORT = Integer.parseInt(preferences.getString("GPRS_PORT","6778"));
		
//		strs_detail[7] = preferences.getString(strs_sp[7], noset);
//		strs_detail[8] = preferences.getString(strs_sp[7], noset);
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			if (arg0.length() >= ENTRY_EDITTEXT_COUNT) {
				Toast.makeText(
						SystemConfig.this,
						getResources().getString(R.string.entry_max_num_toast)
								+ ENTRY_EDITTEXT_COUNT, Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
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
	};
	
	private void initEdittextDialogView_IP() {

		DexttextDlg = new AlertDialog.Builder(SystemConfig.this).create(); 
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_EDITTEXT_COUNT_IP) });
		
		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() > ENTRY_EDITTEXT_COUNT_IP) {
					Toast.makeText(
							SystemConfig.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_EDITTEXT_COUNT_IP, Toast.LENGTH_SHORT)
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
					
					if(mid>=48&&mid<=57){ 
					return;
					}
					if(mid==46){ 
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
	
	
	private void initEdittextDialogView_port() {

		DexttextDlg = new AlertDialog.Builder(SystemConfig.this).create();
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });

		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() > ENTRY_EDITTEXT_COUNT) {
					Toast.makeText(
							SystemConfig.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_EDITTEXT_COUNT, Toast.LENGTH_SHORT)
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

	private void initEdittextDialogView() {

		DexttextDlg = new AlertDialog.Builder(SystemConfig.this).create();
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				ENTRY_EDITTEXT_COUNT) });

		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() > ENTRY_EDITTEXT_COUNT) {
					Toast.makeText(
							SystemConfig.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_EDITTEXT_COUNT, Toast.LENGTH_SHORT)
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
	
	private String pucodeFormat(String string) {
		
		string = string.substring(0, 2) + "-" + string.substring(2, 4) + "-" +	string.substring(4, 6) + "-" + string.substring(6); 
		return string;
	}
	
	//
//	
	@SuppressLint("ValidFragment")
	public class TimePickerFragment_begin extends DialogFragment implements
	TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
		
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity())); 
			
		}
	
		@Override
		public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

			 String hour = String.valueOf(arg1);
			 String min = String.valueOf(arg2);
			 if(hour.length() <= 1){
				 hour = "0"+ hour;
			 }
			 if(min.length() <= 1){
				 min = "0"+ min;
			 }
			 voteBeginTime = hour + min;
			 
			 Log.v("crjlog", "voteBeginDate+voteBeginTime = " + voteBeginDate+voteBeginTime);
			 
			 setVoteBeginDateTime(voteBeginDate+voteBeginTime);
			 
		}
 
	}
	
	@SuppressLint("ValidFragment")
	public class TimePickerFragment_end extends DialogFragment implements
	TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
		
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity())); 
			
		}
	
		@Override
		public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

			 String hour = String.valueOf(arg1);
			 String min = String.valueOf(arg2);
			 if(hour.length() <= 1){
				 hour = "0"+ hour;
			 }
			 if(min.length() <= 1){
				 min = "0"+ min;
			 }
			 voteEndTime = hour + min;
			 
			 setVoteEndDateTime(voteEndDate+voteEndTime);
			
		}
	}
	
	
	public static class DatePickerFragment_begin extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Do something with the date chosen by the user
		 String month_ext = String.valueOf(month+1);
		 String day_ext = String.valueOf(day);
		 if(month_ext.length() <= 1){
			 month_ext = "0"+ month_ext;
		 }
		 if(day_ext.length() <= 1){
			 day_ext = "0"+ day_ext;
		 }
		 voteBeginDate = String.valueOf(year) + month_ext + day_ext;
	}
	}
	
	public static class DatePickerFragment_end extends DialogFragment implements
	DatePickerDialog.OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
	
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Do something with the date chosen by the user
		 
		 String month_ext = String.valueOf(month+1);
		 String day_ext = String.valueOf(day);
		 if(month_ext.length() <= 1){
			 month_ext = "0"+ month_ext;
		 }
		 if(day_ext.length() <= 1){
			 day_ext = "0"+ day_ext;
		 }
		 voteEndDate = String.valueOf(year) + month_ext + day_ext;
		 Log.v("crjlog", "voteEndDate = " + voteEndDate);
		
	}
	}
	//
	
	@SuppressLint("ValidFragment")
	public class TimePickerFragment extends DialogFragment implements
	TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
		
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity())); 
			
		}
	
		@Override
		public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, arg1); // 时
			c.set(Calendar.MINUTE, arg2); // 分
			long when = c.getTimeInMillis();
			SystemClock.setCurrentTimeMillis(when);			
			
			getSystemTime();
			updatalist();
		}
	} 

	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
		
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Do something with the date chosen by the user
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);  // 年
		c.set(Calendar.MONTH, month); // 月
		c.set(Calendar.DAY_OF_MONTH, day); // 日
		long when = c.getTimeInMillis();
		SystemClock.setCurrentTimeMillis(when);
	}
	}

	public class ListViewAdapter extends BaseAdapter {
		private String mlistDetail[];
		private LayoutInflater inflater;
		private TextView mNmaeTitle, mDetailTitle;

		public ListViewAdapter(String listname[]) {
			
			mlistDetail = listname;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return strs_name.length;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View itemView = convertView;
			inflater = (LayoutInflater) SystemConfig.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.systemconfig_list, null);

			mNmaeTitle = (TextView) itemView.findViewById(R.id.sclist_name);
			mNmaeTitle.setText(strs_name[position]);
			mDetailTitle = (TextView) itemView.findViewById(R.id.sclist_detail);
			mDetailTitle.setText(mlistDetail[position]);  
			
			return itemView;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub

	}
	
	
	public class MyThread2 implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket mSocket = null;
			String datetime = null;

			DataOutputStream mDataOutputStream = null;
			DataInputStream mDataInputStream = null;
			OutputStream mOutputStream = null;
			BufferedOutputStream fo = null;		
			int bytesRead = 0;
			byte[] buffer = new byte[10 * 1024];

			try {
				mSocket = new Socket();
				mSocket.connect(new InetSocketAddress(IP_ADDR, PORT), 6778);

				mDataOutputStream = new DataOutputStream(
						mSocket.getOutputStream());
				
				mDataOutputStream.writeUTF("Config");
				mOutputStream = mSocket.getOutputStream();
				mOutputStream.flush();
				
				// 一定要加上这句，否则收不到来自服务器端的消息返回
				mSocket.shutdownOutput();
				mDataInputStream = new DataInputStream(mSocket.getInputStream());
				
				
				String votepath = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name);
				File votefile = new File(votepath);
				
				if (!votefile.exists()) {
					votefile.mkdirs();
				}
				
				String path = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name) +"/"+ "Config.txt";
				fo = new BufferedOutputStream(new FileOutputStream(new File(path)));

				while ((bytesRead = mDataInputStream.read(buffer, 0, buffer.length)) != -1) {
					fo.write(buffer, 0, bytesRead);
				}
				fo.flush();
				fo.close();
				
				Message message = Message.obtain(); 
				message.what=2;
				//通过Handler发布传送消息，handler
				mhandler.sendMessage(message);				
				Log.v("crjlog", "receive Config complete!");
				
				//更新系统时间
//				updateSystemDatetime(datetime);
				
				

			} catch (Exception e) {
				// TODO: handle exception

				Log.v("crjlog", "getMessage = " + e.getMessage()); 
				
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
	
	
	public class MyThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket mSocket = null;
			byte[] buffer = null;
			String datetime = null;

			DataOutputStream mDataOutputStream = null;
			DataInputStream mDataInputStream = null;
			OutputStream mOutputStream = null;

			try {
				mSocket = new Socket();
				mSocket.connect(new InetSocketAddress(IP_ADDR, PORT), 6778);

				mDataOutputStream = new DataOutputStream(
						mSocket.getOutputStream());
				
				mDataOutputStream.writeUTF("Time");
				mOutputStream = mSocket.getOutputStream();
				mOutputStream.flush();
				
				// 一定要加上这句，否则收不到来自服务器端的消息返回
				mSocket.shutdownOutput();
				mDataInputStream = new DataInputStream(mSocket.getInputStream());
				
				if (mDataInputStream != null) {
					
					datetime = mDataInputStream.readUTF();
					Log.v("crjlog", "datetime = " + datetime);	// 1 success , 0  fail  
				}
				
				Message message = Message.obtain(); 
				message.what=1;
				//通过Handler发布传送消息，handler
				mhandler.sendMessage(message);				
				Log.v("crjlog", "datetime sending complete!");
				
				//更新系统时间
				updateSystemDatetime(datetime);
				
				

			} catch (Exception e) {
				// TODO: handle exception

				Log.v("crjlog", "Exception = " + e.toString());
				Log.v("crjlog", "getMessage = " + e.getMessage()); 
				
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
	
	private void changeSystemConfigstatus(){
		
		String path = Environment.getExternalStorageDirectory().toString()
				+ "/" + getResources().getString(R.string.app_name) + "/"
				+ "Config.txt";

		
		String path_ext = Environment.getExternalStorageDirectory().toString()
				+ "/" + getResources().getString(R.string.app_name) + "/"
				+ "Config_updated.txt";
		
		try {
			File fconfigTxt1 = new File(path_ext);

			if (fconfigTxt1.exists()) {
				fconfigTxt1.delete();
			}
			
			File fconfigTxt = new File(path);
			File fconfigTxt_ext = new File(path_ext);
			
			fconfigTxt.renameTo(fconfigTxt_ext);

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	
	private boolean configTxtIsExists() {

		try {
			String path = Environment.getExternalStorageDirectory().toString()
					+ "/" + getResources().getString(R.string.app_name) + "/"
					+ "Config.txt";

			File fconfigTxt = new File(path);

			if (!fconfigTxt.exists()) {
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

		return true;

	}
	
	private void updateSystemConfig(){
		
		String path = Environment.getExternalStorageDirectory().toString()+ "/" + getResources().getString(R.string.app_name) +"/"+ "Config.txt";
		try {
			ArrayList<String> list=new ArrayList<String>(); 
			BufferedReader bReader;
			File file=new File(path);
			bReader = new BufferedReader(new FileReader(file));
			String str=null;
			while((str=bReader.readLine())!=null)
			{
				list.add(str);
			}
			
			//将读取的设置保存到systemconfig中
			// 选举类型
			editor.putString("CURRENT_ELECTION_TYPE",list.get(0));
			
//			try {
////				aaa1 = SystemDateTimeformat.parse(list.get(1));
////				
////				String ddd = VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(1)));
//				
//				
//				Log.v("crjlog", "ddd = " + VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(1))));
//			} catch (ParseException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			try {
				// 选举起始时间
//				String aaa = VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(1)));
//				Log.v("crjlog", "aaa = " + aaa);
//				editor.putString(strs_sp[2],VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(1))));
				
				// 选举结束时间
//				String bbb = SystemDateTimeformat.format(VoteDateTimeformat.parse(list.get(2)).getTime());
//				Log.v("crjlog", "bbb = " + VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(2))));
				//editor.putString(strs_sp[3],VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(2))));
				
				
				setVoteBeginDateTime(VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(1))));
				setVoteEndDateTime(VoteDateTimeformat.format(SystemDateTimeformat.parse(list.get(2))));
				
				String fp_value = list.get(3);
				if(isNumeric(fp_value)){
					
					if(Integer.parseInt(fp_value) >= 12 && Integer.parseInt(fp_value) <= 20){
						editor.putString("CURRENT_FINGERPRINT_NUM",fp_value);
					}else{
						Toast.makeText(SystemConfig.this, R.string.finger_matchvlaue_fail, Toast.LENGTH_SHORT).show();
					}
					
				}else{
					
					Toast.makeText(SystemConfig.this, R.string.finger_matchvlaue_fail, Toast.LENGTH_SHORT).show();
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			// IP地址
//			editor.putString(strs_sp[4],list.get(3));
//			// 端口号
//			editor.putString(strs_sp[5],list.get(4));
			// 提交更改
			
			editor.commit();
			
			Toast.makeText(SystemConfig.this, R.string.systemconfig_toast_updateconfig_s, Toast.LENGTH_SHORT).show();
			updatalist();
			
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(SystemConfig.this, R.string.systemconfig_get_sc_fail_nofile_toast, Toast.LENGTH_SHORT).show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(SystemConfig.this, R.string.systemconfig_dialog_set_fail_toast, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	public static boolean isNumeric(String str){
		  for (int i = str.length();--i>=0;){   
		   if (!Character.isDigit(str.charAt(i))){
		    return false;
		   }
		  }
		  return true;
	}
	//更新系统时间
	private void updateSystemDatetime(String time) throws ParseException  {
		
		Log.v("crjlog", "time = " + time);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(SystemDateTimeformat.parse(time));
			long when = c.getTimeInMillis();
			SystemClock.setCurrentTimeMillis(when);
			
		} catch (Exception e) {
			Log.v("crjlog", "Exception = " + e.getMessage());
		}
	}

	private boolean isIpAddress(String value) {

		int start = 0;
		int end = value.indexOf('.');
		int numBlocks = 0;
		while (start < value.length()) {

			if (end == -1) {
				end = value.length();
			}

			try {
				int block = Integer.parseInt(value.substring(start, end));
				if ((block > 255) || (block < 0)) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}

			numBlocks++;
			start = end + 1;
			end = value.indexOf('.', start);

		}
		return numBlocks == 4;
	}

}
