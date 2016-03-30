package com.example.jy.demo.fingerprint;


import java.io.File;
import java.util.Calendar;

import com.example.jy.demo.fingerprint.SystemConfig.DatePickerFragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

//import com.io.io;
public class gatherMain extends Activity {
	
	int id_num = 00000001;
	
	private EditText dcs_name,dcs_id,dcs_gender,dcs_address,dcs_idno;
	private Button bt_next,bt_gender,bt_bir,bt_back;
	
	private Vote_DBHelper mGatherDB;
	private Cursor mCursor_gather;
	private String GATHER_TABLE_NAME, GATHER_NAME;
	
    private SharedPreferences preferences;
    
    private int num = 0;
    private String fileName;
    private int ENTRY_NAME_NUM,ENTRY_ADDRESS_NUM;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gather_info);
		
		mGatherDB = new Vote_DBHelper(this);
		mCursor_gather = mGatherDB.Query_Gather_table();

		GATHER_TABLE_NAME = mGatherDB.GATHER_USER_TABLE_NAME;
		ENTRY_NAME_NUM = mGatherDB.ENTRY_NAME_NUM;
		ENTRY_ADDRESS_NUM = mGatherDB.ENTRY_ADDRESS_NUM;
		
		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		dcs_name = (EditText)findViewById(R.id.dcsinfo_name);
		dcs_name.setSingleLine();
		dcs_name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_NAME_NUM)});
		
		
		dcs_id = (EditText)findViewById(R.id.dcsinfo_id);
		bt_gender = (Button)findViewById(R.id.dcsinfo_gender);
		dcs_address = (EditText)findViewById(R.id.dcsinfo_address);
		
		dcs_address.setSingleLine();
		dcs_address.setFilters(new InputFilter[] { new InputFilter.LengthFilter(ENTRY_ADDRESS_NUM)});


		
		dcs_idno= (EditText)findViewById(R.id.dcsinfo_IDNO);
		dcs_idno.setSingleLine();
		
		bt_bir = (Button)findViewById(R.id.dcsinfo_bir);
		bt_bir.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub 
				
				DatePickerFragment dateDialog = new DatePickerFragment();
				dateDialog.show(getFragmentManager(), "date");

			}
		});		
		
		
		
		bt_back = (Button)findViewById(R.id.dcs_info_back);
		bt_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		bt_next = (Button)findViewById(R.id.dcs_info_next);
		bt_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if ((dcs_name.getText().length() > 0)&&(dcs_address.getText().length() > 0)&&(dcs_idno.getText().length() > 0)&&(bt_bir.getText().length() > 0)&&(bt_gender.getText().length() > 0)){
					
				//	mGatherDB.insert_gathertable(dcs_name.getText().toString(), bt_gender.getText().toString(),dcs_address.getText().toString());
					Intent it = new Intent(gatherMain.this, gatherFinger.class);
					startActivity(it);
					
					// 向数据库更新flag
					Editor editor = preferences.edit();
					//id
					editor.putString("id", dcs_id.getText().toString());
					//name
					editor.putString("name", dcs_name.getText().toString());
					//gender
					editor.putString("gender", bt_gender.getText().toString());
					//bir
					editor.putString("birthday", bt_bir.getText().toString());
					//address
					editor.putString("address", dcs_address.getText().toString());

					
					editor.putString("idno", dcs_idno.getText().toString());
					// 提交更改
					editor.commit();
					
					//delete old folder
			        File fileFolder = new File(fileName);
			        if (fileFolder.exists()) {
//						deleteDate();
						delete(fileFolder);
			        }
			        
			        finish();
					
				}else{
					
					Toast.makeText(gatherMain.this, "empty item",Toast.LENGTH_LONG).show();
				}
				
			}
		});		
		
		//gender
		bt_gender.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				new AlertDialog.Builder(gatherMain.this) 
				.setTitle("请选择")  
				.setIcon(android.R.drawable.ic_dialog_info)                  
				.setSingleChoiceItems(new String[] {"Male","Female"}, 0,   
				  new DialogInterface.OnClickListener() {  
				                              
				     public void onClick(DialogInterface dialog, int which) {  
				        
				        switch (which) {
						case 0:
							bt_gender.setText("Male");
							
							break;
						case 1:
							
							bt_gender.setText("Female");
							break;

						default:
							break;
						}
				        
				        dialog.dismiss();  
				        
				     }  
				  }  
				)  
				.setNegativeButton("取消", null)  
				.create()
				.show();  
				
			}
		});	
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//id
		num = mCursor_gather.getCount() + 1;
		String num_ext = String.format("%1$,06d",num); 
		dcs_id.setText(String.valueOf(num_ext)); 
		
		fileName = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name) + "/" + dcs_id.getText().toString() + "/";
		Log.v("crjlog", "fileName111111111 = " + fileName);
		
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
	
	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
		
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Do something with the date chosen by the user
		
		 String month_ext = String.valueOf(month+1);
		 String day_ext = String.valueOf(day);
		 if(month_ext.length() <= 1){
			 month_ext = "0"+ month_ext;
		 }
		 if(day_ext.length() <= 1){
			 day_ext = "0"+ day_ext;
		 }
		 
		String timetxt = String.valueOf(year) + "-" + month_ext + "-" + day_ext;
		bt_bir.setText(timetxt);
		
	}
	}
}
