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

import java.io.IOException;
import java.util.Arrays;

import com.accessltd.device.AccessParserNDKInterface;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import android.media.ToneGenerator;

import android_serialport_api.SerialPort;

public class ConsoleActivity extends SerialPortActivity {

	EditText mReception, Emission, Emission_Parser;

	private Button mButton_clean, mButton_back;// ,mButton_regi;
//	WriteThread mWriteThread;
	byte[] mBuffer;
	private TextView passprot_name1,passprot_name2, passprot_sex,
					 passprot_nationality, passprot_date_of_birth,
					 passprot_discretionary, passprot_discretionary2,
					 passprot_place_of_issue, passprot_date_of_expiry,
					 passprot_type, passprot_country_code, passprot_passport_no,
					 passprot_mrz_code1, passprot_mrz_code2;// ,mButton_regi;

	String String2 = "";

	private AccessParserNDKInterface accessParserNDKInterface = new AccessParserNDKInterface();

	public static final String[] OCR_PARSED_FIELD_NAMES = { "DOB", "Expiry",
			"Issuer", "Document Type", "Last Name", "First Name",
			"Nationality", "Discretionary", "Discretionary2",
			"Document Number", "Sex" };
	private String dob = "";
	private String expiry = "";
	private String issuer = "";
	private String Passport_type = "";
	private String lastname = "";
	private String firstname = "";
	private String nationality = "";
	private String discretionary = "";
	private String discretionary2 = "";
	private String passport_num = "";
	private String sex = "";

	public static StringBuffer stringBuffer = new StringBuffer();

	public static String newline = System.getProperty("line.separator");
	private static audioPlay ap = new audioPlay();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);

		// setTitle("Loopback test");
		mReception = (EditText) findViewById(R.id.EditTextReception);
		Emission = (EditText) findViewById(R.id.EditTextEmission);
//		Emission_ext = (EditText) findViewById(R.id.EditTextEmission_ext);
		
		//Emission_Parser = (EditText) findViewById(R.id.EditTextEmission_Parser); huangmin del
		
		

		mButton_back = (Button) this.findViewById(R.id.button_back);
		mButton_clean = (Button) this.findViewById(R.id.button_clean);

		passprot_name1 = (TextView)this.findViewById(R.id.content_name1);
		
		passprot_name2 = (TextView)this.findViewById(R.id.content_name2);
		passprot_sex = (TextView)this.findViewById(R.id.content_sex);
		passprot_nationality = (TextView)this.findViewById(R.id.content_nationality);
		passprot_date_of_birth = (TextView)this.findViewById(R.id.content_date_of_birth);
		passprot_discretionary = (TextView)this.findViewById(R.id.content_Discretionary);
		passprot_discretionary2 = (TextView)this.findViewById(R.id.content_Discretionary2);
		//passprot_place_of_issue = (TextView)this.findViewById(R.id.content_place_of_issue);
		passprot_date_of_expiry = (TextView)this.findViewById(R.id.content_date_of_expiry);

		passprot_type = (TextView)this.findViewById(R.id.content_type);
		passprot_country_code= (TextView)this.findViewById(R.id.content_country_code);
		passprot_passport_no= (TextView)this.findViewById(R.id.content_passport_no);
		passprot_mrz_code1= (TextView)this.findViewById(R.id.content_mrz_code1);
		passprot_mrz_code2 = (TextView)this.findViewById(R.id.content_mrz_code2);

		
		mButton_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				finish();

			}
		});

		mButton_clean.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passprot_name1.setText("");
				
				passprot_name2.setText("");
				passprot_sex.setText("");
				passprot_nationality.setText("");
				passprot_date_of_birth.setText("");
				passprot_discretionary.setText("");
				passprot_discretionary2.setText("");
				//passprot_place_of_issue.setText("passprot_place_of_issue");
				passprot_date_of_expiry.setText("");
				
				passprot_type.setText("");
				passprot_country_code.setText("");
				passprot_passport_no.setText("");
				passprot_mrz_code1.setText("");
				passprot_mrz_code2.setText("");
				stringBuffer.setLength(0);

				mReception.setText("");
				Emission.setText("");
//				Emission_ext.setText("");
				//Emission_Parser.setText(""); huangmin del

			}
		});

//		// william start
//		mBuffer = new byte[1024];
//		Arrays.fill(mBuffer, (byte) 0xff);
		//if (mSerialPort != null) {
//			mWriteThread = new WriteThread();
//			mWriteThread.start();
		//		mSerialPort.ocr315powerswitch(1);
				// william power on
	//	}
//		// william end
	}

	public static String asciiToString(String value)  
	{  
	    StringBuffer sbu = new StringBuffer();  
	    String[] chars = value.split(",");  
	    for (int i = 0; i < chars.length; i++) {  
	        sbu.append((char) Integer.parseInt(chars[i]));  
	    }  
	    return sbu.toString();  
	} 


	@Override
	protected void onResume() {
		super.onResume();
		android.util.Log.d("huangmin","onresume");
		if (mSerialPort != null) {
//			mWriteThread = new WriteThread();
//			mWriteThread.start();
				
			   mSerialPort.ocr315powerswitch(1);
				Toast.makeText(ConsoleActivity.this, R.string.device_open,
						Toast.LENGTH_SHORT).show();

				// william power on
		}
	}
	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() { 
			@SuppressLint("NewApi")
			
			public void run() {
				String String3 = null;
				String String4 = null;
				String resultString_ext = "";
				if((buffer[0] == 28)){//起始位
					mReception.setText("");
					Emission.setText("");
					passprot_name1.setText("");
					
					stringBuffer.setLength(0);
					passprot_name2.setText("");
					passprot_sex.setText("");
					passprot_nationality.setText("");
					passprot_date_of_birth.setText("");
					passprot_discretionary.setText("");
					passprot_discretionary2.setText("");
					//passprot_place_of_issue.setText("passprot_place_of_issue");
					passprot_date_of_expiry.setText("");
					
					passprot_type.setText("");
					passprot_country_code.setText("");
					passprot_passport_no.setText("");
					
					passprot_mrz_code1.setText("");
					passprot_mrz_code2.setText("");
					//Emission_Parser.setText("");huangmin del
				}
				//if (mReception != null) {
				
	 				String regex = "[^\\p{Graph}\\s]";
	 				String string1 = new String(buffer, 0, size);
	 				string1 = string1.replaceAll(regex,"");
					stringBuffer.append(string1);
					String[] temp = null;
	 				//mReception.append(string1);
						

					if(buffer[size - 1] == 29){//结束位
					
						String string_enter = asciiToString("13");//回车的ascii码转字符串
						temp = stringBuffer.toString().split(string_enter);
						if(temp.length > 1){
							String3 = temp[0];
							String4 = temp[1];
						}
						if(String3 != null){
							passprot_mrz_code1.setText(String3);
						}
						
						if(String4 != null){
							passprot_mrz_code2.setText(String4);
						}
						
						stringBuffer.setLength(0);
						
					}
				
				if(String3 != null && String4 != null){
					String resultString = "";
					String[] token = null;
					try {
						resultString = accessParserNDKInterface.AccessHIDParseOCR(String3, String4, "", true);
						
	
						token = resultString.split("\n");		// Don't split on /r as all lines must exist
						
						dob = token[0];
						expiry = token[1];
						issuer = token[2];
						Passport_type = token[3];
						lastname = token[4];
						firstname = token[5];
						nationality = token[6];
						discretionary = token[7];
						discretionary2 = token[8];
						passport_num = token[9];
						sex = token[10];
						
						
						try {
							ap.PlayTone(ToneGenerator.TONE_PROP_BEEP, 500);
						} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
					catch (Exception ex)
					{
						stringBuffer.setLength(0);
						
						VibratorUtil.Vibrate(ConsoleActivity.this, 100);
						Toast.makeText(ConsoleActivity.this, R.string.AccessDataOCRLinesRx_fail,
								Toast.LENGTH_SHORT).show();
						Log.d("huangmin", "AccessDataOCRLinesRx - Error - " + ex.toString());					}
	
					
					/*try { 
					for (int Counter = 0; Counter < OCR_PARSED_FIELD_NAMES.length; Counter++) {
	
	
							if (Counter < token.length)
							{
								// End tokens that are empty are not part of the array
	
								resultString_ext += OCR_PARSED_FIELD_NAMES[Counter] + ": " + token[Counter] + newline;	
								Log.v("huangmin","resultString_ext  = " + resultString_ext);
							}
						}
	
					}
					catch (Exception ex)
					{
						Log.d("huangmin", "AccessDataOCRLinesRx - Error - " + ex.toString());
					}
					*/
					passprot_name1.setText(firstname);
					
					passprot_name2.setText(lastname);
					passprot_sex.setText(sex);
					passprot_nationality.setText(nationality);
					passprot_date_of_birth.setText(dob);
					passprot_discretionary.setText(discretionary);
					passprot_discretionary2.setText(discretionary2);
					//passprot_place_of_issue.setText("passprot_place_of_issue");
					passprot_date_of_expiry.setText(expiry);
					
					passprot_type.setText(Passport_type);
					passprot_country_code.setText(issuer);
					passprot_passport_no.setText(passport_num);
					
					
					
				}
				if (Emission != null) {
					if(String4 != null){
						Emission.setText(String4);
					}
				}

//				if (Emission_ext != null) {
//					if(text3 != null)
//					Emission_ext.append(text3);
//				}
				
				/*if (Emission_Parser != null) {
					if(resultString_ext != null)
					Emission_Parser.setText(resultString_ext);
				}*///huangmin del

			}
		});
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if (mSerialPort != null) {

			mSerialPort.ocr315powerswitch(0);
			mSerialPort.close();
			mSerialPort = null; 
			// william power off

		}
//		Process su;
//		try {
//			Log.v("crjlog", " Runtime.getRuntime/system/bin/tda8026_app");
//			su = Runtime.getRuntime().exec("/system/bin/tda8026_app");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
