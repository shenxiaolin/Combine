package com.xiongdi.recognition.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by moubiao on 2016/3/22.
 * 单选对话框
 */
@SuppressLint("ValidFragment")
public class SingleChoiceDialogFragment extends DialogFragment {
    private String dialogTitle;
    private String[] dialogItems;
    private int selectedID;
    DialogInterface.OnClickListener clickListener;

    @SuppressLint("ValidFragment")
    public SingleChoiceDialogFragment(String dialogTitle, String[] dialogItems, int selectID) {
        this.dialogTitle = dialogTitle;
        this.dialogItems = dialogItems;
        this.selectedID = selectID;
    }

    public void setListener(DialogInterface.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitle)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setSingleChoiceItems(dialogItems, selectedID, clickListener)
                .setNegativeButton("Done", null)
                .create();

        return dialog;
    }
}
