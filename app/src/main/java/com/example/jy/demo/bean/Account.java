package com.example.jy.demo.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/18.
 */
@DatabaseTable(tableName = "account")
public class Account implements Serializable {
    @DatabaseField(id = true)
    private String user;
    @DatabaseField
    private String password;

    public Account() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
