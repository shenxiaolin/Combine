package com.example.psamrftest.util;

import android.util.Log;

import com.xiongdi.EmpPad;

/**
 * Created by moubiao on 2016/5/13.
 * <p/>
 * 操作PRAM卡的工具类
 */
public class PSAMUtil {
    private static final String TAG = "moubiao";

    /**
     * 打开PSAM模块
     */
    public static boolean openPSAMModule() {
        if (0 == EmpPad.OpenSimMoudle()) {
            return true;
        } else {
            Log.e(TAG, "open Module: failed!");
            return false;
        }
    }

    /**
     * 关闭PSAM模块
     */
    public static boolean closePSAMModule() {
        if (0 == EmpPad.CloseSimModule()) {
            return true;
        } else {
            Log.e(TAG, "close Module: failed!");
            return false;
        }
    }

    /**
     * 复位PSAM卡
     */
    public static boolean resetPSAM(byte CardSelect, int uiRate, byte ucVoltage, byte[] rLen, byte[] ATR, byte mode) {
        if (0 == EmpPad.IccSimReset(CardSelect, uiRate, ucVoltage, rLen, ATR, mode)) {
            return true;
        } else {
            Log.e(TAG, "reset PSAM: failed!");
            return false;
        }
    }

    public static boolean sendAPDU(byte Slot, byte[] buffer, short length, byte[] rbuffer, short[] Revlen, short[] SW) {
        long startTime = System.currentTimeMillis();
        if (0 == EmpPad.Sim_Apdu(Slot, buffer, length, rbuffer, Revlen, SW)) {
            Log.d(TAG, "sendAPDU: success spend time = " + (System.currentTimeMillis() - startTime));
            return true;
        } else {
            Log.d(TAG, "sendAPDU: failed spend time = " + (System.currentTimeMillis() - startTime));
            Log.e(TAG, "send APDU: failed!");
            return false;
        }
    }
}
