package com.example.jy.demo.passport;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.example.jy.demo.passport.R;
import com.xd.rfid;
import com.xd.Converter;


public class SimTestActivity extends Activity {
	private final String TAG1 = "SimTestActivity";
	//声明spinner对象  
	private Spinner spinner1,spinner2,spinner3;
	private Button btn_reset, btn_apdu;
	private byte st_slot = 0;
	private int st_baut = 0;
	private int st_volt = 0;
	private int nRet = 0;
	private TextView mTvRestEcho;
	private TextView mTvCmdEcho;
	private EditText mEtCmd;
	byte[] bATR = new byte[64];
	byte[] bATRlen = new byte[1];
	String strOut;
	long t0, t1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simtest);

		com.xd.rfid.samOpen(1);
		//根据id获取对象
		spinner1=(Spinner) findViewById(R.id.spinner_slot);
		spinner2=(Spinner) findViewById(R.id.spinner_baut);
		spinner3=(Spinner) findViewById(R.id.spinner_volt);

		//设置获取焦点
		spinner1.setFocusable(true);
		spinner1.setFocusableInTouchMode(true);
		spinner1.requestFocus();
		spinner1.requestFocusFromTouch();

		//显示的数组
		final String arr1[]=new String[]{
				"1",
				"2",
				"3",
				"4"
		};

		final String arr2[]=new String[]{
				"9600",
				"19200",
				"38400",
				"115200"
		};

		final String arr3[]=new String[]{
				"1.8v",
				"3.3v",
				"5.0v"
		};

		//arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//设置显示的数据
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, arr1);
		spinner1.setAdapter(arrayAdapter);

		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, arr2);	//simple_spinner_item
		spinner2.setAdapter(arrayAdapter);

		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, arr3);
		spinner3.setAdapter(arrayAdapter);

//      Toast.makeText(getApplicationContext(), "main Thread"+spinner1.getItemIdAtPosition(spinner1.getSelectedItemPosition()), Toast.LENGTH_LONG).show();  

		//下拉列表框注册事件, 当选择项变化时, 接收消息处理
		spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Spinner spinner1=(Spinner) parent;
				//Toast.makeText(getApplicationContext(), "xxxx"+spinner1.getItemAtPosition(position), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//Toast.makeText(getApplicationContext(), "没有改变的处理", Toast.LENGTH_LONG).show();
			}

		});


		//设置cos 默认命令
		mEtCmd = (EditText) findViewById(R.id.editText_cmd);
		mEtCmd.setText("00A40000023F00");
		//mEtCmd.setText("0084000004");	//get random num

		//按钮处理
		btn_reset = (Button) this.findViewById(R.id.button_reset);
		btn_apdu = (Button) this.findViewById(R.id.button_apdu);

		btn_reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				st_slot = (byte) Integer.parseInt(spinner1.getSelectedItem().toString());
				st_baut =        Integer.parseInt(spinner2.getSelectedItem().toString());
				st_volt =        spinner3.getSelectedItemPosition() + 1;	//base from 0, so add 1
				//Integer.parseInt(spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString())
				Log.i(TAG1, "slot="+st_slot+" baut="+st_baut+" volt="+st_volt);


				mTvRestEcho = (TextView) findViewById(R.id.textView_para);
				mTvRestEcho.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);	//下划线
				mTvRestEcho.setTextColor(Color.BLUE);
//				mTvRestEcho.setText("Slot="+st_slot+" baut="+st_baut+ " volt="+st_volt);

				t0 = System.currentTimeMillis();
				nRet = com.xd.rfid.samReset( st_slot, st_baut, st_volt, bATR, bATRlen);
				t1 = System.currentTimeMillis();
				if (nRet == 0)
				{
					strOut = "ATR=" + Converter.printHexLenString(bATR, bATRlen[0]) +
							",  Len=" + bATRlen[0] + ", time=" + (t1-t0) + "(ms)";
				}
				else
				{
					strOut = "sim Card slot"+st_slot+" reset failed, nRet="+nRet;
				}
				mTvRestEcho.setText(strOut);
			}
		});


		btn_apdu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				mTvCmdEcho = (TextView) findViewById(R.id.textView_cmdEcho);
				mTvCmdEcho.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);	//下划线
				mTvCmdEcho.setTextColor(Color.BLUE);

				byte[] bCmd = null;
				byte[] cosResponse = new byte[255];
				short[] cosResLenth = new short[1];
				//short[] cosSW = new short[1];
				byte[] bSW = new byte[2];

				//bCmd = mEtCmd.getText().toString().getBytes();	//just change "00A400" to 303041343030, not 00A400
				//strOut = "slot="+st_slot+" cmd=" + mEtCmd.getText().toString() + " len=" + bCmd.length;
				bCmd = Converter.hexStringToBytes(mEtCmd.getText().toString());		//change "00A400" to 00A400

				t0 = System.currentTimeMillis();
				nRet = com.xd.rfid.samApdu(st_slot, bCmd, (short)bCmd.length, cosResponse, cosResLenth, bSW );
				t1 = System.currentTimeMillis();
				if (nRet == 0)
				{
					//bSW[0] = (byte)( cosSW[0] & 0xff00 >> 8);
					//bSW[1] = (byte)( cosSW[0] & 0x00ff);
//					strOut = "Apdu [" + mEtCmd.getText().toString() + "] success, sw=[" + Converter.printHexLenString(bSW, 2) + "] Cmd Echo=[" + Converter.printHexLenString(cosResponse, (int)cosResLenth[0]) + "]";
					strOut = "sim Apdu OK, SW=" + Converter.printHexLenString(bSW, 2) +
							",  EchoData=" + Converter.printHexLenString(cosResponse, (int)cosResLenth[0]) +
							",  EchoLen=" + cosResLenth[0] + ", time=" + (t1-t0) + "(ms)";
				}
				else
				{
					strOut = "sim Apdu failed, nRet="+nRet;
				}

				mTvCmdEcho.setText(strOut);
			}
		});


	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


}
