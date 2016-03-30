/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;

import com.accessltd.device.AccessParserNDKInterface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	
	private AccessParserNDKInterface accessParserNDKInterface = new AccessParserNDKInterface();

	public static final String[] OCR_PARSED_FIELD_NAMES = {
		"DOB",
		"Expiry",
		"Issuer",
		"Document Type",
		"Last Name",
		"First Name",
		"Nationality",
		"Discretionary",
		"Discretionary2",
		"Document Number",
		"Sex"
	};

	public static String newline = System.getProperty("line.separator");


	private class ReadThread extends Thread {

		@SuppressLint("NewApi")
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					
					size = mInputStream.read(buffer);
					
					if (size > 0) {
						onDataReceived(buffer, size);
					}

					
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*try {
			mSerialPort = new SerialPort(new File("/dev/ttyMT0"), 19200, 0); 
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}*/
	}

	
		@Override
		protected void onResume() {
			super.onResume();
			try {
				mSerialPort = new SerialPort(new File("/dev/ttyMT0"), 19200, 0); 
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
			
				/* Create a receiving thread */
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (SecurityException e) {
				DisplayError(R.string.error_security);
			} catch (IOException e) {
				DisplayError(R.string.error_unknown);
			} catch (InvalidParameterException e) {
				DisplayError(R.string.error_configuration);
			}
	}

	// protected abstract void onDataReceived(final byte[] buffer, final int
	// size, final byte[] new_bts2, final int length);
	protected abstract void onDataReceived(final byte[] buffer, final int size);

//	protected void onDataReceived(byte[] buffer, int size, byte[] new_bts2,
//			int length, String text2) {
		// TODO Auto-generated method stub
		
//	}
	
	@Override
	protected void onDestroy() {
		
		if (mReadThread != null)
			mReadThread.interrupt();
		
		if(mSerialPort !=null){
			mSerialPort.close();
			mSerialPort = null;
		}
		
		super.onDestroy();
	}
}
