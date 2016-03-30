package com.example.jy.demo.fingerprint;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserQuery extends Activity {

	private Button mbuttonback, mbuttonquery, mbuttonAdd, mbuttonDelete;
	private TextView mUserlistQueryNum;
	private Vote_DBHelper mVoteDB;
	private Cursor mCursor, mQueryCursor;
	private ListView mDBlist;

	private DBUserAdapter mDBListAdapter;
	private String USER_TABLE_NAME, USER_NAME, USER_PWD;
	private int litemNem;

	private int ENTRY_NAME_NUM;

	// dialog view
	private AlertDialog DexttextDlg, textviewDlg, listviewDlg;
	private Button dialog_bt_cancel, dialog_bt_ok, listDialog_bt1,
			listDialog_bt2;
	private EditText dialog_et;
	private TextView dialog_tv, dialog_title;
	private Boolean is_Query = false;

	private final List<Integer> mIdList = new ArrayList<Integer>();
	
    private String[] type = {"Collection User","Verification User"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.db_query_user);
		mVoteDB = new Vote_DBHelper(this);
		USER_TABLE_NAME = mVoteDB.USER_TABLE_NAME;
		USER_NAME = mVoteDB.USER_NAME;
		USER_PWD = mVoteDB.USER_PWD;
		ENTRY_NAME_NUM = mVoteDB.ENTRY_NAME_NUM;
		mCursor = mVoteDB.Query_User_table();

		setListData();

		mDBListAdapter = new DBUserAdapter(this, mCursor, mIdList);

		mUserlistQueryNum = (TextView) findViewById(R.id.userquery_num);
		mUserlistQueryNum.setText(getResources().getString(
				R.string.userlist_title_sum)
				+ " " + String.valueOf(mCursor.getCount()));

		mbuttonquery = (Button) findViewById(R.id.button_userQuery);
		mbuttonAdd = (Button) findViewById(R.id.button_add);
		mbuttonDelete = (Button) findViewById(R.id.button_delete);
		mbuttonback = (Button) findViewById(R.id.button_back);
		mDBlist = (ListView) findViewById(R.id.votedb_list);
		mDBlist.setAdapter(mDBListAdapter);

		mDBlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long itemId) {
				// TODO Auto-generated method stub

				int id = (int) ((DBUserAdapter) mDBlist.getAdapter())
						.getItemId(position);
				
				if(id != 1){
					
					CheckBox checkBox = (CheckBox) arg1
							.findViewById(R.id.userlist_checkbox);

					if (checkBox.isChecked()) {

						checkBox.setChecked(false);
						((DBUserAdapter) mDBlist.getAdapter()).setCheckBox(id,
								false);

					} else {

						checkBox.setChecked(true);
						((DBUserAdapter) mDBlist.getAdapter())
								.setCheckBox(id, true);

					}
				}

			}
		});

		mDBlist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				litemNem = arg2;

				initListDialogView();

				dialog_title.setText(R.string.userlist_dialogtitle);

				listDialog_bt1.setText(R.string.userlist_text_modifyname);
				listDialog_bt2.setText(R.string.userlist_text_modifypwd);

				listDialog_bt1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						if(((DBUserAdapter) mDBlist.getAdapter()).getItemId(litemNem) == 1){
							
							Toast.makeText(UserQuery.this,getResources().getString(R.string.userlist_toast_changename_admin),Toast.LENGTH_SHORT).show();
							dismissListDialogView();
							return;
						}
						
						
						dismissListDialogView();

						initEdittextDialogView();

						dialog_tv.setText(R.string.entry_username_modify_toast);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								if (dialog_et.getText().length() > 0) {
									
									if(dialog_et.getText().toString().equals("imei") || dialog_et.getText().toString().equals("IMEI")
											|| dialog_et.getText().toString().equals("INEC") ||dialog_et.getText().toString().equals("inec")){
											
											Toast.makeText(UserQuery.this,
													R.string.add_user_s_fail_toast,
													Toast.LENGTH_SHORT).show();
											
											dialog_et.setText("");
											
											return;
									}									
									
									// 判断修改的用户名是否存在
									if (queryUser(dialog_et.getText().toString()) > 0) {
										// 此用户已存在
										Toast.makeText(UserQuery.this,
												R.string.add_user_error_toast,
												Toast.LENGTH_SHORT).show();

									} else {										
										
										try {
											if (is_Query) {
												mQueryCursor
												.moveToPosition(litemNem);
												mVoteDB.update_usertable(
														mQueryCursor.getInt(0),
														dialog_et.getText()
														.toString(), true);
												Toast.makeText(
														UserQuery.this,
														R.string.userlist_success_modifyname,
														Toast.LENGTH_SHORT).show();
												updataAllList();
												
											} else {
												mCursor = mVoteDB
														.Query_User_table();
												mCursor.moveToPosition(litemNem);
												mVoteDB.update_usertable(mCursor
														.getInt(0), dialog_et
														.getText().toString(), true);
												Toast.makeText(
														UserQuery.this,
														R.string.userlist_success_modifyname,
														Toast.LENGTH_SHORT).show();
												updataAllList();
												
											}
											
											// 系统日志 修改用户名
											mVoteDB.insert_syslogtable(
													"Admin",
													getResources()
													.getString(
															R.string.System_Log_event_changename));
										} catch (Exception e) {
											// TODO: handle exception
											Toast.makeText(
													UserQuery.this,
													R.string.userlist_fail_modifyname,
													Toast.LENGTH_SHORT).show();
										}
										
									}

								} else {
									Toast.makeText(UserQuery.this,
											R.string.add_username_empty_toast,
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
						dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
						dialog_tv.setText(R.string.entry_user_pwd_toast);
						dialog_bt_ok.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								if (dialog_et.getText().length() > 0) {
									try {

										if (is_Query) {
											mQueryCursor
													.moveToPosition(litemNem);
											mVoteDB.update_usertable(
													mQueryCursor.getInt(0),
													dialog_et.getText()
															.toString(), false);
											Toast.makeText(
													UserQuery.this,
													R.string.userlist_success_modifypwd,
													Toast.LENGTH_SHORT).show();
											updataAllList();

										} else {
											mCursor = mVoteDB
													.Query_User_table();
											mCursor.moveToPosition(litemNem);
											mVoteDB.update_usertable(mCursor
													.getInt(0), dialog_et
													.getText().toString(),
													false);
											Toast.makeText(
													UserQuery.this,
													R.string.userlist_success_modifypwd,
													Toast.LENGTH_SHORT).show();
											updataAllList();
										}

										// 系统日志 修改用户密码
										mVoteDB.insert_syslogtable(
												"Admin",
												getResources()
														.getString(
																R.string.System_Log_event_changepwd));

									} catch (Exception e) {
										// TODO: handle exception
										Toast.makeText(
												UserQuery.this,
												R.string.userlist_fail_modifypwd,
												Toast.LENGTH_SHORT).show();
									}

								} else {
									Toast.makeText(UserQuery.this,
											R.string.add_userpwd_empty_toast,
											Toast.LENGTH_SHORT).show();
								}

								dismissEditTextDialogView();
							}
						});
					}
				});

				return false;
			}
		});

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

				initEdittextDialogView();

				dialog_tv.setText(R.string.userlist_dialogtitle_query);
				dialog_bt_ok.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (dialog_et.getText().length() > 0) {

							mCursor = mVoteDB.query(USER_TABLE_NAME, null,
									"user_name like '%"
											+ dialog_et.getText().toString()
											+ "%'", null, null, null, null);
							mUserlistQueryNum.setText(getResources().getString(
									R.string.userlist_title_sum)
									+ " " + String.valueOf(mCursor.getCount()));
							updataQueryList(mCursor);
							Toast.makeText(UserQuery.this,
									R.string.userlist_toast_query_over,
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(UserQuery.this,
									R.string.add_username_empty_toast,
									Toast.LENGTH_SHORT).show();
						}

						dismissEditTextDialogView();
					}
				});
			}
		});

		mbuttonAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				chooseUsertype();
				
				updataAllList();
				// 系统日志 新增用户
				mVoteDB.insert_syslogtable(
						"Admin",
						getResources().getString(
								R.string.System_Log_event_adduser));

			}
		});

		mbuttonDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				initTextDialogView();

				dialog_title.setText(R.string.screen_mainmenu_dialog_title);
				dialog_tv.setText(R.string.userlist_dialogtitle_delete);
				dialog_bt_ok.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						boolean is_delete = false;

						if (!is_Query) {
							mCursor = mVoteDB.Query_User_table();
							mCursor.moveToFirst();
						}

						List<Integer> mItemId;
						mItemId = mDBListAdapter.getCheckedPosList();
						Log.v("crjlog", " mItemId = " + mItemId);
						Log.v("crjlog", " mItemId.size() = " + mItemId.size());

						int delete_sum = 0;
						for (int i = 0; i < mItemId.size(); i++) {

							Log.v("crjlog",
									" mItemId.get(i) = " + mItemId.get(i));
							// mCursor = mVoteDB.Query_User_table();
							// mCursor.moveToFirst();
							// mCursor.moveToPosition(mItemId.get(i) - 1);
							mVoteDB.user_delete(mItemId.get(i));
							is_delete = true;
							delete_sum++;

						}

						// for (int i = 0; i < mCursor.getCount(); i++) {
						//
						// if (mDBListAdapter.getCheckboxflag(i) == true) {
						// mCursor.moveToPosition(i);
						// if (mCursor.getInt(0) != 1) {
						// mVoteDB.user_delete(mCursor.getInt(0));
						// is_delete = true;
						// delete_sum++;
						// } else {
						// Toast.makeText(
						// UserQuery.this,
						// R.string.userlist_toast_delete_admin,
						// Toast.LENGTH_SHORT).show();
						// }
						// }
						// }
						// Log.v("crjlog", " delete_sum = " + delete_sum);

						if (is_delete) {

							Toast.makeText(UserQuery.this,
									getResources().getString(R.string.userlist_toast_success_delete) + " " + delete_sum,
									Toast.LENGTH_SHORT).show();
							updataAllList();

							// 系统日志 删除用户
							mVoteDB.insert_syslogtable(
									"Admin",
									getResources()
											.getString(
													R.string.System_Log_event_deleteuser));
						} else {
							Toast.makeText(UserQuery.this,
									R.string.userlist_toast_delete_noselete,
									Toast.LENGTH_SHORT).show();
						}

						dismissTextDialogView();
					}
				});
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("crjlog", "onDestroy");
		try {
			mCursor.close();
			mVoteDB.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void dismissEditTextDialogView() {

		if (DexttextDlg != null)
			DexttextDlg.dismiss();

	}

	private void dismissTextDialogView() {

		if (textviewDlg != null)
			textviewDlg.dismiss();

	}

	private void dismissListDialogView() {

		if (listviewDlg != null)
			listviewDlg.dismiss();

	}

	private void initListDialogView() {

		listviewDlg = new AlertDialog.Builder(UserQuery.this).create();
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

	private void initTextDialogView() {

		textviewDlg = new AlertDialog.Builder(UserQuery.this).create();
		textviewDlg.show();
		Window window = textviewDlg.getWindow();
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
				updataAllList();
				dismissTextDialogView();
			}
		});
	}

	private void initEdittextDialogView() {

		DexttextDlg = new AlertDialog.Builder(UserQuery.this).create();
		DexttextDlg.show();
		Window window = DexttextDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_edittext);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_tv = (TextView) window.findViewById(R.id.edittext_dialog_title);
		dialog_et = (EditText) window.findViewById(R.id.edittext_dialog_et);
		dialog_et.setSingleLine();
		dialog_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				ENTRY_NAME_NUM) });
		dialog_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() >= ENTRY_NAME_NUM) {
					Toast.makeText(
							UserQuery.this,
							getResources().getString(
									R.string.entry_max_num_toast)
									+ ENTRY_NAME_NUM, Toast.LENGTH_SHORT)
							.show();
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

					if (mid >= 48 && mid <= 57) {
						return;
					}
					if (mid >= 65 && mid <= 90) {
						return;
					}
					if (mid >= 97 && mid <= 122) {
						return;
					}
					arg0.delete(temp.length() - 1, temp.length());
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

	private void setListData() {

		mIdList.clear();
		mCursor.moveToFirst();

		for (int j = 0; j < mCursor.getCount(); j++) {

			mIdList.add(mCursor.getInt(0));
			mCursor.moveToNext();
		}

		Log.v("crjlog", "mIdList = " + mIdList);

	}

	// insert vote.db
	private void insert_db(final int usertype) {

		initEdittextDialogView();

		dialog_tv.setText(R.string.entry_user_name_toast);
		dialog_bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 用户名不能为空
				if (dialog_et.getText().length() > 0) {
					
					if(dialog_et.getText().toString().equals("imei") || dialog_et.getText().toString().equals("IMEI")
						|| dialog_et.getText().toString().equals("INEC") ||dialog_et.getText().toString().equals("inec")){
						
						Toast.makeText(UserQuery.this,
								R.string.add_user_s_fail_toast,
								Toast.LENGTH_SHORT).show();
						
						dialog_et.setText("");
						
						return;
					}
					

					if (queryUser(dialog_et.getText().toString()) > 0) {
						// 此用户已存在
						Toast.makeText(UserQuery.this,
								R.string.add_user_error_toast,
								Toast.LENGTH_SHORT).show();

					} else {

						final String insert_name = dialog_et.getText()
								.toString();
						dismissEditTextDialogView();

						initEdittextDialogView();
						dialog_et.setInputType(InputType.TYPE_CLASS_NUMBER);
						dialog_tv.setText(R.string.entry_user_pwd_toast);

						dialog_bt_ok.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// 用户名不能为空
								if (dialog_et.getText().length() > 0) {
									long is_ok = mVoteDB.insert_usertable(
											insert_name, dialog_et.getText()
													.toString(),usertype);
									if (is_ok != -1) {
										updataAllList();
										Toast.makeText(
												UserQuery.this,
												R.string.add_user_success_toast,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(UserQuery.this,
												R.string.add_user_fail_toast,
												Toast.LENGTH_SHORT).show();
									}

								} else {
									// 用户密码为空提示
									Toast.makeText(UserQuery.this,
											R.string.add_userpwd_empty_toast,
											Toast.LENGTH_SHORT).show();
								}

								dismissEditTextDialogView();
							}
						});
					}
				} else {
					// 用户名为空提示
					Toast.makeText(UserQuery.this,
							R.string.add_username_empty_toast,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	private int queryUser(String name) {
		
		mCursor = mVoteDB.query(USER_TABLE_NAME, null, "USER_NAME = '" + name
				+ "'", null, null, null, null);
		mCursor.moveToFirst();
		int Count = mCursor.getCount();
		return Count;

	}

	private void updataAllList() {

		mCursor = mVoteDB.Query_User_table();
		mUserlistQueryNum.setText(getResources().getString(
				R.string.userlist_title_sum)
				+ " " + String.valueOf(mCursor.getCount()));

		setListData();
		mDBListAdapter = new DBUserAdapter(this, mCursor, mIdList);
		mDBlist.setAdapter(mDBListAdapter);
		is_Query = false;
	}

	private void updataQueryList(Cursor mCursor) {
		setListData();
		mDBListAdapter = new DBUserAdapter(mCursor,mIdList);
		mDBlist.setAdapter(mDBListAdapter);

		mQueryCursor = mCursor;
		is_Query = true;
	}

	public class DBUserAdapter extends BaseAdapter {
		private Context mContext;
		private Cursor mCursor;
		private LayoutInflater inflater;
		private TextView nameTitle, pwdTitle;

		private SparseBooleanArray mCheckStates;
		private List<Integer> mCheckedItemId;

		private CheckBox Titlecheck;
		private List<Integer> mIdList_ext;

		public DBUserAdapter(Cursor cursor, List<Integer> idList) {

			mCursor = cursor;
			mIdList_ext = idList;
			mCheckStates = new SparseBooleanArray();
			mCheckedItemId = new ArrayList<Integer>();
		}

		public DBUserAdapter(Context context, Cursor cursor,
				List<Integer> idList) {

			mContext = context;
			mCursor = cursor;
			mIdList_ext = idList;
			mCheckStates = new SparseBooleanArray();
			mCheckedItemId = new ArrayList<Integer>();

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

			long id = mIdList_ext.get(position);
			return id;
		}

		protected List<Integer> getCheckedPosList() {
			int listSize = mCheckStates.size();
			for (int i = 0; i < listSize; i++) {
				boolean state = mCheckStates.valueAt(i);
				int curPos = mCheckStates.keyAt(i);
				if (state && !mCheckedItemId.contains(curPos)) {
					mCheckedItemId.add(curPos);
				}
			}

			Log.v("crjlog", "mCheckedItemId1 = " + mCheckedItemId);
			return mCheckedItemId;
		}

		protected void setCheckBox(int id, boolean checked) {

			mCheckStates.put(id, checked);
		}

		//
		// public boolean getCheckboxflag(int position) {
		//
		// View mView = mDBlist.getChildAt(position);
		// if(mView != null){
		//
		// Titlecheck = (CheckBox) mView.findViewById(R.id.userlist_name);
		// if (Titlecheck == null) {
		//
		// Log.v("crjlog", " position1 = " + position);
		// return false;
		//
		// } else {
		// Log.v("crjlog", " position2 = " + position);
		// return Titlecheck.isChecked();
		// }
		//
		// }
		//
		// Log.v("crjlog", " position3 = " + position);
		// return false;
		//
		// }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			mCursor.moveToPosition(position);
			View itemView = convertView;
			inflater = (LayoutInflater) UserQuery.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.userquery_list, null);

			Titlecheck = (CheckBox) itemView
					.findViewById(R.id.userlist_checkbox);
			int id = (int) getItemId(position);
			Titlecheck.setChecked(mCheckStates.get(id));

			if (mCursor.getInt(0) == 1) {
				Titlecheck.setVisibility(View.INVISIBLE);
			}

			nameTitle = (TextView) itemView.findViewById(R.id.userlist_name);
			nameTitle.setText(mCursor.getString(1));

			pwdTitle = (TextView) itemView.findViewById(R.id.userlist_pwd);
			pwdTitle.setText(mCursor.getString(2));

			return itemView;

		}
	}
	
	private void chooseUsertype() {
		
		new AlertDialog.Builder(UserQuery.this) 
		.setTitle(getResources().getString(R.string.wt_text_choose))
		.setIcon(android.R.drawable.ic_dialog_info)                  
		.setSingleChoiceItems(type, 0,   
		  new DialogInterface.OnClickListener() {  
		                              
		     public void onClick(DialogInterface dialog, int which) {  
		    	 
				insert_db(which+1);
		        dialog.dismiss();  
		        
		     }  
		  }  
		)  
		.setNegativeButton(getResources().getString(R.string.cancel), null)  
		.create()
		.show();  

	}
	
}
