package com.example.jy.demo.fingerprint;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Vote_DBHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "Data.db";
	private final static int DATABASE_VERSION = 1;

	public final static String SYSLOG_TABLE_NAME = "systemlog_table";
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String DATE = "date";
	public final static String TIME = "time";
	public final static String EVENT = "event";
	public final static String ID_desc = "id desc";

	public final static String USER_TABLE_NAME = "user_table"; 
	public final static String ID_USER = "id";
	public final static String USER_NAME = "user_name";
	public final static String USER_PWD = "password";
	public final static String USER_TYPE = "type";		//0 -- admin , 1 -- C User , 2 -- V User

	public final static String ADMIN_USER_NAME = "Admin";
	public final static String DEFAULT_USER_NAME = "user";
	public final static int ENTRY_VIN_NUM = 15;
	public final static int ENTRY_NAME_NUM = 10;
	public final static int ENTRY_ADDRESS_NUM = 20;

	//gather
	public final static String GATHER_USER_TABLE_NAME = "gatherinfo_table"; 
	public final static String GATHER_ID_USER = "id";
	public final static String GATHER_USER_NAME = "gather_name";
	public final static String GATHER_GENDER = "gender";
	public final static String GATHER_BIRTHDAY = "birthday";
	public final static String GATHER_ADDRESS = "address";
	
	
	// public final static String USER_TIME = "login_time";

	public Vote_DBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		Create_Systemlog_table(db);
		
		Create_User_table(db);
		
		Create_Gather_table(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + SYSLOG_TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public void Create_Systemlog_table(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + SYSLOG_TABLE_NAME + "(" + ID
				+ " integer primary key autoincrement," 
				+ NAME + " String,"
				+ DATE + " text," 
				+ TIME + " text," 
				+ EVENT + " text" + ")");
	}

	public void Create_User_table(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + "("
				+ ID_USER + " integer primary key autoincrement," 
				+ USER_NAME	+ " String," 
				+ USER_PWD + " text,"
				+ USER_TYPE + " integer" + ")");
	}
	
	public void Create_Gather_table(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + GATHER_USER_TABLE_NAME + "("
				+ GATHER_ID_USER + " integer primary key autoincrement," 
				+ GATHER_USER_NAME + " String," 
				+ GATHER_GENDER + " String," 
				+ GATHER_BIRTHDAY + " String," 
				+ GATHER_ADDRESS + " text" + ")");
	}

	public Cursor Query_Syslog_table() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(SYSLOG_TABLE_NAME, null, null, null, null, null,
				ID_desc);
		return cursor;
	}

	public Cursor Query_User_table() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(USER_TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}
	
	public Cursor Query_Gather_table() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(GATHER_USER_TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}
	

	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy);

		return cursor;
	}

	// 增加操作
	// public long insert(Integer vin, String time, String status) {
	public long insert_syslogtable(String name, String event) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
		String date = format.format(new Date());
		String time = format2.format(new Date());
		
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(NAME, name);
		cv.put(DATE, date);
		cv.put(TIME, time);
		cv.put(EVENT, event);
		long is_ok = db.insert(SYSLOG_TABLE_NAME, null, cv);
		return is_ok;
	}

	public long insert_usertable(String userName, String passWord, int type) {
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(USER_NAME, userName);
		cv.put(USER_PWD, passWord);
		cv.put(USER_TYPE, type);

		long is_ok = db.insert(USER_TABLE_NAME, null, cv);
		return is_ok;
	}

	
	public long insert_gathertable(String userName, String gender, String birthday, String address) {
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(GATHER_USER_NAME, userName);
		cv.put(GATHER_GENDER, gender);
		cv.put(GATHER_BIRTHDAY, birthday);
		cv.put(GATHER_ADDRESS, address);
		long is_ok = db.insert(GATHER_USER_TABLE_NAME, null, cv);
		return is_ok;
	}
	
	
	// 删除操作
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = NAME + " = ?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(SYSLOG_TABLE_NAME, where, whereValue);
	}

	public void user_delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID_USER + " = ?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(USER_TABLE_NAME, where, whereValue);
	}

	// 修改操作
	public void update(int id, String bookname, String author) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = NAME + " = ?";
		String[] whereValue = { Integer.toString(id) };

		ContentValues cv = new ContentValues();
		cv.put(TIME, bookname);
		cv.put(EVENT, author);
		db.update(SYSLOG_TABLE_NAME, cv, where, whereValue);
	}
	
	public void update_usertable(int id, String name_pwd, Boolean is_name) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID_USER + " = ?";
		String[] whereValue = { Integer.toString(id) };
		ContentValues cv = new ContentValues();
		if(is_name){
			cv.put(USER_NAME, name_pwd);
		}else{
			cv.put(USER_PWD, name_pwd);
		}
		db.update(USER_TABLE_NAME, cv, where, whereValue);
	}
	
}