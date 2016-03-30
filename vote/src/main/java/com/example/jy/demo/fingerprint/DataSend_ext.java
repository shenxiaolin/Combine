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
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DataSend_ext extends Activity {
	public static final String IP_ADDR = "192.168.16.59";// 服务器地址
	//	private static final String IP_ADDR = "10.0.2.2";// 服务器地址
	private static final int PORT = 6778;// 服务器端口号
	private Button mDataSendButton_Send, mDataSendButton_Back;
	private TextView mStatus;
	private String result;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datasend);

		mStatus = (TextView) findViewById(R.id.datasend_status);
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
				new Thread(new MyThread()).start();
			}
		});
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

				mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
				mDataOutputStream.writeUTF("log.txt");
				// InputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/"+"nfc.txt");
				// InputStream inputStream = new FileInputStream("system/vote.db");
				mInputStream = new FileInputStream("system/log.txt");
				mOutputStream = mSocket.getOutputStream();

				buffer = new byte[10 * 1028];
				int temp = 0;
				while ((temp = mInputStream.read(buffer)) != -1) {
					mOutputStream.write(buffer, 0, temp);
				}

				mOutputStream.flush();

				// 一定要加上这句，否则收不到来自服务器端的消息返回   
				mSocket.shutdownOutput();

				mDataInputStream = new DataInputStream(mSocket.getInputStream());

				if(mDataInputStream != null){
					String result = mDataInputStream.readUTF();
					Log.v("crjlog", "result = " + result);
				}

				Log.v("crjlog", "sending complete!");
			} catch (Exception e) {
				// TODO: handle exception

				Log.v("crjlog", "Exception = " + e.toString());
				e.printStackTrace();
			} finally {

				if(buffer != null)
					buffer = null;

				if(mOutputStream != null)
					try {
						mOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v("crjlog", "111111111");
						e.printStackTrace();
					}

				if(mInputStream != null)
					try {
						mInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block 
						Log.v("crjlog", "2222222222");
						e.printStackTrace();
					}

				if(mDataOutputStream != null)
					try {
						mDataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v("crjlog", "3333333333");
						e.printStackTrace();
					}

				if(mSocket != null)
					try {
						mSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v("crjlog", "4444444444");
						e.printStackTrace();
					}
			}
		}
	}

}