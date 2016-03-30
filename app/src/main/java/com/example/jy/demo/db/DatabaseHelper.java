package com.example.jy.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.jy.demo.bean.Account;
import com.example.jy.demo.passport.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Administrator on 2016/3/18.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "REFT_DATABASE";
    private static final int DATABASE_VERSION = 1;

    {
        createTable();
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }

    private void createTable() {
        try {
            TableUtils.createTableIfNotExists(getConnectionSource(), Account.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
