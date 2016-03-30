package com.example.jy.demo.fingerprint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VoteVin_DBHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "Vote.db";
	private final static int DATABASE_VERSION = 1;

	public final static String VIN_TABLE_NAME = "vin_table";
	public final static String ID = "ID";
	public final static String CODE = "CODE";
	public final static String VIN = "VIN";
	public final static String DATE = "DATE";
	public final static String TIME = "TIME";
	public final static String STATUS = "STATUS";
	public final static String ET = "VTYPE";
	public final static String ID_desc = "id desc";
	public final static int ENTRY_VIN_NUM = 19; 
	
//
//	public final static String USER_TABLE_NAME = "user_table"; 
//	public final static String ID_USER = "id";
//	public final static String USER_NAME = "user_name";
//	public final static String USER_PWD = "password";
//
//	public final static String ADMIN_USER_NAME = "Admin";
//	public final static int ADMIN_USER_PWD = 1234;
//	public final static int ENTRY_NAME_NUM = 10;
	
	// public final static String USER_TIME = "login_time";

	public VoteVin_DBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		Create_Vin_table(db);
//		Create_User_table(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + VIN_TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public void Create_Vin_table(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + VIN_TABLE_NAME + "(" + ID
				+ " integer primary key autoincrement," + CODE + " text,"
				+ VIN + " text," + STATUS + " text,"
				+ DATE + " text," + TIME + " text," + ET + " text" + ")");
	}

//	public void Create_User_table(SQLiteDatabase db) {
//
//		db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + "("
//				+ ID_USER + " integer primary key autoincrement," + USER_NAME
//				+ " String," + USER_PWD + " text" + ")");
//	}

	public Cursor Query_Vin_table() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(VIN_TABLE_NAME, null, null, null, null, null,
				ID_desc);
		return cursor;
	}

//	public Cursor Query_User_table() {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(USER_TABLE_NAME, null, null, null, null, null,
//				null);
//		return cursor;
//	}

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
	public void insert_vintable(String code,String vin, String status, String date ,String time, String et) {
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues(); 
		
		cv.put(CODE, code);
		cv.put(VIN, vin);
		cv.put(STATUS, status);
		cv.put(DATE, date);
		cv.put(TIME, time);
		cv.put(ET, et);
		
		db.insert(VIN_TABLE_NAME, null, cv);
	}

//	public long insert_usertable(String userName, String passWord) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		/* ContentValues */
//		ContentValues cv = new ContentValues();
//		cv.put(USER_NAME, userName);
//		cv.put(USER_PWD, passWord);
//		long is_ok = db.insert(USER_TABLE_NAME, null, cv);
//		return is_ok;
//	}

	// 删除操作
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = VIN + " = ?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(VIN_TABLE_NAME, where, whereValue);
	}

//	public void user_delete(int id) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		String where = ID_USER + " = ?";
//		String[] whereValue = { Integer.toString(id) };
//		db.delete(USER_TABLE_NAME, where, whereValue);
//	}

	// 修改操作
	public void update_vintable(String vin, String Status) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = VIN + " = ?";
		String[] whereValue = {vin};
		
		ContentValues cv = new ContentValues();
		cv.put(STATUS, Status);
		db.update(VIN_TABLE_NAME, cv, where, whereValue);
	}
	
//	public void update_usertable(int id, String name_pwd, Boolean is_name) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		String where = ID_USER + " = ?";
//		String[] whereValue = { Integer.toString(id) };
//		ContentValues cv = new ContentValues();
//		if(is_name){
//			cv.put(USER_NAME, name_pwd);
//		}else{
//			cv.put(USER_PWD, name_pwd);
//		}
//		db.update(USER_TABLE_NAME, cv, where, whereValue);
//	}
	
}