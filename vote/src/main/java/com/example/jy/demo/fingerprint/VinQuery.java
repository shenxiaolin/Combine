package com.example.jy.demo.fingerprint;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VinQuery extends Activity {

	private Button mbuttonback, mbuttonquery;//, mbuttonshow;
	private TextView mDBQueryTitle, mDBQueryTitle_s, mDBQueryTitle_f,
			mDBQueryTitle_q;
	private EditText mEditTextVin;
	private VoteVin_DBHelper mVoteDB;
	private Cursor mCursor;
	private ListView mDBlist;
	private String[] queryListItem;

	private DBVinAdapter mDBListAdapter;

	private String VIN_TABLE_NAME;
	private int ENTRY_VIN_NUM;

	private SharedPreferences preferences;

	// dialog view
	private AlertDialog DexttextDlg, textviewDlg, listviewDlg; 
	private Button dialog_bt_cancel, dialog_bt_ok, listDialog_bt1,
			listDialog_bt2;
	private EditText dialog_et;
	private TextView dialog_tv, dialog_title;
	private Boolean is_Query = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.db_query_vin);
		mVoteDB = new VoteVin_DBHelper(this);
		mCursor = mVoteDB.Query_Vin_table();
		mDBListAdapter = new DBVinAdapter(this, mCursor);

		ENTRY_VIN_NUM = mVoteDB.ENTRY_VIN_NUM;
		VIN_TABLE_NAME = mVoteDB.VIN_TABLE_NAME;

		preferences = this.getSharedPreferences(
				getResources().getString(R.string.SystemConfig_sp),
				MODE_PRIVATE);

		mDBlist = (ListView) findViewById(R.id.votedb_list);
		mDBlist.setAdapter(mDBListAdapter);

		mbuttonquery = (Button) findViewById(R.id.button_query);
//		mbuttonshow = (Button) findViewById(R.id.button_statusQuery);
		mbuttonback = (Button) findViewById(R.id.button_back);

		mDBQueryTitle = (TextView) findViewById(R.id.vinquery_num_all);
		mDBQueryTitle.setText(getResources().getString(
				R.string.userlist_title_sum)
				+ " " + String.valueOf(mCursor.getCount()));

		mDBQueryTitle_s = (TextView) findViewById(R.id.vinquery_num_success);
		mDBQueryTitle_f = (TextView) findViewById(R.id.vinquery_num_fail);

		getSuccessNum();
		getFailNum();

		queryListItem = getResources().getStringArray(R.array.query_item);

		mbuttonback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mbuttonquery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				// TODO Auto-generated method stub

				initListDialogView();
				dialog_title.setText(R.string.vinlist_dialog_query_title);

				listDialog_bt1.setText(R.string.vinlist_title_vin);
				listDialog_bt2.setText(R.string.vinlist_title_status);

				listDialog_bt1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismissListDialogView();
						initEdittextDialogView();

						dialog_tv.setText(R.string.vinlist_dialog_query_title);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {

								if (dialog_et.getText().length() > 0) {
									
									StringBuffer Vin_ext = new StringBuffer(dialog_et.getText().toString());
									
									if(Vin_ext.length() >16){
										
										Vin_ext.insert(16," ");
										Vin_ext.insert(12," ");
										Vin_ext.insert(8," ");
										Vin_ext.insert(4," ");
										
									}else if (Vin_ext.length() >12){
										
										Vin_ext.insert(12," ");
										Vin_ext.insert(8," ");
										Vin_ext.insert(4," ");
										
									} else if (Vin_ext.length() >8){
										
										Vin_ext.insert(8," ");
										Vin_ext.insert(4," ");
										
									} else if (Vin_ext.length() >4){
										
										Vin_ext.insert(4," ");
										
									}
									
									Log.v("crjlog","Vin_ext.toString() = " +  Vin_ext.toString()); 
									
									mCursor = mVoteDB.query(VIN_TABLE_NAME,
											null, "vin like '%"
													+ Vin_ext.toString() + "%'",
											null, null, null, null);
									
//									mCursor = mVoteDB.query(VIN_TABLE_NAME,
//											null, "vin like '%"
//													+ dialog_et.getText().toString() + "%'",
//											null, null, null, null);
									// mDBListAdapter = new
									// DBVinAdapter(mCursor);
									// mDBlist.setAdapter(mDBListAdapter);
									updataQueryList(mCursor);

								} else {
									Toast.makeText(VinQuery.this,
											R.string.vinquery_dialog_edittext_empty,
											Toast.LENGTH_SHORT).show();
								}

								dismissEditTextDialogView();
							}
						});
					}
				});

				listDialog_bt2.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismissListDialogView();
						initListDialogView();

						dialog_title
								.setText(R.string.vinlist_dialog_query_title);
						listDialog_bt1
								.setText(R.string.vinlist_listdialog_status_y);
						listDialog_bt2
								.setText(R.string.vinlist_listdialog_status_n);

						listDialog_bt1
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub

										mCursor = mVoteDB.query(VIN_TABLE_NAME,
												null, "status=?",
												new String[] { "Y" }, null,
												null, null);
										// mDBListAdapter = new
										// DBVinAdapter(mCursor);
										// mDBlist.setAdapter(mDBListAdapter);
										updataQueryList(mCursor);

										dismissListDialogView();
									}
								});

						listDialog_bt2
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub

										mCursor = mVoteDB.query(VIN_TABLE_NAME,
												null, "status=?",
												new String[] { "N" }, null,
												null, null);
										// mDBListAdapter = new
										// DBVinAdapter(mCursor);
										// mDBlist.setAdapter(mDBListAdapter);
										updataQueryList(mCursor);

										dismissListDialogView();
									}
								});

					}
				});
			}
		});

//		mbuttonshow.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				insert_db();
//				updataAllList();
//			}
//		});

	}

	private void dismissEditTextDialogView() {

		if (DexttextDlg != null)
			DexttextDlg.dismiss();

	}

	private void dismissListDialogView() {

		if (listviewDlg != null)
			listviewDlg.dismiss();

	}

	private void initListDialogView() {

		listviewDlg = new AlertDialog.Builder(VinQuery.this).create();
		listviewDlg.show();
		Window window = listviewDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_listview);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window
				.findViewById(R.id.listview_dialog_title);
		listDialog_bt1 = (Button) window
				.findViewById(R.id.listview_dialog_listbutton1);
		listDialog_bt2 = (Button) window
				.findViewById(R.id.listview_dialog_listbutton2);

		dialog_bt_cancel = (Button) window
				.findViewById(R.id.listview_dialog_button_cancel);
		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissListDialogView();
			}
		});
	}

	private void initEdittextDialogView() {

		DexttextDlg = new AlertDialog.Builder(VinQuery.this).create();
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				ENTRY_VIN_NUM) });
		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() >= ENTRY_VIN_NUM) {
					Toast.makeText(
							VinQuery.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_VIN_NUM, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				try {
					String temp = arg0.toString();
					String tem = temp.substring(temp.length() - 1,
							temp.length());
					char[] temC = tem.toCharArray();
					int mid = temC[0];
					
					if(mid>=48&&mid<=57){ 
					return;
					}
					if(mid>=65&&mid<=90){ 
					return;
					}
					if(mid>=97&&mid<=122){ 
					return;
					}
					arg0.delete(temp.length()-1, temp.length());
					} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});

		dialog_et.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

		dialog_bt_ok = (Button) window
				.findViewById(R.id.edittext_dialog_button_ok);
		dialog_bt_cancel = (Button) window
				.findViewById(R.id.edittext_dialog_button_cancel);

		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissEditTextDialogView();
			}
		});

	}

	// insert vote.db
	private void insert_db() {

		// int Vin = (int) (Math.random() * 1000000000);

		String code = "34-16-10-003";
		String Vin = "1111 AFBB E729 444";
		// String Vin = "11 22";

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("hh:mm");
		String date = format.format(new Date());
		String time = format2.format(new Date());

		String[] readomWord = { "Y", "N" };
		String Status = readomWord[(int) (Math.random() * 2)];

		mVoteDB.insert_vintable(code, Vin, Status, date, time,
				preferences.getString("CURRENT_ELECTION_TYPE", "President"));
		Toast.makeText(this, "Add Successed!", Toast.LENGTH_SHORT).show();

	}

	//
	private void getSuccessNum() {

		Cursor mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "status=?",
				new String[] { "Y" }, null, null, null);
		mDBQueryTitle_s.setText(getResources().getString(
				R.string.userlist_title_success)
				+ " " + String.valueOf(mCursor.getCount()));
		mCursor.close();

	}

	private void getFailNum() {

		Cursor mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "status=?",
				new String[] { "N" }, null, null, null);
		mDBQueryTitle_f.setText(getResources().getString(
				R.string.userlist_title_fail)
				+ " " + String.valueOf(mCursor.getCount()));
		mCursor.close();
	}

	private void updataAllList() {
		getSuccessNum();
		getFailNum();
		mCursor = mVoteDB.Query_Vin_table();
		mDBQueryTitle.setText(getResources().getString(
				R.string.userlist_title_sum)
				+ " " + String.valueOf(mCursor.getCount()));
		mDBListAdapter = new DBVinAdapter(mCursor);
		mDBlist.setAdapter(mDBListAdapter);
	}

	private void updataQueryList(Cursor mCursor) {
		mDBQueryTitle.setText(getResources().getString(
				R.string.userlist_title_Query)
				+ " " + String.valueOf(mCursor.getCount()));
		mDBListAdapter = new DBVinAdapter(mCursor);
		mDBlist.setAdapter(mDBListAdapter);
	}

	public class DBVinAdapter extends BaseAdapter {
		private Context mContext;
		private Cursor mCursor;
		private LayoutInflater inflater;

		public DBVinAdapter(Cursor cursor) {

			mCursor = cursor;
		}

		public DBVinAdapter(Context context, Cursor cursor) {

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
				inflater = (LayoutInflater) VinQuery.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.vinquery_list, null);
				convertView.setTag(viewHolder);

				viewHolder.VinTitle = (TextView) convertView
						.findViewById(R.id.vinlist_vin);
				viewHolder.StatusTitle = (TextView) convertView
						.findViewById(R.id.vinlist_status);

			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.VinTitle.setText(mCursor.getString(2));
			viewHolder.StatusTitle.setText(mCursor.getString(3));

			return convertView;

		}

		class ViewHolder {
			private TextView VinTitle;
			private TextView StatusTitle;
		}
	}

}
