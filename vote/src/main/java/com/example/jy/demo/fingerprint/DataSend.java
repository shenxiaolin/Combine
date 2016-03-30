package com.example.jy.demo.fingerprint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler.Value;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DataSend extends Activity {
//	public static final String IP_ADDR = "192.168.16.51";// 服务器地址
	// private static final String IP_ADDR = "10.0.2.2";// 服务器地址
//	private static final int PORT = 8080;// 服务器端口号

	private static int PORT;// 服务器端口号
	public static String IP_ADDR,PU_CODE;
	public static final String fileName = "Vote.db";
	public static final String filePath = "/databases/Vote.db";

	private String SYSTEM_CONFIG_PUCODE = "PU_CODE";
	private Button mDataSendButton_Send, mDataSendButton_Back;
	private TextView mStatus,mError;
	private int Status;
	private Handler mhandler;
	private ProgressDialog mpDialog;

	private SharedPreferences preferences;

	private VoteVin_DBHelper mVoteDB;
	private Cursor mCursor = null;

	private Vote_DBHelper mVoteDB_log;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datasend);

		mVoteDB_log = new Vote_DBHelper(this);
		mVoteDB = new VoteVin_DBHelper(this);

		mStatus = (TextView) findViewById(R.id.datasend_status);
		mError = (TextView) findViewById(R.id.datasend_error_text);
		mError.setVisibility(View.GONE);

		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		PU_CODE = preferences.getString(SYSTEM_CONFIG_PUCODE,"34-16-10-003");
		IP_ADDR = preferences.getString("GPRS_IP","216.24.172.73");
		PORT = Integer.parseInt(preferences.getString("GPRS_PORT","6778"));

		mDataSendButton_Back = (Button) findViewById(R.id.datasend_button_back);
		mDataSendButton_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mDataSendButton_Send = (Button) findViewById(R.id.datasend_button_send);
		mDataSendButton_Send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mCursor = mVoteDB.Query_Vin_table();
				if (mCursor.getCount() == 0) {// 默认第一次写入一条admin的记录

					Toast.makeText(DataSend.this, R.string.datasend_toast_nodata, Toast.LENGTH_SHORT).show();

				}else{
					mStatus.setText(getResources().getString(R.string.datasend_text_status_process));
					mError.setVisibility(View.GONE);

					mpDialog = new ProgressDialog(DataSend.this);
					mpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mpDialog.setMessage(getResources().getString(R.string.datasend_progressdialog_text));
					mpDialog.setMax(100);
					mpDialog.setIndeterminate(false);
					mpDialog.setCanceledOnTouchOutside(false);
					mpDialog.show();

					new Thread(new MyThread()).start();
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

						mStatus.setText(getResources().getString(R.string.datasend_text_status_fail));
						mError.setVisibility(View.VISIBLE);
						mError.setText(msg.obj.toString());
//					Toast.makeText(DataSend.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
						Toast.makeText(DataSend.this, R.string.download_new_software_f, Toast.LENGTH_SHORT).show();

						if(mpDialog != null)
							mpDialog.dismiss();

						mhandler.removeMessages(3);
						break;

					case 1:

						mStatus.setText(getResources().getString(R.string.datasend_text_status_success));
						mError.setVisibility(View.GONE);
//					Toast.makeText(DataSend.this, R.string.datasend_text_status_success, Toast.LENGTH_LONG).show();

						//系统日志 传输数据
						mVoteDB_log.insert_syslogtable(preferences.getString("last_login_username","Admin"),getResources().getString(R.string.System_Log_event_transfer));

						if(mpDialog != null)
							mpDialog.dismiss();

						mhandler.removeMessages(3);
						break;

					case 3:

						mpDialog.setProgress(msg.arg1);

					default:
						break;
				}

			}
		};

	}

	public class MyThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket mSocket = null;
			byte[] buffer = null;

			DataOutputStream mDataOutputStream = null;
			DataInputStream mDataInputStream = null;
			InputStream mInputStream = null;
			OutputStream mOutputStream = null;

			try {
				mSocket = new Socket();
				mSocket.connect(new InetSocketAddress(IP_ADDR, PORT), 6778);

				mDataOutputStream = new DataOutputStream(
						mSocket.getOutputStream());

				String mData = "Data_" + PU_CODE;
				Log.v("crjlog", "mData = " + mData);

//				mDataOutputStream.writeUTF(fileName);
				mDataOutputStream.writeUTF(mData);

//				mInputStream = new FileInputStream("system/"+fileName);
				mInputStream = new FileInputStream(getFilesDir().getParent().toString() + filePath);///data/data/com.example.jy.demo.fingerprint/databases/vote.db
				mOutputStream = mSocket.getOutputStream();

				buffer = new byte[10*1024];
				int temp = 0;
				int Allcount = mInputStream.available();

				while ((temp = mInputStream.read(buffer)) != -1) {

					dataEncDec(buffer, 3);
					mOutputStream.write(buffer, 0, temp);

					int result = Allcount - mInputStream.available();
					Message message2 = Message.obtain();
					message2.what=3;
					message2.arg1 = (result*100)/Allcount;
					mhandler.sendMessage(message2);

				}

				mOutputStream.flush();

				// 一定要加上这句，否则收不到来自服务器端的消息返回
				mSocket.shutdownOutput();
				mDataInputStream = new DataInputStream(mSocket.getInputStream());

				if (mDataInputStream != null) {
					Status = mDataInputStream.readInt();
					Log.v("crjlog", "Status = " + Status);	// 1 success , 0  fail  
				}

				Log.v("crjlog", "sending complete!");

				if(Status == 1){
					Message message = Message.obtain();
					message.what=1;
					//通过Handler发布传送消息，handler
					mhandler.sendMessage(message);
					Log.v("crjlog", "reveice complete!");
				}

			} catch (Exception e) {
				// TODO: handle exception


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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(mCursor != null)
			mCursor.close();
	}

}