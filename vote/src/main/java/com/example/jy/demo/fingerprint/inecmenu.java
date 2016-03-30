package com.example.jy.demo.fingerprint;


import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class inecmenu extends Activity {

	private Button mButton_about,mButton_exit;
	
	private SharedPreferences preferences;
	
	// dialog view
	private AlertDialog dlg,DexttextDlg;
	private Button dialog_bt_cancel, dialog_bt_ok; 
	private TextView dialog_title, dialog_tv;
	private EditText dialog_et;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inec_main);
		
		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		
		
		mButton_exit = (Button) this.findViewById(R.id.button_inecExit);
		mButton_exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				exitDialogView_exit();
			}
		});
		

		mButton_about = (Button) this.findViewById(R.id.button_inecreset);
		mButton_about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				exitDialogView();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		exitDialogView_exit();
	}
	
	private void initDialogView() {

		dlg = new AlertDialog.Builder(inecmenu.this).create();
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
				if(dlg != null)
					dlg.dismiss();
			}
		});
	}
	
	private void exitDialogView_exit() {

		initDialogView();

		try {
			dialog_title.setText(R.string.screen_mainmenu_dialog_title);
			dialog_tv.setText(R.string.screen_login_exit_dialog);

			dialog_bt_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void exitDialogView() {

		initDialogView();

		try {
			dialog_title.setText(R.string.adminmain_text_reset_title);
			dialog_tv.setText(R.string.adminmain_text_reset);

			dialog_bt_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					deleteDate();
					finish();
					Toast.makeText(inecmenu.this,getResources().getString(R.string.adminmain_text_reset_success),Toast.LENGTH_SHORT).show();

				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void deleteDate() {
		String Path1 = getFilesDir().getParent().toString(); 
		File oldfile2 = new File(Path1);
		delete(oldfile2);
		
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		
		
		if(Environment.isExternalStorageRemovable()){
			
			String fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
			File fileText = new File(fileName);
			delete(fileText);
			
			String fileName2 = "/storage/sdcard1/Vote";
			File fileText2 = new File(fileName2);
			delete(fileText2);
			
			
		}else{
			
			String fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
			File fileText = new File(fileName);
			delete(fileText);
			
		}
		
	}
	
	public void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}
	
}