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

package com.example.passprotdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accessltd.device.AccessParserNDKInterface;
import com.example.helper.ReadCardKit;
import com.example.mrzdemo.R;

import java.io.IOException;


public class PassprotActivity extends SerialPortActivity {

    private ImageView image_photo = null;
    private TextView tip_name1, tip_sex,
            tip_nationality, tip_date_of_birth,
            tip_type, tip_country_code,
            tip_passport_no, tip_date_of_expiry;
    private String lan_tip_str_teq[] = {"tam adı", "Cinsiyeti",
            "Uyruğu", "Doğum Tarihi",
            "Tip", "Ülke kodu",
            "Pasaport numarası.", "Geçerlilik"};
    private String lan_tip_str_en[] = {"Name", "Sex",
            "Nationality", "Date of birth",
            "Type", "Country code",
            "Passport No.", "Date of expiry"};

    private TextView passprot_name1, passprot_name2, passprot_sex,
            passprot_nationality, passprot_date_of_birth,
            passprot_discretionary, passprot_discretionary2,
            passprot_place_of_issue, passprot_date_of_expiry,
            passprot_type, passprot_country_code, passprot_passport_no,
            passprot_mrz_code1, passprot_mrz_code2;// ,mButton_regi;

    String String2 = "";

    private AccessParserNDKInterface accessParserNDKInterface = new AccessParserNDKInterface();

    public static final String[] OCR_PARSED_FIELD_NAMES = {"DOB", "Expiry",
            "Issuer", "Document Type", "Last Name", "First Name",
            "Nationality", "Discretionary", "Discretionary2",
            "Document Number", "Sex"};
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
    private static AudioPlay ap = new AudioPlay();

    private int lan_id = 0;//默认是英文0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passprot_activity);//console


        //
        //mReception = (EditText) findViewById(R.id.EditTextReception);
        //Emission = (EditText) findViewById(R.id.EditTextEmission);
        passprot_name1 = (TextView) this.findViewById(R.id.content_name1);
        //passprot_name2 = (TextView)this.findViewById(R.id.content_name2);
        passprot_sex = (TextView) this.findViewById(R.id.content_sex);
        passprot_nationality = (TextView) this.findViewById(R.id.content_nationality);
        passprot_date_of_birth = (TextView) this.findViewById(R.id.content_date_of_birth);
        passprot_discretionary = (TextView) this.findViewById(R.id.content_Discretionary);
        passprot_discretionary2 = (TextView) this.findViewById(R.id.content_Discretionary2);
        passprot_date_of_expiry = (TextView) this.findViewById(R.id.content_date_of_expiry);

        passprot_type = (TextView) this.findViewById(R.id.content_type);
        passprot_country_code = (TextView) this.findViewById(R.id.content_country_code);
        passprot_passport_no = (TextView) this.findViewById(R.id.content_passport_no);
        passprot_mrz_code1 = (TextView) this.findViewById(R.id.content_mrz_code1);
        passprot_mrz_code2 = (TextView) this.findViewById(R.id.content_mrz_code2);

        //
        tip_name1 = (TextView) this.findViewById(R.id.name1);
        tip_sex = (TextView) this.findViewById(R.id.sex);
        tip_nationality = (TextView) this.findViewById(R.id.nationality);
        tip_date_of_birth = (TextView) this.findViewById(R.id.date_of_birth);
        tip_type = (TextView) this.findViewById(R.id.type);
        tip_country_code = (TextView) this.findViewById(R.id.country_code);
        tip_passport_no = (TextView) this.findViewById(R.id.passport_no);
        tip_date_of_expiry = (TextView) this.findViewById(R.id.date_of_expiry);

        image_photo = (ImageView) this.findViewById(R.id.image_photo);

        //判断页面
        Bundle bundle = this.getIntent().getExtras();
        String name = bundle.getString("lan_id");
        if (name.equals("1")) {
            lan_id = 1;

            //
            tip_name1.setText(lan_tip_str_teq[0]);
            tip_sex.setText(lan_tip_str_teq[1]);
            tip_nationality.setText(lan_tip_str_teq[2]);
            tip_date_of_birth.setText(lan_tip_str_teq[3]);
            tip_type.setText(lan_tip_str_teq[4]);
            tip_country_code.setText(lan_tip_str_teq[5]);
            tip_passport_no.setText(lan_tip_str_teq[6]);
            tip_date_of_expiry.setText(lan_tip_str_teq[7]);
        } else {
            lan_id = 0;

            //
            tip_name1.setText(lan_tip_str_en[0]);
            tip_sex.setText(lan_tip_str_en[1]);
            tip_nationality.setText(lan_tip_str_en[2]);
            tip_date_of_birth.setText(lan_tip_str_en[3]);
            tip_type.setText(lan_tip_str_en[4]);
            tip_country_code.setText(lan_tip_str_en[5]);
            tip_passport_no.setText(lan_tip_str_en[6]);
            tip_date_of_expiry.setText(lan_tip_str_en[7]);
        }
    }

    public static String asciiToString(String value) {
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
        android.util.Log.d("huangmin", "onresume");
        if (mSerialPort != null) {
            mSerialPort.ocr315powerswitch(1);
            Toast.makeText(PassprotActivity.this, R.string.device_open,
                    Toast.LENGTH_SHORT).show();
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
                if ((buffer[0] == 28)) {//起始位
                    //mReception.setText("");
                    //Emission.setText("");
                    passprot_name1.setText("");

                    stringBuffer.setLength(0);
                    //passprot_name2.setText("");
                    passprot_sex.setText("");
                    passprot_nationality.setText("");
                    passprot_date_of_birth.setText("");
                    passprot_discretionary.setText("");
                    passprot_discretionary2.setText("");
                    passprot_date_of_expiry.setText("");

                    passprot_type.setText("");
                    passprot_country_code.setText("");
                    passprot_passport_no.setText("");

                    passprot_mrz_code1.setText("");
                    passprot_mrz_code2.setText("");
                }

                String regex = "[^\\p{Graph}\\s]";
                String string1 = new String(buffer, 0, size);
                string1 = string1.replaceAll(regex, "");
                stringBuffer.append(string1);
                String[] temp = null;


                if (buffer[size - 1] == 29) {//结束位

                    String string_enter = asciiToString("13");//回车的ascii码转字符串
                    temp = stringBuffer.toString().split(string_enter);
                    if (temp.length > 1) {
                        String3 = temp[0];
                        String4 = temp[1];
                    }
                    if (String3 != null) {
                        passprot_mrz_code1.setText(String3);
                    }

                    if (String4 != null) {
                        passprot_mrz_code2.setText(String4);
                    }

                    stringBuffer.setLength(0);

                }

                if (String3 != null && String4 != null) {
                    String resultString = "";
                    String[] token = null;
                    try {
                        resultString = accessParserNDKInterface.AccessHIDParseOCR(String3, String4, "", true);


                        token = resultString.split("\n");        // Don't split on /r as all lines must exist

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


                    } catch (Exception ex) {
                        stringBuffer.setLength(0);

                        //VibratorUtil.Vibrate(ConsoleActivity.this, 100);
                        Toast.makeText(PassprotActivity.this, R.string.AccessDataOCRLinesRx_fail,
                                Toast.LENGTH_SHORT).show();
                        Log.d("huangmin", "AccessDataOCRLinesRx - Error - " + ex.toString());
                    }


                    //passprot_name1.setText(firstname);
                    if (!lastname.equals("") && null != lastname) {
                        passprot_name1.setText(firstname + ", " + lastname);
                    } else {
                        passprot_name1.setText(firstname);
                    }

                    //passprot_name2.setText(lastname);
                    passprot_sex.setText(sex);
                    passprot_nationality.setText(nationality);
                    passprot_date_of_birth.setText(dob);
                    passprot_discretionary.setText(discretionary);
                    passprot_discretionary2.setText(discretionary2);
                    passprot_date_of_expiry.setText(expiry);

                    passprot_type.setText(Passport_type);
                    passprot_country_code.setText(issuer);
                    passprot_passport_no.setText(passport_num);


                    //
                    ReadCardKit cardKit = new ReadCardKit();
                    image_photo.setBackgroundResource(R.drawable.photo);

                    //
                    if (cardKit.openModule()) {
                        Log.e("", "开始读卡...");
                        if (0 != cardKit.ReadCard()) {
                            Log.e("无卡", "请将护照靠近读卡区域.");
                            Toast.makeText(PassprotActivity.this, "请重新读卡", 0).show();
                        } else {
                            Bitmap bmp = cardKit.getPhotoBmp();
                            if (null != bmp) {
                                image_photo.setImageBitmap(bmp);
                                Toast.makeText(PassprotActivity.this, "读卡成功", 1).show();
                                Log.e("", "读卡成功");
                            } else {
                                Toast.makeText(PassprotActivity.this, "图片空", 0).show();
                            }
                        }

                        cardKit.closeModule();
                    }

                }

//				if (Emission != null) {
//					if(String4 != null){
//						Emission.setText(String4);
//					}
//				}
            }
        });
    }

    @Override
    protected void onStop() {
        //
        super.onStop();

        if (mSerialPort != null) {
            mSerialPort.ocr315powerswitch(0);
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
