package com.example.passprotdemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mrzdemo.R;
import com.xd.Converter;

import java.util.Arrays;


public class RfidTestActivity extends Activity {
    private final String TAG = "RfidTest activity";
    private Button btn_RfidOpen, btn_RfidInit, btn_GetSnr, btn_Rats;
    private EditText et_apdu;
    private Button btn_RfApdu, btn_RfidClose, btn_RfClose;
    private TextView tv_OpenEcho, tv_RfidInit, tv_GetSnr, tv_Rats;
    private TextView tv_RfApdu, tv_RfidClose, tv_RfClose;
    private int nRet = 0;
    String strOut;
    byte[] bIdLen = new byte[1];
    byte[] bSNR = new byte[64];
    long t0, t1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rfidtest);

        //根据id获取对象  
        btn_RfidOpen = (Button) this.findViewById(R.id.button_rfidopen);
        btn_RfidInit = (Button) this.findViewById(R.id.button_rfidinit);
        btn_GetSnr = (Button) this.findViewById(R.id.button_getsnr);
        btn_Rats = (Button) this.findViewById(R.id.button_rats);
        et_apdu = (EditText) this.findViewById(R.id.editText_rfapdu);
        btn_RfApdu = (Button) this.findViewById(R.id.button_rfapdu);
        btn_RfidClose = (Button) this.findViewById(R.id.button_rfidclose);
        btn_RfClose = (Button) this.findViewById(R.id.button_rfclose);


        tv_OpenEcho = (TextView) this.findViewById(R.id.textView_openecho);
        tv_RfidInit = (TextView) this.findViewById(R.id.textView_initecho);
        tv_GetSnr = (TextView) this.findViewById(R.id.textView_getsnr);
        tv_Rats = (TextView) this.findViewById(R.id.textView_rats);
        tv_RfApdu = (TextView) this.findViewById(R.id.textView_rfapduecho);
        tv_RfidClose = (TextView) this.findViewById(R.id.textView_rfidclose);
        tv_RfClose = (TextView) this.findViewById(R.id.textView_rfcloseEcho);

        //设置获取焦点
        btn_RfidOpen.setFocusable(true);
        btn_RfidOpen.setFocusableInTouchMode(true);
        btn_RfidOpen.requestFocus();
        btn_RfidOpen.requestFocusFromTouch();


        btn_RfidOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                nRet = com.xd.rfid.RFIDModuleOpen();
                if (nRet == 0) {
                    strOut = "Open OK !";
                } else {
                    strOut = "Open failed, nRet=" + nRet;
                }
                tv_OpenEcho.setText(strOut);
            }
        });


        btn_RfidInit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                nRet = com.xd.rfid.RFIDInit();
                if (nRet == 0) {
                    strOut = "Init OK !";
                    nRet = com.xd.rfid.RFIDTypeSet(0);
                    if (nRet != 0) {
                        strOut = "Init failed, nRet=" + nRet;
                    }
                } else {
                    strOut = "Init failed, nRet=" + nRet;
                }
                tv_RfidInit.setText(strOut);
            }
        });

        btn_GetSnr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
//				byte[] bIdLen = new byte[1];
//				byte[] bSNR = new byte[64];
                byte sak = 0;
                short atqa = 0;
                String cardType = null;
                String m1para = null;
                byte[] bId = new byte[4];

                t0 = System.currentTimeMillis();
                nRet = com.xd.rfid.RFIDGetSNR(0, bIdLen, bSNR);
                t1 = System.currentTimeMillis();
                if (nRet == 0) {
                    strOut = "SNR=" + Converter.printHexLenString(bSNR, bIdLen[0]) + ",  len=" + Converter.printHexLenString(bIdLen, 1) +
                            ", time=" + (t1 - t0) + "(ms)";

                    sak = bSNR[bIdLen[0] - 3];
                    atqa = (short) (bSNR[bIdLen[0] - 1] * 256 + bSNR[bIdLen[0] - 2]);
                    if (((sak & 0x20) == 0x20) || (sak == 0x53)) {
                        if (atqa == 0x0344) {
                            //Desfire
                            cardType = ", Desfire card";
                        } else {
                            //CPU
                            cardType = ", CPU card";
                        }
                    } else if (atqa == 0x0044) {
                        //UL
                        cardType = ", UL card";
                    } else if ((sak == 0x08) || (sak == 0x18)) {

                        //M1 S50 / S70
                        if (sak == 0x08) {
                            cardType = ", S50 card";
                        } else {
                            cardType = ", S70 card";
                        }

                    } else {
                        cardType = ", Unknown card";
                    }

                    strOut += cardType;
                } else {
                    strOut = "GetSNR failed, nRet=" + nRet;
                }
                tv_GetSnr.setText(strOut);
            }
        });


        btn_Rats.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

				
/*
                byte[] bKey = new byte[6];
				Arrays.fill(bKey, (byte)0xFF);
				byte[] bLocalId = new byte[4];
				bLocalId[0] = bSNR[0];
				bLocalId[1] = bSNR[1];
				bLocalId[2] = bSNR[2];
				bLocalId[3] = bSNR[3];
				byte[] bOutData = new byte[16];
				
				t0 = System.currentTimeMillis();
				nRet = com.xd.rfid.MifAuthen((byte)0x0A, (byte)0, bKey, bLocalId);
				t1 = System.currentTimeMillis();
				if (nRet != 0)
				{
					Log.v(TAG, "MifAuthen error, nRet="+nRet);
					
				}
				else
				{
					Log.v(TAG, "MifAuthen time=" + (t1-t0) + "(ms)");
					strOut = "Data:\r\n";
					
					t0 = System.currentTimeMillis();
					for (int i=0; i<4; i++)
					{
						nRet = com.xd.rfid.MifRead((byte)i, bOutData);
						//nRet = com.xd.rfid.MifWrite(byte blockNo, byte[] bInData);
						if (nRet == 0)
						{
							strOut += Converter.printHexLenString(bOutData, 16);
							strOut += "\r\n";
						}
						else
						{
							strOut = "MifRead failed, i=" + i + ", nRet=" + nRet;
							break;
						}
					}
					t1 = System.currentTimeMillis();
					strOut += ("Read time=" + (t1-t0) + "(ms)");
					tv_Rats.setText(strOut);
				}
*/


                // TODO Auto-generated method stub
                byte[] bRats = new byte[32];
                Arrays.fill(bRats, (byte) 0);
                t0 = System.currentTimeMillis();
                nRet = com.xd.rfid.RFIDTypeARats(0, bRats);
                t1 = System.currentTimeMillis();
                if (nRet == 0) {
                    strOut = "ATS=" + Converter.printHexLenString(bRats, 32) + ", time=" + (t1 - t0) + "(ms)";

                } else {
                    strOut = "Rats failed, nRet=" + nRet;
                }
                tv_Rats.setText(strOut);


            }
        });


        //et_apdu.setText("00A40000023F00");
        et_apdu.setText("0084000004");    //get random num
        et_apdu.setText("00A4040007A0000002471001");

        tv_RfApdu = (TextView) findViewById(R.id.textView_rfapduecho);
        tv_RfApdu.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);    //下划线
        tv_RfApdu.setTextColor(Color.BLUE);


        btn_RfApdu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                byte[] bCmd = new byte[255];
                int inLen = 0;
                byte[] bSw = new byte[2];
                byte[] bOutData = new byte[255];
                byte[] bOutLen = new byte[1];

                bCmd = Converter.hexStringToBytes(et_apdu.getText().toString());
                inLen = et_apdu.getText().toString().length() / 2;

                t0 = System.currentTimeMillis();
                nRet = com.xd.rfid.RFIDRfApdu(0, bCmd, inLen, bOutData, bOutLen, bSw);
                t1 = System.currentTimeMillis();
                if (nRet == 0) {
                    strOut = "SW=" + Converter.printHexLenString(bSw, 2);
                    if (bOutLen[0] > 0) {
                        strOut += (",  echoData=" + Converter.printHexLenString(bOutData, bOutLen[0]));
                        strOut += (",  echoLen=" + Converter.printHexLenString(bOutLen, 1));
                        strOut += (",  Apdu time=" + (t1 - t0) + "(ms)");
                    }
                } else {
                    strOut = "Rfid Apdu failed, nRet=" + nRet;
                }
                tv_RfApdu.setText(strOut);
            }
        });


        btn_RfidClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                nRet = com.xd.rfid.RFIDMoudleClose();
                if (nRet == 0) {
                    strOut = "Close OK !";
                } else {
                    strOut = "Close failed, nRet=" + nRet;
                }
                tv_RfidClose.setText(strOut);
            }
        });


        btn_RfClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                nRet = com.xd.rfid.RFIDRfClose();
                if (nRet == 0) {
                    strOut = "Field Off OK !";
                } else {
                    strOut = "Field Off failed, nRet=" + nRet;
                }
                tv_RfClose.setText(strOut);
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
