package com.example.jy.demo.passport;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jy.demo.bean.Account;
import com.example.jy.demo.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * author：moubiao 2016-03-30
 * 登录界面
 */
public class login extends Activity implements OnClickListener {
    private Button mButton_help, mButton_enter;
    private EditText mEditText_name, mEditText_psw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initView();
        saveOrReadAccount(false);
        setListener();
    }

    private void initView() {
        mButton_help = (Button) this.findViewById(R.id.button_regist);
        mButton_enter = (Button) this.findViewById(R.id.login_bt);
        mEditText_name = (EditText) this.findViewById(R.id.editText_name);
        mEditText_psw = (EditText) this.findViewById(R.id.editText_psw);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void setListener() {
        mButton_enter.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_bt:
                if (verifyAccount()) {
                    Intent intent = new Intent();
                    if ("userc".equals(mEditText_name.getText().toString())) {
                        intent.setClass(login.this, gatherMain.class);
                    } else if ("userv".equals(mEditText_name.getText().toString())) {
                        intent.setClass(login.this, MatchResultActivity.class);
                        intent.putExtra("haveData", false);
                    }

                    saveOrReadAccount(true);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "用户名或密码不正确", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();
                }

            default:
                break;


        }
    }

    private void saveOrReadAccount(boolean isSave) {
        SharedPreferences sp = getSharedPreferences("account", Activity.MODE_PRIVATE);
        if (isSave) {
            Editor editor = sp.edit();
            editor.putString("userName", mEditText_name.getText().toString());
            editor.commit();
        } else {
            String userName = sp.getString("userName", null);
            if (userName != null) {
                mEditText_name.setText(userName);
            }
        }

    }

    private boolean verifyAccount() {
        String userName = mEditText_name.getText().toString();
        String password = mEditText_psw.getText().toString();

        if (0 == userName.length() || 0 == password.length()) {
            return false;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        try {
            Dao accountDao = databaseHelper.getDao(Account.class);
            Account account = (Account) accountDao.queryForId(userName);
            if (null == account) {
                return false;
            }
            String correctPassword = account.getPassword();

            if (correctPassword.equals(password)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
