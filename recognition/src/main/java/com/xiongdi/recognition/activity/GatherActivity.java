package com.xiongdi.recognition.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.jy.demo.fingerprint.CallDecoder;
import com.example.jy.demo.fingerprint.CallFprint;
import com.xiongdi.recognition.R;
import com.xiongdi.recognition.adapter.GatherInfoVpAdapter;
import com.xiongdi.recognition.fragment.LeftHandFragment;
import com.xiongdi.recognition.fragment.PictureFragment;
import com.xiongdi.recognition.fragment.RightHandFragment;
import com.xiongdi.recognition.media.CropOption;
import com.xiongdi.recognition.media.CropOptionAdapter;
import com.xiongdi.recognition.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by moubiao on 2016/3/22.
 * 采集指纹和头像的activity
 */
public class GatherActivity extends AppCompatActivity implements View.OnClickListener {
    public final static int PICTURE_ACTIVITY = 0;//采集照片
    public final static int FINGERPRINT_ACTIVITY = 1;//采集指纹
    private static final int CROP_FROM_CAMERA = 2;//裁剪照片

    public static String fingerPrint_pic_path = "";

    private TabLayout gatherTab;
    private ViewPager gatherVP;
    private Button backBT, takePictureBT, saveBT;
    private GatherInfoVpAdapter gatherAdapter;
    private List<Fragment> gatherData;

    private String gatherID;
    private String pictureUrl;
    Uri mImageCaptureUri;
    FileUtil fileUtil;

    PictureFragment pictureFg;

    private boolean haveInformation = false;//判断是否有有效信息

    public void setFingerNUM(int fingerNUM) {
        this.fingerNUM = fingerNUM;
    }

    private int fingerNUM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gather_info_layout);

        iniData();
        initView();
        setListener();
    }

    private void initView() {
        backBT = (Button) findViewById(R.id.bottom_left_bt);
        takePictureBT = (Button) findViewById(R.id.bottom_middle_bt);
        saveBT = (Button) findViewById(R.id.bottom_right_bt);

        gatherTab = (TabLayout) findViewById(R.id.gather_tab);
        gatherVP = (ViewPager) findViewById(R.id.gather_viewpager);
        gatherVP.setAdapter(gatherAdapter);
        gatherVP.setOffscreenPageLimit(2);

        gatherTab.setupWithViewPager(gatherVP);
    }

    private void iniData() {
        LeftHandFragment leftFg = new LeftHandFragment();
        RightHandFragment rightFg = new RightHandFragment();
        pictureFg = new PictureFragment();
        gatherData = new ArrayList<>();
        gatherData.add(leftFg);
        gatherData.add(rightFg);
        gatherData.add(pictureFg);
        List<String> titleVp = new ArrayList<>();
        titleVp.add(getString(R.string.tab_indicator_title_Left));
        titleVp.add(getString(R.string.tab_indicator_title_right));
        titleVp.add(getString(R.string.tab_indicator_title_picture));

        gatherAdapter = new GatherInfoVpAdapter(getSupportFragmentManager(), gatherData, titleVp);

        Intent data = getIntent();
        gatherID = data.getStringExtra("gatherID");

        fileUtil = new FileUtil();
    }

    private void setListener() {
        backBT.setOnClickListener(this);
        takePictureBT.setOnClickListener(this);
        saveBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_left_bt:
                deleteTemporaryFile();
                finish();
                break;
            case R.id.bottom_middle_bt:
                Intent intent = new Intent();
                intent.setClass(GatherActivity.this, GatherPictureActivity.class);
                intent.putExtra("pictureName", gatherID);
                startActivityForResult(intent, PICTURE_ACTIVITY);
                break;
            case R.id.bottom_right_bt:
                if (haveInformation) {
                    saveFingerprint();
                    Intent data = new Intent();
                    if (pictureUrl != null) {
                        data.putExtra("pictureUrl", pictureUrl);
                    }
                    setResult(Activity.RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(GatherActivity.this, getString(R.string.no_fingerprint), Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    public String getGatherID() {
        return gatherID;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICTURE_ACTIVITY:
                    pictureUrl = data.getStringExtra("pictureUrl");
                    doCrop();
                    break;
                case FINGERPRINT_ACTIVITY:
                    haveInformation = true;
                    break;
                case CROP_FROM_CAMERA:
                    pictureFg.setPicture(pictureUrl);
                    break;
                default:
                    break;
            }
        }
    }

    private void saveFingerprint() {
        String picPath = getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM;

        CallDecoder.Bmp2Pgm(picPath + ".bmp", picPath + ".pgm");//将bmp转换为pgm
        CallFprint.pgmChangeToXyt(picPath + ".pgm", picPath + ".xyt");//将pgm转换成xyt

        //将xyt文件由现在的目录复制到Fingerprint目录下面
        String oldPicPath = getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM + ".xyt";
        String newFilepath = getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + "Fingerprint";
        fileUtil.isDirExist(newFilepath);
        String destFilepath = getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + "Fingerprint" + "/" + "vote" + fingerNUM + ".xyt";
        try {
            fileUtil.copyFile(oldPicPath, destFilepath);
            SharedPreferences.Editor editor = getSharedPreferences("fingerprintPath", Context.MODE_PRIVATE).edit();
            editor.putString("fingerprintPath", destFilepath);
            editor.apply();
            fingerPrint_pic_path = destFilepath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //删除中间文件
        fileUtil.deleteFile(getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM + ".bmp");
        fileUtil.deleteFile(getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM + ".pgm");
        fileUtil.deleteFile(getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM + ".xyt");
    }

    /**
     * 裁剪照片
     */
    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            mImageCaptureUri = Uri.fromFile(new File(pictureUrl));
            intent.setData(mImageCaptureUri);
//			intent.putExtra("outputX", 320);
//			intent.putExtra("outputY", 480);
            intent.putExtra("crop", "true");
//			intent.putExtra("aspectX", 1);
//			intent.putExtra("aspectY", 1);
//			intent.putExtra("scale", true);
//			intent.putExtra("return-data", true);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            intent.putExtra("output", Uri.fromFile(new File(pictureUrl)));

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = getPackageManager().getApplicationLabel(
                            res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(
                            res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(
                                cropOptions.get(item).appIntent,
                                CROP_FROM_CAMERA);
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mImageCaptureUri != null) {
                            getContentResolver().delete(mImageCaptureUri, null, null);
                            mImageCaptureUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        deleteTemporaryFile();
    }

    /**
     * 只能删除指纹的中间文件不能删除照片的，如果删除了照片的会影响裁剪
     */
    private void deleteTemporaryFile() {
        fileUtil.deleteFile(getExternalFilesDir(null) + "/" + getResources().getString(R.string.app_name) + "/"
                + gatherID + "/" + gatherID + "_" + fingerNUM + ".bmp");
    }
}
