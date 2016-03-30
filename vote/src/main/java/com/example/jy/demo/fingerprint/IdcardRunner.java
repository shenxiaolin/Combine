package com.example.jy.demo.fingerprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.example.jy.demo.fingerprint.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//ʶ��Activity�����༰�����ʾҳ��
public class IdcardRunner extends Activity {
	public static final String TAG = "IdcardRunner";
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidWT";
	private Button mbutquit;
	private String selectPath;
	private int nMainID = 0;
	private Boolean cutBoolean = true;
	private String resultFileNameString = "";
//	private RecogService.recogBinder recogBinder;
	private EditText editResult;
	private String str = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ȥ����Ϣ��
		// ����ʶ��
		Intent intentget = this.getIntent();
		selectPath = intentget.getStringExtra("path");
		cutBoolean = intentget.getBooleanExtra("cut", true);
		nMainID = intentget.getIntExtra("nMainID", 13);
		System.out.println("nMainID��" + nMainID);

		
			if (selectPath != null && !selectPath.equals("")) {
				int index = selectPath.lastIndexOf("/");
				resultFileNameString = (String) selectPath.subSequence( 
						index + 1, selectPath.length());

				try {
					String logopath = "";
					// String logopath = getSDPath() + "/photo_logo.png";
					Intent intent = new Intent("wintone.idcard");
					Bundle bundle = new Bundle();
					int nSubID[] = null;// {0x0001};
					bundle.putString("cls", "com.example.jy.demo.fingerprint.IdcardRunner");
					bundle.putInt("nTypeInitIDCard", 0); // ��������0����
					bundle.putString("lpFileName", selectPath);// ָ����ͼ��·��/
					bundle.putInt("nTypeLoadImageToMemory", 0);// 0��ȷ��������ͼ��1�ɼ���ͼ��2�����ͼ��4�����ͼ
					if (nMainID == 1000) {
						nSubID[0] = 3;
					}
					bundle.putInt("nMainID", nMainID); // ֤���������͡�6����ʻ֤��2�Ƕ���֤������ֻ���Դ�һ��֤�������͡�ÿ��֤������һ��Ψһ��ID�ţ���ȡֵ��֤��������˵��
					bundle.putIntArray("nSubID", nSubID); // ����Ҫʶ���֤������ID��ÿ��֤����������������ͼ�֤��������˵����nSubID[0]=null����ʾ����������ΪnMainID������֤����
					// bundle.putBoolean("GetSubID", true);
					// //GetSubID�õ�ʶ��ͼ���������id
					// bundle.putString("lpHeadFileName",
					// "/mnt/sdcard/head.jpg");//����·��������׺ֻ��Ϊjpg��bmp��tif
					// bundle.putBoolean("GetVersionInfo", true); //��ȡ�������İ汾��Ϣ

					// �����õ��ļ����sn
					File file = new File(PATH);
					String snString = null;
					if (file.exists()) {
						String filePATH = PATH + "/IdCard.sn";
						File newFile = new File(filePATH);
						if (newFile.exists()) {
							BufferedReader bfReader = new BufferedReader(
									new FileReader(newFile));
							snString = bfReader.readLine().toUpperCase();
							bfReader.close();
						} else {
							bundle.putString("sn", "");
						}
						if (snString != null && !snString.equals("")) {
							bundle.putString("sn", snString);
							// String string = (String) bundle.get("sn");
							// Toast.makeText(getApplicationContext(),
							// "snString=="+string, 3000).show();
						} else {
							bundle.putString("sn", "");
						}
					} else {
						bundle.putString("sn", "");
					}

					// bundle.putString("datefile",
					// "assets");//Environment.getExternalStorageDirectory().toString()+"/wtdate.lsc"
					// bundle.putString("devcode", "SBGAQC7EZAIAXRY");
					// bundle.putString("versionfile",
					// "assets");//Environment.getExternalStorageDirectory().toString()+"/wtversion.lsc"
					// bundle.putString("sn", "XS4XAYRWEFRY248YY4LHYY178");
					// //���кż��ʽ,XS4XAYRWEFRY248YY4LHYY178��ʹ��
					// bundle.putString("server",
					// "http://192.168.0.36:8080");//http://192.168.0.36:8888

					bundle.putString("authfile", ""); // �ļ����ʽ
														// /mnt/sdcard/AndroidWT/357816040594713_zj.txt
					bundle.putString("logo", logopath); // logo·����logo��ʾ��ʶ��ȴ�ҳ�����Ͻ�
					bundle.putBoolean("isCut", cutBoolean); // �粻���ô���Ĭ���Զ�����
					// bundle.putBoolean("isSaveCut", true);
					bundle.putString("returntype", "withvalue");// ����ֵ���ݷ�ʽwithvalue�������Ĵ�ֵ��ʽ���´�ֵ��ʽ��
					intent.putExtras(bundle);
					startActivityForResult(intent, 8);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getString(R.string.noFoundProgram)
									+ "wintone.idcard", 0).show();
				}

			} else {
			}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult");
		String ReturnLPFileName = null;
		setContentView(R.layout.idcardrunner);
		EditText editResult = (EditText) this.findViewById(R.id.edit_file);
		Button takePic = (Button) this.findViewById(R.id.takePic);
		Button backIndex = (Button) this.findViewById(R.id.backIndex);
		backIndex.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				finish();
				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setClass(IdcardRunner.this, ImageChooser.class);
//				IdcardRunner.this.finish();
//				startActivity(intent);
			}
		});
		takePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(IdcardRunner.this, CameraActivity.class);
				intent.putExtra("WIDTH", 640);//����ˣ���Ӧ��������������ս���ǰ���ֱ��ʵ��ô���������ͨ���������ݵķ�ʽ �����һ��
				intent.putExtra("HEIGHT", 480);
				intent.putExtra("srcwidth", 2048);
				intent.putExtra("srcheight",1536);
				intent.putExtra("nMainID", nMainID);
				IdcardRunner.this.finish();
				startActivity(intent);
			}
		});
		if (requestCode == 8 && resultCode == RESULT_OK) {
			// ��ʶ�𷵻�ֵ
			int ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);// ȡ����״̬
			int ReturnInitIDCard = data
					.getIntExtra("ReturnInitIDCard", -100000);// ȡ��ʼ������ֵ
			int ReturnLoadImageToMemory = data.getIntExtra(
					"ReturnLoadImageToMemory", -100000);// ȡ��ͼ��ķ���ֵ
			int ReturnRecogIDCard = data.getIntExtra("ReturnRecogIDCard",
					-100000);// ȡʶ��ķ���ֵ
			// System.out.println("" +
			// intentget.getStringExtra("ReturnGetVersionInfo"));
			// //ȡ�汾��Ϣ�����������GetVersionInfoΪtrue����ȡ�汾��Ϣ
			// System.out.println("ReturnGetSubID" +
			// intentget.getIntExtra("ReturnGetSubID",-1));
			// //ȡʶ��ͼ���������id�����������GetSubID Ϊtrue����ȡʶ��ͼ���������id
			// System.out.println("" +
			// intentget.getIntExtra("ReturnSaveHeadImage",-1));
			// //ȡSaveHeadImage����ֵ�����������lpHeadFileName��·�����ܵõ�ͷ��
			// System.out.println("ReturnUserData:" +
			// intentget.getStringExtra("ReturnUserData"));
			Log.i(TAG,
					"ReturnLPFileName:"
							+ data.getStringExtra("ReturnLPFileName"));

			if (ReturnAuthority == 0 && ReturnInitIDCard == 0
					&& ReturnLoadImageToMemory == 0 && ReturnRecogIDCard > 0) {
				// System.out.println("���ս��");
				String result = "";
				String[] fieldname = (String[]) data
						.getSerializableExtra("GetFieldName");
				String[] fieldvalue = (String[]) data
						.getSerializableExtra("GetRecogResult");
				String time = data.getStringExtra("ReturnTime");
				ReturnLPFileName = data.getStringExtra("ReturnLPFileName");
				if (null != fieldname) {
					if (nMainID == 9) {
						result = getString(R.string.fieldname00)
								+ fieldvalue[0] + ";\n"
								+ getString(R.string.fieldname01)
								+ fieldvalue[1] + ";\n"
								+ getString(R.string.fieldname02)
								+ fieldvalue[2] + ";\n"
								+ getString(R.string.fieldname3)
								+ fieldvalue[3] + ";\n"
								+ getString(R.string.fieldname4)
								+ fieldvalue[4] + ";\n"
								+ getString(R.string.fieldname5)
								+ fieldvalue[5] + ";\n"
								+ getString(R.string.fieldname6)
								+ fieldvalue[6] + ";\n"
								+ getString(R.string.fieldname7)
								+ fieldvalue[7] + ";\n"
								+ getString(R.string.fieldname8)
								+ fieldvalue[8] + ";\n"
								+ getString(R.string.fieldname9)
								+ fieldvalue[9] + ";\n"
								+ getString(R.string.fieldname10)
								+ fieldvalue[10] + ";\n"
								+ getString(R.string.fieldname11)
								+ fieldvalue[11] + ";\n"
								+ getString(R.string.fieldname12)
								+ fieldvalue[12] + ";\n"
								+ getString(R.string.fieldname013)
								+ fieldvalue[13] + ";\n"
								+ getString(R.string.fieldname14)
								+ fieldvalue[14] + ";\n";
					} else if (nMainID == 13) {
						result = getString(R.string.fieldname) + fieldvalue[0]
								+ ";\n" + getString(R.string.fieldname1)
								+ fieldvalue[1] + ";\n"
								+ getString(R.string.fieldname2)
								+ fieldvalue[2] + ";\n"
								+ getString(R.string.fieldname3)
								+ fieldvalue[3] + ";\n"
								+ getString(R.string.fieldname4)
								+ fieldvalue[4] + ";\n"
								+ getString(R.string.fieldname5)
								+ fieldvalue[5] + ";\n"
								+ getString(R.string.fieldname6)
								+ fieldvalue[6] + ";\n"
								+ getString(R.string.fieldname7)
								+ fieldvalue[7] + ";\n"
								+ getString(R.string.fieldname8)
								+ fieldvalue[8] + ";\n"
								+ getString(R.string.fieldname9)
								+ fieldvalue[9] + ";\n"
								+ getString(R.string.fieldname10)
								+ fieldvalue[10] + ";\n"
								+ getString(R.string.fieldname11)
								+ fieldvalue[11] + ";\n"
								+ getString(R.string.fieldname12)
								+ fieldvalue[12] + ";\n"
								+ getString(R.string.fieldname13)
								+ fieldvalue[13] + ";\n"
								+ getString(R.string.fieldname14)
								+ fieldvalue[14] + ";\n";
					} else {

						int count = fieldname.length;
						for (int i = 0; i < count; i++) {
							if (fieldname[i] != null) {
								result += fieldname[i] + ":" + fieldvalue[i]
										+ ";\n";
							}
						}

					}
				}
//				str = "\n" + getString(R.string.recogResult1) + " \n"
//						+ getString(R.string.idcardType) + ReturnRecogIDCard
//						+ "\n" + result + "\n" + time;
				str = "\n" + getString(R.string.recogResult1) + " \n"
						+ getString(R.string.idcardType) + ReturnRecogIDCard
						+ "\n" + result;
				String mrz1 = fieldvalue[10];
				mrz1 = mrz1.replace("<", "");
				Log.v("crjlog", "mrz11111 = " + mrz1);
				
//				mrz1=mrz1.replace('<',' ');
//				mrz1 = mrz1.replace(" ", "");
//				Log.v("crjlog", "mrz111111111 = " + mrz1);
//				
				String mrz2 = fieldvalue[11];
				Log.v("crjlog", "mrz2 = " + mrz2);
				
				if((mrz2.equals("E000000008CHN8510312F2410306NGKELMPONBPJB970") && mrz1.equals("POCHNZHENGJIANYANGBEN")) || (mrz2.equals("E000000008CHN8510312F2410306NGKELMP0NBPJB970") && mrz1.equals("POCHNZHENGJIANYANGBEN")) ){
					
					editResult.setTextColor(0xff00ff00);
				}else{
					
					editResult.setTextColor(0xffff0000);
				}
				editResult.setText(str);
				
			} else {
				String str = "";
				if (ReturnAuthority == -100000) {
					str = getString(R.string.exception) + ReturnAuthority;
				} else if (ReturnAuthority != 0) {
					str = getString(R.string.exception1) + ReturnAuthority;
				} else if (ReturnInitIDCard != 0) {
					str = getString(R.string.exception2) + ReturnInitIDCard;
				} else if (ReturnLoadImageToMemory != 0) {
					if (ReturnLoadImageToMemory == 3) {
						str = getString(R.string.exception3)
								+ ReturnLoadImageToMemory;
					} else if (ReturnLoadImageToMemory == 1) {
						str = getString(R.string.exception4)
								+ ReturnLoadImageToMemory;
					} else {
						str = getString(R.string.exception5)
								+ ReturnLoadImageToMemory;
					}
				} else if (ReturnRecogIDCard != 0) {
					str = getString(R.string.exception6) + ReturnRecogIDCard;
				}
				
				editResult.setText(getString(R.string.recogResult2) + str + "\n");
			}

		}
	}

	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ��Ŀ¼
		}
		return sdDir.toString();

	}

	// ��ת�������Activity
	@Override
	protected void onStop() {
		super.onStop();
		// finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// land
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// port
		}
	}

	protected int readPreferences(String perferencesName, String key) {
		SharedPreferences preferences = getSharedPreferences(perferencesName,
				MODE_PRIVATE);

		int result = preferences.getInt(key, 0);
		return result;
	}
}
