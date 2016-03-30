package com.example.jy.demo.fingerprint;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GatherQuery extends Activity {

	private Button mbuttonback, mbuttonquery;//, mbuttonshow;
	private TextView mDBQueryTitle, mDBQueryTitle_s, mDBQueryTitle_f,
			mDBQueryTitle_q;
	private EditText mEditTextVin;
	private Vote_DBHelper mVoteDB;
	private Cursor mCursor;
	private ListView mDBlist;
	private String[] queryListItem;

	private DBVinAdapter mDBListAdapter;

	private String GATHER_TABLE_NAME;
	private int ENTRY_VIN_NUM;

	private SharedPreferences preferences;

	// dialog view
	private AlertDialog DexttextDlg, textviewDlg, listviewDlg; 
	private Button dialog_bt_cancel, dialog_bt_ok, listDialog_bt1,listDialog_bt2,listDialog_bt3;
	private EditText dialog_et;
	private TextView dialog_tv, dialog_title;
	private Boolean is_Query = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.db_query_gather);
		mVoteDB = new Vote_DBHelper(this);
		mCursor = mVoteDB.Query_Gather_table();
		mDBListAdapter = new DBVinAdapter(this, mCursor);

		ENTRY_VIN_NUM = mVoteDB.ENTRY_VIN_NUM;
		GATHER_TABLE_NAME = mVoteDB.GATHER_USER_TABLE_NAME;

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

//		mDBQueryTitle_s = (TextView) findViewById(R.id.vinquery_num_success);
//		mDBQueryTitle_f = (TextView) findViewById(R.id.vinquery_num_fail);

//		getSuccessNum();
//		getFailNum();

		queryListItem = getResources().getStringArray(R.array.gather_query_item);

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

				initListDialogView_3line();
				dialog_title.setText(R.string.vinlist_dialog_query_title);

				listDialog_bt1.setText(R.string.gather_query_id);
				listDialog_bt2.setText(R.string.gather_query_name);
				listDialog_bt3.setText(R.string.gather_query_gender);
				

				listDialog_bt1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismissListDialogView();
						initEdittextDialogView();

						dialog_tv.setText(R.string.gatherlist_dialogtitle_query_id);
						dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {

								if (dialog_et.getText().length() > 0) {
									
									StringBuffer Vin_ext = new StringBuffer(dialog_et.getText().toString());
									
									Log.v("crjlog","Vin_ext.toString() = " +  Vin_ext.toString()); 
									
									mCursor = mVoteDB.query(GATHER_TABLE_NAME,
											null, "id=?",
											new String[] {Vin_ext.toString()}, null,
											null, null);
									
									updataQueryList(mCursor);

								} else {
									Toast.makeText(GatherQuery.this,
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
						initEdittextDialogView();

						dialog_tv.setText(R.string.gatherlist_dialogtitle_query_name);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {

								if (dialog_et.getText().length() > 0) {
									
									StringBuffer name_ext = new StringBuffer(dialog_et.getText().toString());
									
									Log.v("crjlog","name_ext.toString() = " +  name_ext.toString()); 
									
//									mCursor = mVoteDB.query(GATHER_TABLE_NAME,
//											null, "gather_name=?",
//											new String[] {name_ext.toString()}, null,
//											null, null);
									
									mCursor = mVoteDB.query(GATHER_TABLE_NAME,
											null, "gather_name like '%"
													+ name_ext.toString() + "%'",
											null, null, null, null);

									
									// mDBListAdapter = new
									// DBVinAdapter(mCursor);
									// mDBlist.setAdapter(mDBListAdapter);
									updataQueryList(mCursor);

								} else {
									Toast.makeText(GatherQuery.this,
											R.string.vinquery_dialog_edittext_empty,
											Toast.LENGTH_SHORT).show();
								}

								dismissEditTextDialogView();
							}
						});
					}
				});
				
				
				listDialog_bt3.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismissListDialogView();
						initListDialogView();

						dialog_title
								.setText(R.string.vinlist_dialog_query_title);
						listDialog_bt1
								.setText(R.string.gatherlist_listdialog_male);
						listDialog_bt2
								.setText(R.string.gatherlist_listdialog_Female);

						listDialog_bt1
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub

										mCursor = mVoteDB.query(GATHER_TABLE_NAME,
												null, "gender=?",
												new String[] { "Male" }, null,
												null, null);
										
										updataQueryList(mCursor);

										dismissListDialogView();
									}
								});

						listDialog_bt2
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub

										mCursor = mVoteDB.query(GATHER_TABLE_NAME,
												null, "gender=?",
												new String[] { "Female" }, null,
												null, null);
										
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

	private void initListDialogView_3line() {

		listviewDlg = new AlertDialog.Builder(GatherQuery.this).create();
		listviewDlg.show();
		Window window = listviewDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_listview_3line);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window
				.findViewById(R.id.listview_dialog_title);
		listDialog_bt1 = (Button) window
				.findViewById(R.id.listview_dialog_listbutton1);
		listDialog_bt2 = (Button) window
				.findViewById(R.id.listview_dialog_listbutton2);
		listDialog_bt3 = (Button) window
				.findViewById(R.id.listview_dialog_listbutton3);

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
	
	private void initListDialogView() {

		listviewDlg = new AlertDialog.Builder(GatherQuery.this).create();
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

		DexttextDlg = new AlertDialog.Builder(GatherQuery.this).create();
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
							GatherQuery.this,
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
//	private void insert_db() {
//
//		// int Vin = (int) (Math.random() * 1000000000);
//
//		String code = "34-16-10-003";
//		String Vin = "1111 AFBB E729 444";
//		// String Vin = "11 22";
//
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat format2 = new SimpleDateFormat("hh:mm");
//		String date = format.format(new Date());
//		String time = format2.format(new Date());
//
//		String[] readomWord = { "Y", "N" };
//		String Status = readomWord[(int) (Math.random() * 2)];
//
//		mVoteDB.insert_vintable(code, Vin, Status, date, time,
//				preferences.getString("CURRENT_ELECTION_TYPE", "President"));
//		Toast.makeText(this, "Add Successed!", Toast.LENGTH_SHORT).show();
//
//	}

//	//
//	private void getSuccessNum() {
//
//		Cursor mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "status=?",
//				new String[] { "Y" }, null, null, null);
//		mDBQueryTitle_s.setText(getResources().getString(
//				R.string.userlist_title_success)
//				+ " " + String.valueOf(mCursor.getCount()));
//		mCursor.close();
//
//	}
//
//	private void getFailNum() {
//
//		Cursor mCursor = mVoteDB.query(VIN_TABLE_NAME, null, "status=?",
//				new String[] { "N" }, null, null, null);
//		mDBQueryTitle_f.setText(getResources().getString(
//				R.string.userlist_title_fail)
//				+ " " + String.valueOf(mCursor.getCount()));
//		mCursor.close();
//	}

	private void updataAllList() {
//		getSuccessNum();
//		getFailNum();
		
		mCursor = mVoteDB.Query_Gather_table();
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
				inflater = (LayoutInflater) GatherQuery.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.gatherquery_list, null);
				convertView.setTag(viewHolder);

				viewHolder.Title_id = (TextView) convertView
						.findViewById(R.id.gatherlist_id);
				viewHolder.Title_name = (TextView) convertView
						.findViewById(R.id.gatherlist_name);
				viewHolder.Title_gender = (TextView) convertView
						.findViewById(R.id.gatherlist_gender);

			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			String sum = String.format("%1$,08d",Integer.parseInt(mCursor.getString(0))); 

			viewHolder.Title_id.setText(sum);
			viewHolder.Title_name.setText(mCursor.getString(1));
			viewHolder.Title_gender.setText(mCursor.getString(2));

			return convertView;

		}

		class ViewHolder {
			private TextView Title_id;
			private TextView Title_name;
			private TextView Title_gender;

		}
	}

}
