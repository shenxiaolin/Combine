package com.example.psamrftest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.psamrftest.R;
import com.example.psamrftest.interfaces.SetPSAMParamsInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by moubiao on 2016/5/13.
 * <p>
 * 单选对话框
 */
public class PSAMSetParamsDialog extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static String TAG = "moubiao";
    private Spinner baudRateSp, voltageSp, resetSp;
    private Button sureBT, cancelBT;

    private int slotIndex;
    private List<String> params;
    private SetPSAMParamsInterface listener;
    private String dialogTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pasm_choose_layout, container, false);
        initView(view);
        setInnerListener();

        return view;
    }

    private void initData() {
        params = new ArrayList<>();
        params.add(0, getString(R.string.PSAM_default_baud_rate));
        params.add(1, String.valueOf(1));
        params.add(2, String.valueOf(0));

        Bundle data = getArguments();
        if (data != null) {
            slotIndex = data.getInt("slotIndex", 1);
            if (1 == slotIndex) {
                dialogTitle = getString(R.string.PSAM_serial_number_first);
            } else if (2 == slotIndex) {
                dialogTitle = getString(R.string.PSAM_serial_number_second);
            } else if (3 == slotIndex) {
                dialogTitle = getString(R.string.PSAM_serial_number_third);
            } else if (4 == slotIndex) {
                dialogTitle = getString(R.string.PSAM_serial_number_forth);
            }
        } else {
            dialogTitle = getString(R.string.PSAM_serial_number_first);
        }
    }

    private void initView(View view) {
        ((TextView) view.findViewById(R.id.dialog_title_tv)).setText(dialogTitle);
        baudRateSp = (Spinner) view.findViewById(R.id.baud_Rate_spinner);
        voltageSp = (Spinner) view.findViewById(R.id.voltage_spinner);
        resetSp = (Spinner) view.findViewById(R.id.reset_model_spinner);

        sureBT = (Button) view.findViewById(R.id.choose_sure_bt);
        cancelBT = (Button) view.findViewById(R.id.choose_cancel_bt);
    }

    private void setInnerListener() {
        baudRateSp.setOnItemSelectedListener(this);
        voltageSp.setOnItemSelectedListener(this);
        resetSp.setOnItemSelectedListener(this);

        sureBT.setOnClickListener(this);
        cancelBT.setOnClickListener(this);
    }

    public void setListener(SetPSAMParamsInterface listener) {
        this.listener = listener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] options;
        switch (parent.getId()) {
            case R.id.baud_Rate_spinner:
                options = getResources().getStringArray(R.array.baudRate);
                params.add(0, options[position]);
                break;
            case R.id.voltage_spinner:
                params.add(1, String.valueOf(position + 1));
                break;
            case R.id.reset_model_spinner:
                params.add(2, String.valueOf(position));
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_sure_bt:
                listener.OnSureClick(slotIndex, params);
                break;
            case R.id.choose_cancel_bt:
                listener.OnCancelClick();
                break;
            default:

                break;
        }
        dismiss();
    }
}
