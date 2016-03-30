package com.example.jy.demo.fingerprint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.drm.DrmStore.Action;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DataEntryList extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	private ListView mListView;
	//	private TextView mPathView;
	private FileListAdapter mFileAdpter;
	private TextView mItemCount;
	private String[] dataListarray;

	// dialog view
	private AlertDialog listviewDlg,textviewDlg;
	private Button dialog_bt_cancel,dialog_bt_ok;
	private TextView dialog_title,dialog_tv;

	private Button dialog_listview_bt1,dialog_listview_bt2;
	private Button mbuttonback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dataentry_list);
		initView();
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.file_list);
//		mPathView = (TextView) findViewById(R.id.path);
		mItemCount = (TextView) findViewById(R.id.item_count);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mbuttonback = (Button) findViewById(R.id.file_list_button_back);

		dataListarray = getResources().getStringArray(R.array.datalist_item);

		mbuttonback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		initDataList();
	}

	private void initDataList(){

//		File folder = new File(Environment.getExternalStorageDirectory()
//				.toString() + "/" + getResources().getString(R.string.app_name));

		File folder = this.getFilesDir();
		initData(folder);
	}

	private void initData(File folder) {
//		mPathView.setText(folder.getAbsolutePath());
		ArrayList<File> files = new ArrayList<File>();
		File[] filterFiles = folder.listFiles();
		mItemCount.setText(getResources()
				.getString(R.string.userlist_title_sum)
				+ " "
				+ filterFiles.length);
		if (null != filterFiles && filterFiles.length > 0) {
			for (File file : filterFiles) {
				files.add(file);
			}
		}
		mFileAdpter = new FileListAdapter(this, files);
		mListView.setAdapter(mFileAdpter);
	}

	private class FileListAdapter extends BaseAdapter {

		private ArrayList<File> files;
		private LayoutInflater mInflater;

		public FileListAdapter(Context context, ArrayList<File> files) {
			this.files = files;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public Object getItem(int position) {
			return files.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.dataentry_list_item,
						null);
				convertView.setTag(viewHolder);
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.file_title);
				viewHolder.data = (TextView) convertView
						.findViewById(R.id.file_date);
				viewHolder.size = (TextView) convertView
						.findViewById(R.id.file_size);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			File file = (File) getItem(position);
			String fileName = file.getName();
			viewHolder.title.setText(fileName);
			long fileSize = file.length();
			if (fileSize > 1024 * 1024) {
				float size = fileSize / (1024f * 1024f);
				viewHolder.size.setText(new DecimalFormat("#.00").format(size)
						+ " " + "MB");
			} else if (fileSize >= 1024) {
				float size = fileSize / 1024;
				viewHolder.size.setText(new DecimalFormat("#.00").format(size)
						+ " " + "KB");
			} else {
				viewHolder.size.setText(fileSize + " " + "B");
			}
			viewHolder.data.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm")
					.format(file.lastModified()));
			return convertView;
		}

		class ViewHolder {
			private TextView title;
			private TextView data;
			private TextView size;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		File file = (File) mFileAdpter.getItem(position);
		if (file.isDirectory()) {
			initData(file);
		} else {
			try {
				openFile(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void openFile(File file) throws Exception {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "text/html");
		try {
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, R.string.dataentry_filelist_open_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void dismissDialoglistView() {

		if(listviewDlg != null)
			listviewDlg.dismiss();

	}

	private void dismissDialogtextView() {

		if(textviewDlg != null)
			textviewDlg.dismiss();

	}

	private void initDialogTextView() {

		textviewDlg = new AlertDialog.Builder(DataEntryList.this).create();
		textviewDlg.show();
		Window window = textviewDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_text);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window.findViewById(R.id.text_dialog_title);
		dialog_tv = (TextView) window.findViewById(R.id.text_dialog_tv);

		dialog_bt_ok = (Button) window.findViewById(R.id.text_dialog_button_ok);
		dialog_bt_cancel = (Button) window.findViewById(R.id.text_dialog_button_cancel);

		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissDialogtextView();
			}
		});
	}

	private void initDialoglistView() {

		listviewDlg = new AlertDialog.Builder(DataEntryList.this).create();
		listviewDlg.show();
		Window window = listviewDlg.getWindow();
		window.setContentView(R.layout.theme_dialog_listview);
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		dialog_title = (TextView) window.findViewById(R.id.listview_dialog_title);
		dialog_listview_bt1 = (Button) window.findViewById(R.id.listview_dialog_listbutton1);
		dialog_listview_bt2 = (Button) window.findViewById(R.id.listview_dialog_listbutton2);
		dialog_listview_bt1.setText(dataListarray[0]);
		dialog_listview_bt2.setText(dataListarray[1]);

		dialog_bt_cancel = (Button) window.findViewById(R.id.listview_dialog_button_cancel);
		dialog_bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissDialoglistView();
			}
		});
	}




	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
		// TODO Auto-generated method stub
		final File file = (File) mFileAdpter.getItem(arg2);
		final String path = file.getAbsolutePath();
		final String fileName = file.getName();

		initDialoglistView();

		dialog_title.setText(R.string.userlist_dialogtitle);

		dialog_listview_bt1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String data = readFile(path);

				Intent it = new Intent(DataEntryList.this,DataEntry.class);
				it.putExtra("dataInfo", data);
//				it.putExtra("dataPath", path);
				it.putExtra("dataPath", fileName);
				startActivityForResult(it, 0);


				Log.v("crjlog", " data  = " + data );
				Log.v("crjlog", " fileName  = " + fileName );

				dismissDialoglistView();
			}
		});


		dialog_listview_bt2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dismissDialoglistView();
				initDialogTextView();

				dialog_title.setText(R.string.screen_mainmenu_dialog_title);
				dialog_tv.setText(R.string.dataentry_filelist_delete_dialog);

				dialog_bt_ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub 

						dataDeleteFile(file);
						initDataList();
						dismissDialogtextView();

					}
				});

			}
		});

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		initDataList();
	}

	private void dataDeleteFile(File file) {

		try {
			if (file.exists()) { // 判断文件是否存在
				file.delete();
				Toast.makeText(this, R.string.dataentry_filelist_delete_success,
						Toast.LENGTH_SHORT).show();
			} else {

				Toast.makeText(this, R.string.vinlist_dialog_query_title,
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(this, R.string.dataentry_filelist_delete_error,
					Toast.LENGTH_SHORT).show();
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

}