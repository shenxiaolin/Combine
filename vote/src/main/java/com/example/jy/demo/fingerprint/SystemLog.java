package com.example.jy.demo.fingerprint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SystemLog extends Activity {

	private Button mButton_query, mButton_exit;

	private Vote_DBHelper mVoteDB;
	private Cursor mCursor;
	private DBSysLogAdapter mDBListAdapter;
	private ListView mDBlist;

	private SharedPreferences preferences;
	private CustomerDatePickerDialog dateDialog;

	private String SYSTEMLOG_TABLE_NAME;
	private int mMouth, mDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemlog);

		preferences = this.getSharedPreferences(
				getResources().getString(R.string.SystemConfig_sp),
				MODE_PRIVATE);

		mVoteDB = new Vote_DBHelper(this);
		mCursor = mVoteDB.Query_Syslog_table();
		SYSTEMLOG_TABLE_NAME = mVoteDB.SYSLOG_TABLE_NAME;
		mDBListAdapter = new DBSysLogAdapter(this, mCursor);

		mDBlist = (ListView) findViewById(R.id.syslog_list);
		mDBlist.setAdapter(mDBListAdapter);

		mButton_query = (Button) this.findViewById(R.id.syslog_button_query);
		mButton_exit = (Button) this.findViewById(R.id.syslog_button_back);

		mButton_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final Calendar cal = Calendar.getInstance();

				mMouth = cal.get(Calendar.MONTH) + 1;
				mDay = cal.get(Calendar.DAY_OF_MONTH);
				dateDialog = new CustomerDatePickerDialog(SystemLog.this, null,
						cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				dateDialog.show();
				updataAllList();

			}
		});

		mButton_exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private void updataAllList() {
		mCursor = mVoteDB.Query_Syslog_table();
		mDBListAdapter = new DBSysLogAdapter(mCursor);
		mDBlist.setAdapter(mDBListAdapter);
	}

	private void updataQueryList(Cursor mCursor) {
		mDBListAdapter = new DBSysLogAdapter(mCursor);
		mDBlist.setAdapter(mDBListAdapter);
	}

	private void insert_db() {

		// int Vin = (int) (Math.random() * 1000000000);

		String code = "01/02/03/005";
		String Vin = "1111 AFBB E729 444";
		// String Vin = "11 22";
		// String[] readomWord = {"Y","N"};
		// String Status = readomWord[(int)(Math.random() * 2)];

		mVoteDB.insert_syslogtable("Dwadafsfaer","Modify password");
		// mVoteDB.insert_syslogtable(preferences.getString("last_login_username",
		// "Admin") , date ,time, "login");

	}

	class CustomerDatePickerDialog extends mDatePickerDialog {

		public CustomerDatePickerDialog(Context context,
										OnDateSetListener callBack, int year, int monthOfYear) {
			super(context, callBack, year, monthOfYear);
		}

		@Override
		public void onDateChanged(DatePicker view, int year, int month, int day) {
			super.onDateChanged(view, year, month, day);

			mMouth = month + 1;
			mDay = day;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			super.onClick(dialog, which);

			String mMouth1 = String.valueOf(mMouth);
			String mDay1 = String.valueOf(mDay);
			if (mMouth1.length() <= 1) {
				mMouth1 = "0" + mMouth1;
			}
			if (mDay1.length() <= 1) {
				mDay1 = "0" + mDay1;
			}

			String datatime = mMouth1 + "-" + mDay1;
			// 查询
			mCursor = mVoteDB.query(SYSTEMLOG_TABLE_NAME, null, "date=?",
					new String[] { datatime }, null, null, null);

			if (mCursor != null)
				mCursor.moveToFirst();

			updataQueryList(mCursor);

			String toasttext;

			if (mCursor.getCount() > 0) {
				toasttext = getResources().getString(
						R.string.System_Log_query_text)
						+ " "
						+ mCursor.getCount()
						+ " "
						+ getResources().getString(
						R.string.System_Log_query_num);
			} else {
				toasttext = getResources().getString(
						R.string.System_Log_query_fail);
			}

			Toast.makeText(SystemLog.this, toasttext, Toast.LENGTH_SHORT)
					.show();

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mCursor != null)
			mCursor.close();

	}

	public class DBSysLogAdapter extends BaseAdapter {
		private Context mContext;
		private Cursor mCursor;
		private LayoutInflater inflater;

		public DBSysLogAdapter(Cursor cursor) {

			mCursor = cursor;
		}

		public DBSysLogAdapter(Context context, Cursor cursor) {

			mContext = context;
			mCursor = cursor;
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
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

			mCursor.moveToPosition(position);
			ViewHolder viewHolder;

			if (convertView == null) {
				viewHolder = new ViewHolder();
				inflater = (LayoutInflater) SystemLog.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.systemlog_list, null);
				convertView.setTag(viewHolder);

				viewHolder.NameTitle = (TextView) convertView.findViewById(R.id.sysloglist_name);
				viewHolder.DateTitle = (TextView) convertView.findViewById(R.id.sysloglist_date);
				viewHolder.EventTitle = (TextView) convertView.findViewById(R.id.sysloglist_event);

			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.NameTitle.setText(mCursor.getString(1));
			viewHolder.DateTitle.setText(mCursor.getString(2) + "," + mCursor.getString(3));
			viewHolder.EventTitle.setText(mCursor.getString(4));

			return convertView;
		}

		class ViewHolder {
			private TextView NameTitle;
			private TextView DateTitle;
			private TextView EventTitle;
		}
	}

}