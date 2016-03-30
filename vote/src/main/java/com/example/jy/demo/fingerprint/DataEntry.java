package com.example.jy.demo.fingerprint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DataEntry extends Activity {

	private EditText medittext;

	private Button mdataentry_open, mdataentry_save, mdataentry_back;

	private static final String fileSuf = ".txt";
	private static final String SYSTEMCONFIG_DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";

	private SharedPreferences preferences;
	private String ET, pucode, dataPath;
	private SimpleDateFormat mTimeformat;

	// dialog view
	private AlertDialog dlg;
	private Button dialog_bt_cancel, dialog_bt_ok;
	private TextView dialog_title, dialog_tv;

	private boolean is_modify = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dataentry_screen);

		// mTimeformat = new SimpleDateFormat(SYSTEMCONFIG_DATE_FORMAT);

		preferences = this.getSharedPreferences(
				getResources().getString(R.string.SystemConfig_sp),
				MODE_PRIVATE);
		ET = preferences.getString("CURRENT_ELECTION_TYPE", "President");
		pucode = preferences.getString("PU_CODE", "34-16-10-003");

		medittext = (EditText) findViewById(R.id.de_edittext);
		mdataentry_open = (Button) findViewById(R.id.de_button_open);
		mdataentry_save = (Button) findViewById(R.id.de_button_save);
		mdataentry_back = (Button) findViewById(R.id.de_button_back);

		mdataentry_open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (TextUtils.isEmpty(medittext.getText())) {

					Intent it = new Intent(DataEntry.this, DataEntryList.class);
					startActivity(it);

				} else {

					initDialogView();
					dialog_title.setText(R.string.screen_mainmenu_dialog_title);
					dialog_tv.setText(R.string.dataentry_toast_is_save);
					dialog_bt_ok.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							saveFileToPackage(medittext.getText().toString());

							// backups
							if (Environment.isExternalStorageRemovable()) {

								saveFileToDevice(medittext.getText().toString());

								saveFileToDevice_intern(medittext.getText()
										.toString());
							} else {

								saveFileToDevice(medittext.getText().toString());
							}

							if(is_modify){
								finish();
							}

							Intent it = new Intent(DataEntry.this,
									DataEntryList.class);
							startActivity(it);
							dlg.dismiss();
							medittext.setText(null);
						}
					});
				}
			}
		});

		mdataentry_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if ("".equals(medittext.getText().toString().trim())) {

					Toast.makeText(DataEntry.this,
							R.string.dataentry_file_empty, Toast.LENGTH_SHORT)
							.show();
				} else {

					saveFileToPackage(medittext.getText().toString());

					// backups
					if (Environment.isExternalStorageRemovable()) {

						saveFileToDevice(medittext.getText().toString());

						saveFileToDevice_intern(medittext.getText().toString());
					} else {

						saveFileToDevice(medittext.getText().toString());
					}

					if(is_modify){
						finish();
					}

					// medittext.setText(null);
				}
				// Log.v("crjlog", "len = " + len);
			}
		});

		mdataentry_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private void saveFileToPackage(String toSaveString) {

		try {
			FileOutputStream fos;

			if (dataPath == null) {
				fos = this.openFileOutput(pucode + "_" + ET + fileSuf, 1);
			} else {

				fos = this.openFileOutput(dataPath, 1);
			}

			fos.write(toSaveString.getBytes());// 写入
			fos.close(); // 关闭输出流

			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_success,
					Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_file_nonexistent,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_error,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void saveFileToDevice_intern(String toSaveString) {
		try {
			File saveFile;

			String filePath = "/storage/sdcard1/Vote/" + pucode + "_" + ET
					+ fileSuf;

			saveFile = new File(filePath);
			if (!saveFile.exists()) {
				File dir = new File(saveFile.getParent());
				dir.mkdirs();
				saveFile.createNewFile();
			}

			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(toSaveString.getBytes());
			outStream.close();

			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_success,
					Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_file_nonexistent,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_error,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void saveFileToDevice(String toSaveString) {
		try {
			File saveFile;

			String filePath = Environment.getExternalStorageDirectory()
					.toString()
					+ "/"
					+ getResources().getString(R.string.app_name)
					+ "/"
					+ pucode + "_" + ET + fileSuf;

			saveFile = new File(filePath);
			if (!saveFile.exists()) {
				File dir = new File(saveFile.getParent());
				dir.mkdirs();
				saveFile.createNewFile();
			}

			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(toSaveString.getBytes());
			outStream.close();

			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_success,
					Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_file_nonexistent,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(DataEntry.this, R.string.dataentry_savefile_error,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String dataInfo = getIntent().getStringExtra("dataInfo");

		if (dataInfo != null) {
			is_modify = true;
			dataPath = getIntent().getStringExtra("dataPath");
			medittext.setText(dataInfo);
		} else {

			is_modify = false;
			if (this.getFilesDir().list().length > 0) {

				String data = readFile(this.getFilesDir() + "/" + pucode + "_"
						+ ET + fileSuf);

				dataPath = pucode + "_" + ET + fileSuf;
				medittext.setText(data);

			} else {

				dataPath = null;
				medittext.setText(null);
			}
		}
	}

	private static String readFile(String filePath) {
		String str = "";
		try {
			File readFile = new File(filePath);
			if (!readFile.exists()) {
				return null;
			}
			FileInputStream inStream = new FileInputStream(readFile);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
			}
			str = stream.toString();
			stream.close();
			inStream.close();
			return str;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (dlg != null)
			dlg.dismiss();

		medittext.setText(null);
		dataPath = null;
		is_modify = false;

	}

	private void initDialogView() {

		dlg = new AlertDialog.Builder(DataEntry.this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.theme_dialog_text);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window.findViewById(R.id.text_dialog_title);
		dialog_tv = (TextView) window.findViewById(R.id.text_dialog_tv);

		dialog_bt_ok = (Button) window.findViewById(R.id.text_dialog_button_ok);
		dialog_bt_cancel = (Button) window
				.findViewById(R.id.text_dialog_button_cancel);

		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlg.dismiss();
			}
		});
	}

}
