package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class nfc extends Activity {
	NfcAdapter nfcAdapter;
	TextView promt;
	
	private SharedPreferences preferences;

	byte key_a[] = { (byte) 0x9B, (byte) 0x30, (byte) 0x45, (byte) 0xC1,
			(byte) 0x20, (byte) 0x12, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff, (byte) 0xff };
	byte key_b[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff, (byte) 0xff };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_main);
		promt = (TextView) findViewById(R.id.promt);
		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		// ��ȡĬ�ϵ�NFC������
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			promt.setText("�豸��֧��NFC��");
			finish();
			return;
		}
		if (!nfcAdapter.isEnabled()) {
			promt.setText("����ϵͳ������������NFC���ܣ�");
			finish();
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// �õ��Ƿ��⵽ACTION_TECH_DISCOVERED����
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
			
			//�ж��Ƿ��ѵ�¼
			Boolean is_login = preferences.getBoolean("is_login", false);
			if(is_login){
				// �����intent
				processIntent(getIntent());				
			}else{
				//û��¼��ת����¼����
				Toast.makeText(getApplicationContext(),
						R.string.login_first_toast, Toast.LENGTH_SHORT)
						.show();
				
				Intent it = new Intent(nfc.this, login.class);
				startActivity(it);
				finish();
			}
		}
	}

	// �ַ�����ת��Ϊ16�����ַ���
	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			// System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			Intent it = new Intent(nfc.this, person.class);
			it.putExtra("comparedata", true);
			startActivityForResult(it, 0);
			finish();
		}
	}

	private void processIntent(Intent intent) {
		// ȡ����װ��intent�е�TAG
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		for (String tech : tagFromIntent.getTechList()) {
			// System.out.println(tech);
		}
		boolean auth = false;
		boolean auth2 = false;
		boolean auth3 = false;
		boolean auth4 = false;

		Log.v("crjlog", "processIntent");
//		Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		startActivityForResult(intent1, 1);

		// ��ȡTAG
		MifareClassic mfc = MifareClassic.get(tagFromIntent);

		Log.v("crjlog", "nfc mfc =" + mfc);

		try {
			String metaInfo = "";
			// Enable I/O operations to the tag from this TagTechnology object.
			mfc.connect();
			int type = mfc.getType();// ��ȡTAG������

			Log.v("crjlog", "nkc type = " + type);

			int sectorCount = mfc.getSectorCount();// ��ȡTAG�а�����������
			String typeS = "";
			switch (type) {
			case MifareClassic.TYPE_CLASSIC:
				typeS = "TYPE_CLASSIC";
				break;
			case MifareClassic.TYPE_PLUS:
				typeS = "TYPE_PLUS";
				break;
			case MifareClassic.TYPE_PRO:
				typeS = "TYPE_PRO";
				break;
			case MifareClassic.TYPE_UNKNOWN:
				typeS = "TYPE_UNKNOWN";
				break;
			}
			metaInfo += "��Ƭ���ͣ�" + typeS + "\n��" + sectorCount + "������\n��"
					+ mfc.getBlockCount() + "����\n�洢�ռ�: " + mfc.getSize()
					+ "B\n";
			for (int j = 0; j < sectorCount; j++) {
				// Authenticate a sector with key A.
				// auth = mfc.authenticateSectorWithKeyB(j,
				// MifareClassic.KEY_DEFAULT);
				// auth = mfc.authenticateSectorWithKeyA(j,
				// MifareClassic.KEY_DEFAULT);
				// auth2 = mfc.authenticateSectorWithKeyB(j,
				// MifareClassic.KEY_DEFAULT);

				auth = mfc.authenticateSectorWithKeyA(j, key_a);
				auth2 = mfc.authenticateSectorWithKeyA(j, key_b);

				// Log.v("crjlog","auth = " + auth);
				// Log.v("crjlog","auth2 = " + auth);
				// Log.v("crjlog","auth2 = " + auth2);

				// auth3 = mfc.authenticateSectorWithKeyB(j, key_a);
				// auth4 = mfc.authenticateSectorWithKeyB(j, key_b);

				// Log.v("crjlog","auth3 = " + auth3);
				// Log.v("crjlog","auth4 = " + auth4);

				// System.out.println("xxh+"+bytesToHexString(MifareClassic.KEY_DEFAULT));
				int bCount;
				int bIndex;
				if (auth == true || auth2 == true) {
					metaInfo += "Sector " + j + ":��֤�ɹ�\n"; 
					// ��ȡ�����еĿ�
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);
					
					for (int i = 0; i < bCount; i++) {
						byte[] data = mfc.readBlock(bIndex);
						metaInfo += "Block " + bIndex + " : "
								+ bytesToHexString(data) + "\n";
						bIndex++;
					}
				} else {
					metaInfo += "Sector " + j + ":��֤ʧ��\n";
				}
			}
			promt.setText(metaInfo);

			// File f = new File("/sdcard/nfc.txt");// �����ļ�
			// String mstring = Environment.getExternalStorageDirectory() +
			// "/nfc.txt";
			// String mstring = "/mnt/sdcard/nfc.txt";

			String mstring = "/mnt/extSdCard/nfc.txt";
			File f = new File(mstring);

			Log.v("crjlog", "createNewFile f " + f);

			// File f = new File("/system/nfc.txt");// �����ļ�

			if (!f.exists()) {// �ļ������ڷ���false
				try {
					Log.v("crjlog", "createNewFile");
					f.createNewFile();
					FileOutputStream outStream = new FileOutputStream(f);
					outStream.write(metaInfo.getBytes());
					outStream.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("crjlog", "Not createNewFile");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}