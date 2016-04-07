package com.xiongdi.recognition.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiongdi.recognition.R;
import com.xiongdi.recognition.bean.Person;
import com.xiongdi.recognition.db.PersonDao;
import com.xiongdi.recognition.helper.M1CardHelper;
import com.xiongdi.recognition.util.StringUtil;
import com.xiongdi.recognition.util.ToastUtil;
import com.xiongdi.recognition.widget.ProgressDialogFragment;

/**
 * Created by moubiao on 2016/3/25.
 * 验证身份信息界面
 */
public class VerifyResultActivity extends AppCompatActivity implements View.OnClickListener {
    private final int VERIFY_ACTIVITY = 0;

    private ImageView pictureIMG;
    private TextView personIDTV, personNameTV, personGenderTV, personBirthdayTV, personAddressTV;
    private Button backTB, readCardBT, verifyBT;

    private M1CardHelper m1CardHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_result_ayout);

        initData();
        initView();
        setListener();
    }

    private void initData() {
        m1CardHelper = new M1CardHelper(getApplicationContext());
        m1CardHelper.setRFModule();
    }

    private void initView() {
        ((TextView) findViewById(R.id.verify_ID).findViewById(R.id.verify_title_tv)).setText(R.string.info_title_ID);
        ((TextView) findViewById(R.id.verify_name).findViewById(R.id.verify_title_tv)).setText(R.string.info_item_title_name);
        ((TextView) findViewById(R.id.verify_gender).findViewById(R.id.verify_title_tv)).setText(R.string.info_item_title_gender);
        ((TextView) findViewById(R.id.verify_birthday).findViewById(R.id.verify_title_tv)).setText(R.string.info_item_title_birthday);
        ((TextView) findViewById(R.id.verify_address).findViewById(R.id.verify_title_tv)).setText(R.string.info_item_title_address);

        pictureIMG = (ImageView) findViewById(R.id.verify_img);
        personIDTV = (TextView) findViewById(R.id.verify_ID).findViewById(R.id.verify_content_tv);
        personNameTV = (TextView) findViewById(R.id.verify_name).findViewById(R.id.verify_content_tv);
        personGenderTV = (TextView) findViewById(R.id.verify_gender).findViewById(R.id.verify_content_tv);
        personBirthdayTV = (TextView) findViewById(R.id.verify_birthday).findViewById(R.id.verify_content_tv);
        personAddressTV = (TextView) findViewById(R.id.verify_address).findViewById(R.id.verify_content_tv);

        backTB = (Button) findViewById(R.id.bottom_left_bt);
        verifyBT = (Button) findViewById(R.id.bottom_right_bt);
        verifyBT.setText(R.string.verify);
        readCardBT = (Button) findViewById(R.id.bottom_middle_bt);
        readCardBT.setText(R.string.read_card);
    }

    private void setListener() {
        backTB.setOnClickListener(this);
        verifyBT.setOnClickListener(this);
        readCardBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_left_bt:
                finish();
                break;
            case R.id.bottom_right_bt:
                Intent intent = new Intent(VerifyResultActivity.this, VerifyActivity.class);
                startActivityForResult(intent, VERIFY_ACTIVITY);
                break;
            case R.id.bottom_middle_bt:
                new ReadTask().execute();
                break;
            default:
                break;
        }
    }

    private class ReadTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialogFragment progressDialog = new ProgressDialogFragment(getString(R.string.reading_from_card));

        @Override
        protected void onPreExecute() {
            progressDialog.show(getSupportFragmentManager(), "save");
            m1CardHelper.openRFSignal();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return m1CardHelper.readM1Card();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            m1CardHelper.closeRFSignal();

            if (success) {
                String[] cardData = m1CardHelper.getBaseData();
                personIDTV.setText(String.valueOf(cardData[0]));
                personNameTV.setText(cardData[1]);
                personGenderTV.setText(cardData[2]);
                personBirthdayTV.setText(cardData[3]);
                personAddressTV.setText(cardData[4]);
                Bitmap bitmap = m1CardHelper.getPicture();
                if (bitmap != null) {
                    pictureIMG.setImageBitmap(bitmap);
                } else {
                    pictureIMG.setImageResource(R.mipmap.person_photo);
                }
                if(!StringUtil.hasLength(cardData[1])){
                    ToastUtil.getInstance().showToast(getApplicationContext(), getString(R.string.common_no_data));
                }
            } else {
                ToastUtil.getInstance().showToast(getApplicationContext(), getString(R.string.read_failed_message));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case VERIFY_ACTIVITY:
                    PersonDao personDao = new PersonDao(getApplicationContext());
                    Long recordCount = personDao.getQuantity();
                    Person person = personDao.queryById(Integer.parseInt(String.valueOf(recordCount)));
                    if (person != null) {
                        personIDTV.setText(String.valueOf(person.getPersonID()));
                        personNameTV.setText(person.getName());
                        personGenderTV.setText(person.getGender());
                        personBirthdayTV.setText(person.getBirthday());
                        personAddressTV.setText(person.getAddress());
                        Bitmap bitmap = BitmapFactory.decodeFile(person.getGatherPictureUrl());
                        if (bitmap != null) {
                            pictureIMG.setImageBitmap(bitmap);
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            refreshView();
        }
    }

    private void refreshView() {
        personIDTV.setText("");
        personNameTV.setText("");
        personGenderTV.setText("");
        personBirthdayTV.setText("");
        personAddressTV.setText("");
        pictureIMG.setImageResource(R.mipmap.person_photo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        m1CardHelper.closeRFModule();
    }
}
