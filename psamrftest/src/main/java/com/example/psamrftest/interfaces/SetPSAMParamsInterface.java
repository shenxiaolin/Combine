package com.example.psamrftest.interfaces;

import java.util.List;

/**
 * Created by moubiao on 2016/5/13.
 */
public interface SetPSAMParamsInterface {
    /**
     * 按确定按钮的回调接口
     */
    void OnSureClick(int slotIndex, List<String> params);

    /**
     * 按取消按钮的回调接口
     */
    void OnCancelClick();
}
