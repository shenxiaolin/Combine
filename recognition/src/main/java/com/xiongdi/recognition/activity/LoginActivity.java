package com.xiongdi.recognition.activity;

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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.xiongdi.recognition.R;
import com.xiongdi.recognition.bean.Account;
import com.xiongdi.recognition.db.AccountDao;
import com.xiongdi.recognition.db.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

public class LoginActivity extends Activity implements OnClickListener {
    private Button loginBT;
    private EditText nameTX, passwordTX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        initView();
        saveOrReadAccount(false);
        setListener();
    }

    private void initView() {
        nameTX = (EditText) this.findViewById(R.id.editText_name);
        passwordTX = (EditText) this.findViewById(R.id.editText_psw);
        loginBT = (Button) this.findViewById(R.id.login_bt);
    }


    private void setListener() {
        loginBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_bt:
                if (verifyAccount()) {
                    Intent intent = new Intent();
                    if ("userc".equals(nameTX.getText().toString())) {
                        intent.setClass(LoginActivity.this, FillInfoActivity.class);
                    } else if ("userv".equals(nameTX.getText().toString())) {
                        intent.setClass(LoginActivity.this, VerifyResultActivity.class);
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

    /**
     * 保存或读取账户信息
     */
    private void saveOrReadAccount(boolean isSave) {
        SharedPreferences sp = getSharedPreferences("account", Activity.MODE_PRIVATE);
        if (isSave) {
            Editor editor = sp.edit();
            editor.putString("userName", nameTX.getText().toString());
            editor.commit();
        } else {
            String userName = sp.getString("userName", null);
            if (userName != null) {
                nameTX.setText(userName);
            }
        }

    }

    /**
     * 验证账户
     */
    private boolean verifyAccount() {
        String userName = nameTX.getText().toString();
        String password = passwordTX.getText().toString();

        if (0 == userName.length() || 0 == password.length()) {
            return false;
        }

        try {
            AccountDao accountDao = new AccountDao(getApplicationContext());
            QueryBuilder<Account, Integer> queryBuilder = accountDao.getQueryBuilder();
            Where<Account, Integer> where = queryBuilder.where();
            where.eq("name", userName);
            PreparedQuery<Account> preparedQuery = where.prepare();
            List<Account> actList = accountDao.query(preparedQuery);

            if (0 == actList.size()) {
                return false;
            }
            String correctPassword = actList.get(0).getPassword();

            if (correctPassword.equals(password)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
