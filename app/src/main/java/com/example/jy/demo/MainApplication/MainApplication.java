package com.example.jy.demo.MainApplication;

import android.app.Application;

import com.example.jy.demo.bean.Account;
import com.example.jy.demo.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * Created by Administrator on 2016/3/18.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initDatabase();
    }

    private void initDatabase() {
        Dao accountDao;
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        try {
            accountDao = databaseHelper.getDao(Account.class);
            Account collectionUser = new Account();
            collectionUser.setUser("userc");
            collectionUser.setPassword("123");
            accountDao.createIfNotExists(collectionUser);

            Account verifyUser = new Account();
            verifyUser.setUser("userv");
            verifyUser.setPassword("123");
            accountDao.createIfNotExists(verifyUser);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            accountDao = null;
        }
    }
}
