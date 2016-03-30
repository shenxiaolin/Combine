package com.example.jy.demo.fingerprint;

//import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.xd.Converter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class M1  extends Activity {
	private final String TAG = "M1 activity";
	private ListView lv_data;
	private Button btn_read, btn_write;
	private TextView tv_m1para, tv_status;
	private EditText et_data;
	private int nRet = 0;
	private String strOut = null;
	private String[] strs = null;
	private byte[] m1Id = new byte[4];
	private ArrayAdapter<String> adapter;
	private long t0, t1;
	private byte flag_4k = 0;
	private int nSector = 16;
	private int nBlock = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1);

		tv_m1para = (TextView) this.findViewById(R.id.textView_snr);
		lv_data = (ListView) this.findViewById(R.id.listView1);
		et_data = (EditText) this.findViewById(R.id.editText_data);
		btn_read = (Button) this.findViewById(R.id.button_read);
		btn_write = (Button) this.findViewById(R.id.button_write);
		tv_status = (TextView) this.findViewById(R.id.textView_status);

		//设置获取焦点
		btn_read.setFocusable(true);
		btn_read.setFocusableInTouchMode(true);
		btn_read.requestFocus();
		btn_read.requestFocusFromTouch();

		//取得启动该Activity的Intent对象
		Intent intent = getIntent();
        /*取出Intent中附加的数据*/
		String m1para = intent.getStringExtra("M1Para");
		//判断是 1K 还是  4K 的卡
		if (m1para.indexOf("S70 card") > 0)
		{
			flag_4k = 1;
			nSector = 40;
		}

		m1Id = intent.getByteArrayExtra("M1Id");

		tv_m1para.setText(m1para);
		et_data.setText("00112233445566778899AABBCCDDEE");

		//写入数据设置为绿色
		et_data.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);	//下划线
		et_data.setTextColor(Color.GREEN);

		//状态信息提示设置为蓝色
		tv_status.setTextColor(Color.RED);	//Color.BLUE


		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);	//simple_expandable_list_item_1  

		btn_read.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				byte[] bKey = new byte[6];
				byte[] bOutData = new byte[16];
				byte errflag = 0;
				int blockIndex = 0, sectorNumber=0;


				// S50 的卡, 16 扇区;  S70的卡, 40扇区
				t0 = System.currentTimeMillis();
				for(int i=0; i<nSector; i++)
				{
					long t3 = System.currentTimeMillis();

					Arrays.fill(bKey, (byte)0xFF);

					if (i > 31)
					{
						sectorNumber = 32 + (i - 32)*4;
						nRet = com.xd.rfid.MifAuthen((byte)0x0A, (byte)sectorNumber, bKey, m1Id);
					}
					else
					{
						if(i == 0)
						{
							System.arraycopy(m1Id, 0, bKey, 0, 4);

							bKey[4] = 0x20;
							bKey[5] = 0x12;

							nRet = com.xd.rfid.MifAuthen((byte)0x0A, (byte)i, bKey, m1Id);
						}
						else
						{
							nRet = com.xd.rfid.MifAuthen((byte)0x0A, (byte)i, bKey, m1Id);
						}
					}

					long t4 = System.currentTimeMillis();
					Log.i(TAG, "MifRead time=" + (t4-t3) + "(ms)");

					if (nRet != 0)
					{
						strOut = "MifAuthen sector(" + i + ") failed, nRet=" + nRet;
						tv_status.setText(strOut);
						errflag = 1;
						break;
					}
					else
					{
						if (i > 31)
						{
							nBlock = 16;
						}

						for (int j=0; j<nBlock; j++)
						{
							blockIndex = (i * nBlock + j);
							if (i > 31)
							{
								blockIndex = 32*4 + (i - 32)*nBlock + j;
							}

							long t5 = System.currentTimeMillis();
							nRet = com.xd.rfid.MifRead(blockIndex, bOutData);
							long t6 = System.currentTimeMillis();
							Log.i(TAG, "MifRead time=" + (t6-t5) + "(ms)");

							if(nRet != 0)
							{
								strOut = "MifRead block(" + blockIndex + ") failed, nRet=" + nRet;
								tv_status.setText(strOut);
								errflag = 2;
								break;
							}

							DecimalFormat df = new DecimalFormat();
							String style = "000";
							df.applyPattern(style);
							//df.format(blockIndex);

							adapter.add("Blk(" + df.format(blockIndex) + ") " + Converter.printHexLenString(bOutData, 16));
							//为ListView设置 Adapter来绑定数据
							Log.d("kobe","Converter.printHexLenString(bOutData, 16)= "+Converter.printHexLenString(bOutData, 16));

						}//for j
						if (errflag != 0)	break;

						lv_data.setAdapter(adapter);

					}

				}//for i

				if (errflag == 0)
				{
					t1 = System.currentTimeMillis();
					tv_status.setText("time=" + (t1-t0) + "(ms)");
					Log.i(TAG, "Total Time=" + (t1-t0) + "(ms)");
				}
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
