package com.xiongdi.recognition.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.dao.Dao;
import com.xiongdi.recognition.bean.Account;
import com.xiongdi.recognition.db.AccountDao;
import com.xiongdi.recognition.db.DatabaseHelper;

import java.sql.SQLException;

/**
 * Created by moubiao on 2016/3/22.
 * 自定义的application
 */
public class RecognitionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initDatabase();
    }

    private void initDatabase() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        if (!sp.getBoolean("addAccCompleted", false)) {
            AccountDao accountDao = new AccountDao(this);
            Account collectionUser = new Account();
            //采集指纹的账号
            collectionUser.setName("userc");
            collectionUser.setPassword("123");
            accountDao.add(collectionUser);
            //验证指纹的账号
            Account verifyUser = new Account();
            verifyUser.setName("userv");
            verifyUser.setPassword("123");
            accountDao.add(verifyUser);
        }
    }

}
