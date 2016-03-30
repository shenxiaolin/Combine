package com.example.jy.demo.fingerprint;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Submain extends Activity {

	private Button mButton_about,mButton_syslog,mButton_back,mButton_setting; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sub_main);

		mButton_about = (Button) this.findViewById(R.id.button_subabout);
		mButton_syslog = (Button) this.findViewById(R.id.button_subsyslog); 
		mButton_setting = (Button) this.findViewById(R.id.button_subseting); 
		mButton_back = (Button) this.findViewById(R.id.button_subback); 
		
		mButton_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mButton_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent it = new Intent(android.provider.Settings.ACTION_SETTINGS);
				startActivity(it);
				
//		        Intent intent22 = new Intent(android.provider.Settings.ACTION_SETTINGS);
//		        intent22.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent22);
			}
		});
		
		mButton_about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent it = new Intent(Submain.this,SystemAbout.class);
				startActivity(it);
			}
		});
		
		mButton_syslog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent it = new Intent(Submain.this,SystemLog.class);
				startActivity(it);
			}
		});
	}
}