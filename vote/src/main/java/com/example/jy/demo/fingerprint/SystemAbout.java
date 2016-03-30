package com.example.jy.demo.fingerprint;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SystemAbout extends Activity {

	private ListView mlist;
	private String[] strs_name, strs_detail;
	private ListViewAdapter mAdapter;
	
	private Button mbuttonback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemabout);

		strs_name = getResources().getStringArray(R.array.system_about);
		strs_detail = getResources().getStringArray(R.array.system_about);
		mlist = (ListView) findViewById(R.id.sa_list);
		
		mbuttonback = (Button) findViewById(R.id.about_button_back);
		mbuttonback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		// init strs2
		updatalist();

	}

	// updata list
	public void updatalist() {
		// TODO Auto-generated method stub

		initData();
		mAdapter = new ListViewAdapter(strs_detail);
		mlist.setAdapter(mAdapter);
	}

	// init strs2[]
	public void initData() {

		// String gnbuildTime = SystemProperties.get("ro.build.date");

		try {

			Class cl = Class.forName("android.os.SystemProperties");
			Object invoker = cl.newInstance();
			Method m = cl.getMethod("get", new Class[] { String.class,
					String.class });
			Object result = m.invoke(invoker, new Object[] {
					"gsm.version.baseband", "no message" });
			String s = new String((String) result);
			String result_ext[] = s.split(",");
			strs_detail[1] = result_ext[0];
		} catch (Exception e) {

		}
		strs_detail[0] = android.os.Build.MODEL;
		strs_detail[2] = android.os.Build.VERSION.RELEASE;
		strs_detail[3] = android.os.Build.DISPLAY;
		
		
		  long timeLng = android.os.Build.TIME;
		  java.util.Date date = new java.util.Date(timeLng);
		  java.util.Calendar cal = java.util.Calendar.getInstance();
		  cal.setTime(date);
		  
		  String month = String.valueOf(cal.get(java.util.Calendar.MONTH)+1);	
		  String day = String.valueOf(cal.get(java.util.Calendar.DAY_OF_MONTH));
		  String year = String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2);
			
		 if(day.length() <= 1){
			 day = "0"+ day;
		 }	
		
		 String versionName = year+"."+month+day+".1";
		 strs_detail[4] = versionName;

//		try {
//			PackageInfo info = this.getPackageManager().getPackageInfo(
//					this.getPackageName(), 0);
////			strs_detail[4] = info.versionName;
//			//strs_detail[4] = "1.14.0607.01";
//			strs_detail[4] = "1.14.0611.01";
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public class ListViewAdapter extends BaseAdapter {
		private String mlistDetail[];
		private LayoutInflater inflater;
		private TextView mNmaeTitle, mDetailTitle;

		public ListViewAdapter(String listname[]) {

			mlistDetail = listname;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return strs_name.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View itemView = convertView;
			inflater = (LayoutInflater) SystemAbout.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.systemabout_list, null);

			mNmaeTitle = (TextView) itemView.findViewById(R.id.salist_name);
			mNmaeTitle.setText(strs_name[position]);
			mDetailTitle = (TextView) itemView.findViewById(R.id.salist_detail);
			mDetailTitle.setText(mlistDetail[position]);

			return itemView;
		}
	}

}
