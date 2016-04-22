package com.xiongdi.recognition.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import com.xd.rfid;
import com.xiongdi.recognition.util.FileUtil;
import com.yzq.OpenJpeg;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by moubiao on 2016/4/7.
 * 操作M1卡的帮助类
 */
public class M1CardHelper {
    private static String TAG = "moubiao";
    private static int BLOCK_LENGTH = 16;

    private Context mContext;
    private String IDCard;
    private String nameCard;
    private String genderCard;
    private String birthdayCard;
    private String addressCard;
    private String IDNOCard;
    private String imgUrlCard;
    private String fingerUrlCard;
    private Bitmap cardImg;

    //Mode：0 表示 ISO14443-A；1 表示 ISO14443-B；2 表示 Felica C；
    private static int RFID_PROTOCOL_A = 0;

    public M1CardHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void setSaveData(String[] saveData) {
        IDCard = saveData[0];
        nameCard = saveData[1];
        genderCard = saveData[2];
        birthdayCard = saveData[3];
        addressCard = saveData[4];
        IDNOCard = saveData[5];
        imgUrlCard = saveData[6];
        fingerUrlCard = saveData[7];
    }

    public String[] getBaseData() {
        return new String[]{
                IDCard,
                nameCard,
                genderCard,
                birthdayCard,
                addressCard,
                IDNOCard,
                fingerUrlCard
        };
    }

    public Bitmap getPicture() {
        return cardImg;
    }

    /**
     * 设置射频模块
     */
    public void setRFModule() {
        //打开射频模块
        if (rfid.RFIDModuleOpen() != 0) {
            Log.d(TAG, "RFID module open failed!");
            return;
        }

        //初始化射频模块
        if (rfid.RFIDInit() != 0) {
            Log.d(TAG, "RFID module initialize failed!");
        }
    }

    public void closeRFModule() {
        if (rfid.RFIDMoudleClose() != 0) {
            Log.d(TAG, "RFID module close failed!");
            closeRFModule();
        }
    }

    /**
     * 打开射频信号
     */
    public void openRFSignal() {
        int openRFSignalResult = rfid.RFIDRfOpen();
        if (openRFSignalResult != 0) {
            Log.d(TAG, "RFID signal open failed!");
            openRFSignal();
        }
    }

    /**
     * 关闭射频信号
     */
    public void closeRFSignal() {
        int closeRFSignalResult = rfid.RFIDRfClose();
        if (closeRFSignalResult != 0) {
            closeRFSignal();
        }
    }

    /**
     * 设置射频协议
     *
     * @return true:设置成功；false：设置失败
     */
    private boolean setRFIDProtocol() {
        if (rfid.RFIDTypeSet(RFID_PROTOCOL_A) != 0) {
            Log.d(TAG, "RFID protocol set failed!");
            return false;
        }

        return true;
    }

    /**
     * 认证扇区
     *
     * @param sectorNo 认证的扇区
     * @param serialNo 卡的序列号
     * @return true：认证成功；false：认证失败
     */
    private boolean authenticateCard(int sectorNo, byte[] serialNo) {
        byte[] key = new byte[6];
        Arrays.fill(key, (byte) 0xFF);
        byte[] ID = new byte[4];
        ID[0] = serialNo[0];
        ID[1] = serialNo[1];
        ID[2] = serialNo[2];
        ID[3] = serialNo[3];
        int verifyRet = rfid.MifAuthen((byte) 0x0A, (byte) sectorNo, key, ID);
        if (verifyRet != 0) {
            Log.d(TAG, "authenticateCard sector " + sectorNo + " failed!");
            return false;
        }

        return true;
    }

    /**
     * 读M1卡
     */
    public boolean readM1Card() {
        //设置射频协议
        if (!setRFIDProtocol()) {
            return false;
        }

        //获取序列号
        byte[] serialLength = new byte[1];
        byte[] serialNo = new byte[64];
        int serialRet = rfid.RFIDGetSNR(0, serialLength, serialNo);
        if (serialRet != 0) {
            Log.d(TAG, "serial number read failed!");

            return false;
        }

        return readBaseData(serialNo) && readPicture(serialNo) && readFingerprint(serialNo);
    }

    /**
     * 读取基本数据
     */
    private boolean readBaseData(byte[] serialNo) {
        byte[] readBaseData = new byte[112];
        Arrays.fill(readBaseData, (byte) 0x00);

        int readBaseOffset = 0;
        //验证扇区0
        if (!authenticateCard(0, serialNo)) {
            return false;
        }

        for (int i = 1; i < 3; i++) {
            if (!readBlock(readBaseData, readBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readBaseOffset++;
        }

        //验证扇区1
        if (!authenticateCard(1, serialNo)) {
            return false;
        }

        for (int i = 4; i < 7; i++) {
            if (!readBlock(readBaseData, readBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readBaseOffset++;
        }

        //验证扇区2
        if (!authenticateCard(2, serialNo)) {
            return false;
        }
        for (int i = 8; i < 10; i++) {
            if (!readBlock(readBaseData, readBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readBaseOffset++;
        }

        byte[] IDDataRead = new byte[5];
        byte[] nameDataRead = new byte[16];
        byte[] genderDataRead = new byte[6];
        byte[] birthdayDataRead = new byte[10];
        byte[] addressDataRead = new byte[60];
        byte[] IDNODataRead = new byte[5];
        System.arraycopy(readBaseData, 0, IDDataRead, 0, 5);//ID
        System.arraycopy(readBaseData, 5, nameDataRead, 0, 16);//name
        System.arraycopy(readBaseData, 21, genderDataRead, 0, 6);//gender
        System.arraycopy(readBaseData, 27, birthdayDataRead, 0, 10);//birthday
        System.arraycopy(readBaseData, 37, addressDataRead, 0, 60);//address
        System.arraycopy(readBaseData, 97, IDNODataRead, 0, 5);//IDNO

        byte[] finNameData = null;
        for (int i = (nameDataRead.length - 1); i >= 0; i--) {
            if (nameDataRead[i] != 0) {
                finNameData = new byte[i + 1];
                System.arraycopy(nameDataRead, 0, finNameData, 0, i + 1);
                break;
            }

        }

        byte[] finGenderData = null;
        for (int i = (genderDataRead.length - 1); i >= 0; i--) {
            if (genderDataRead[i] != 0) {
                finGenderData = new byte[i + 1];
                System.arraycopy(genderDataRead, 0, finGenderData, 0, i + 1);
                break;
            }

        }

        byte[] finAddressData = null;
        for (int i = (addressDataRead.length - 1); i >= 0; i--) {
            if (addressDataRead[i] != 0) {
                finAddressData = new byte[i + 1];
                System.arraycopy(addressDataRead, 0, finAddressData, 0, i + 1);
                break;
            }
        }

        IDCard = new String(IDDataRead);
        if (finNameData != null) {
            nameCard = new String(finNameData);
        } else {
            nameCard = "";
        }
        if (finGenderData != null) {
            genderCard = new String(finGenderData);
        } else {
            genderCard = "";
        }
        birthdayCard = new String(birthdayDataRead);
        if (finAddressData != null) {
            addressCard = new String(finAddressData);
        } else {
            addressCard = "";
        }
        IDNOCard = new String(IDNODataRead);

        Log.d(TAG, "read data success!");

        return true;
    }


    /**
     * 读取卡里的照片
     */
    private boolean readPicture(byte[] serialNo) {
        //先检查外部存储是否可用，如果不可用则不保存
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d(TAG, "storage mounted failed, save fingerprint failed!");
            return false;
        }

        byte[] picData = new byte[960];
        Arrays.fill(picData, (byte) 0x00);

        int readOffset = 0;
        //验证扇区36
        if (!authenticateCard(48, serialNo)) {
            return false;
        }

        for (int i = 192; i < 207; i++) {
            if (!readBlock(picData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区37
        if (!authenticateCard(52, serialNo)) {
            return false;
        }

        for (int i = 208; i < 223; i++) {
            if (!readBlock(picData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区38
        if (!authenticateCard(56, serialNo)) {
            return false;
        }

        for (int i = 224; i < 239; i++) {
            if (!readBlock(picData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区39
        if (!authenticateCard(60, serialNo)) {
            return false;
        }

        for (int i = 240; i < 255; i++) {
            if (!readBlock(picData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        byte[] realPicData = null;
        for (int i = (picData.length - 1); i >= 0; i--) {
            if (picData[i] != 0) {
                realPicData = new byte[i + 1];
                System.arraycopy(picData, 0, realPicData, 0, i + 1);
                break;
            }
        }

        OpenJpeg opj2k = new OpenJpeg();
        opj2k.GetLibVersion();
        FileOutputStream fos = null;
        try {
            File directory = mContext.getExternalFilesDir("card");
            if (directory != null && !directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "readPicture: create card directory failed");
                    return false;
                }
            }
            if (directory != null) {
                String filePath = directory.getPath() + "/decodePic.jp2";
                String decompressPath = directory.getPath() + "/decodePic.png";
                fos = new FileOutputStream(filePath);
                if (realPicData != null) {
                    fos.write(realPicData);
                    fos.flush();
                    if(0 == opj2k.DecompressImage(filePath, decompressPath)){
                        cardImg = BitmapFactory.decodeFile(decompressPath);
                        new FileUtil().deleteFile(filePath);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * 读指纹
     */
    private boolean readFingerprint(byte[] serialNo) {
        //先检查外部存储是否可用，如果不可用则不保存
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d(TAG, "storage mounted failed, save fingerprint failed!");
            return false;
        }

        byte[] readFingerData = new byte[1392];
        Arrays.fill(readFingerData, (byte) 0x00);

        int readOffset = 0;
        //验证扇区3(12-15)
        if (!authenticateCard(3, serialNo)) {
            return false;
        }
        for (int i = 12; i < 15; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区4(16-19)
        if (!authenticateCard(4, serialNo)) {
            return false;
        }

        for (int i = 16; i < 19; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区5(20-23)
        if (!authenticateCard(5, serialNo)) {
            return false;
        }

        for (int i = 20; i < 23; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区6(24-27)
        if (!authenticateCard(6, serialNo)) {
            return false;
        }

        for (int i = 24; i < 27; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区7(28-31)
        if (!authenticateCard(7, serialNo)) {
            return false;
        }
        for (int i = 28; i < 31; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区8(32-35)
        if (!authenticateCard(8, serialNo)) {
            return false;
        }

        for (int i = 32; i < 35; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区9(36-39)
        if (!authenticateCard(9, serialNo)) {
            return false;
        }

        for (int i = 36; i < 39; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区10(40-43)
        if (!authenticateCard(10, serialNo)) {
            return false;
        }

        for (int i = 40; i < 43; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区11(44-47)
        if (!authenticateCard(11, serialNo)) {
            return false;
        }
        for (int i = 44; i < 47; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区12(48-51)
        if (!authenticateCard(12, serialNo)) {
            return false;
        }

        for (int i = 48; i < 51; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区13(52-55)
        if (!authenticateCard(13, serialNo)) {
            return false;
        }

        for (int i = 52; i < 55; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区14(56-59)
        if (!authenticateCard(14, serialNo)) {
            return false;
        }

        for (int i = 56; i < 59; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区15(60-63)
        if (!authenticateCard(15, serialNo)) {
            return false;
        }
        for (int i = 60; i < 63; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区16(64-67)
        if (!authenticateCard(16, serialNo)) {
            return false;
        }

        for (int i = 64; i < 67; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区17(68-71)
        if (!authenticateCard(17, serialNo)) {
            return false;
        }

        for (int i = 68; i < 71; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区18(72-75)
        if (!authenticateCard(18, serialNo)) {
            return false;
        }

        for (int i = 72; i < 75; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区19(76-79)
        if (!authenticateCard(19, serialNo)) {
            return false;
        }
        for (int i = 76; i < 79; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区20(80-83)
        if (!authenticateCard(20, serialNo)) {
            return false;
        }

        for (int i = 80; i < 83; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区21(84-87)
        if (!authenticateCard(21, serialNo)) {
            return false;
        }

        for (int i = 84; i < 87; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区22(88-91)
        if (!authenticateCard(22, serialNo)) {
            return false;
        }

        for (int i = 88; i < 91; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区23(92-95)
        if (!authenticateCard(23, serialNo)) {
            return false;
        }
        for (int i = 92; i < 95; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区24(96-99)
        if (!authenticateCard(24, serialNo)) {
            return false;
        }

        for (int i = 96; i < 99; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区25(100-103)
        if (!authenticateCard(25, serialNo)) {
            return false;
        }

        for (int i = 100; i < 103; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区26(104-107)
        if (!authenticateCard(26, serialNo)) {
            return false;
        }

        for (int i = 104; i < 107; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区27(108-111)
        if (!authenticateCard(27, serialNo)) {
            return false;
        }
        for (int i = 108; i < 111; i++) {
            if (!readBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            readOffset++;
        }

        //验证扇区28(112-115)
//        if (!authenticateCard(28, serialNo)) {
//            return;
//        }
//
//        for (int i = 112; i < 115; i++) {
//            if (!writeBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            readOffset++;
//        }
//
//        //验证扇区29(116-119)
//        if (!authenticateCard(29, serialNo)) {
//            return;
//        }
//
//        for (int i = 116; i < 119; i++) {
//            if (!writeBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            readOffset++;
//        }
//
//        //验证扇区30(120-123)
//        if (!authenticateCard(30, serialNo)) {
//            return;
//        }
//
//        for (int i = 120; i < 123; i++) {
//            if (!writeBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            readOffset++;
//        }
//
//        //验证扇区31(124-127)
//        if (!authenticateCard(31, serialNo)) {
//            return;
//        }
//        for (int i = 124; i < 127; i++) {
//            if (!writeBlock(readFingerData, readOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            readOffset++;
//        }

        byte[] validFingerData = null;
        for (int i = (readFingerData.length - 1); i >= 0; i--) {
            if (readFingerData[i] != 0) {
                validFingerData = new byte[i + 1];
                System.arraycopy(readFingerData, 0, validFingerData, 0, i + 1);
                break;
            }
        }

        File file = mContext.getExternalFilesDir("card");
        if (file != null && !file.exists()) {
            if (!file.mkdirs()) {
                Log.d(TAG, "create the directory of fingerprint failed!");
                return false;
            }
        }
        File fingerFile = new File(file, "cardFingerprint.xyt");
        if (!fingerFile.exists()) {
            try {
                if (!fingerFile.createNewFile()) {
                    Log.d(TAG, "create the file of fingerprint failed!");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedOutputStream stream = null;
        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(fingerFile);
            stream = new BufferedOutputStream(fstream);
            if (validFingerData != null) {
                stream.write(validFingerData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (null != fstream) {
                    fstream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            String[] scanFile = new String[]{
                    mContext.getExternalFilesDir("card") + File.separator + "cardFingerprint.xyt"
            };
            MediaScannerConnection.scanFile(mContext, scanFile, null, null);
        }

        return true;
    }

    /**
     * 读取块里的数据
     *
     * @param desData 存放数据的数组
     * @param offset  偏移量
     * @param block   块
     */
    private boolean readBlock(byte[] desData, int offset, int block) {
        byte[] readTemp = new byte[BLOCK_LENGTH];
        int readRet = rfid.MifRead(block, readTemp);
        if (readRet != 0) {
            Log.d(TAG, "read block " + block + " failed!");
            return false;
        }
        System.arraycopy(readTemp, 0, desData, offset, BLOCK_LENGTH);

        return true;
    }

    /**
     * 写M1卡
     */
    public boolean writeM1Card() {
        //设置射频协议
        if (!setRFIDProtocol()) {
            return false;
        }

        //获取序列号
        byte[] serialLength = new byte[1];
        byte[] serialNo = new byte[64];
        int serialRet = rfid.RFIDGetSNR(0, serialLength, serialNo);
        if (serialRet != 0) {
            Log.d(TAG, "serial number read failed!");
            return false;
        }

        //写数据
        if (!writeBaseData(serialNo)) {
            return false;
        }
        if (imgUrlCard != null) {
            if (!writePicture(serialNo)) {
                return false;
            }
        }
        if (fingerUrlCard != null) {
            if (!writeFingerprint(serialNo)) {
                return false;
            }
        }

        return true;
    }

    private boolean writeBaseData(byte[] serialNo) {
        byte[] writeBaseData = new byte[112];
        Arrays.fill(writeBaseData, (byte) 0x00);
        System.arraycopy(IDCard.getBytes(), 0, writeBaseData, 0, IDCard.getBytes().length);
        System.arraycopy(nameCard.getBytes(), 0, writeBaseData, 5, nameCard.getBytes().length);
        System.arraycopy(genderCard.getBytes(), 0, writeBaseData, 21, genderCard.getBytes().length);
        System.arraycopy(birthdayCard.getBytes(), 0, writeBaseData, 27, birthdayCard.getBytes().length);
        System.arraycopy(addressCard.getBytes(), 0, writeBaseData, 37, addressCard.getBytes().length);
        System.arraycopy(IDNOCard.getBytes(), 0, writeBaseData, 97, IDNOCard.getBytes().length);

        int writeBaseOffset = 0;
        //验证扇区0(0-3，只有1，2可以写数据)
        if (!authenticateCard(0, serialNo)) {
            return false;
        }

        for (int i = 1; i < 3; i++) {
            if (!writeBlock(writeBaseData, writeBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeBaseOffset++;
        }

        //验证扇区1
        if (!authenticateCard(1, serialNo)) {
            return false;
        }

        for (int i = 4; i < 7; i++) {
            if (!writeBlock(writeBaseData, writeBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeBaseOffset++;
        }

        //验证扇区2
        if (!authenticateCard(2, serialNo)) {
            return false;
        }
        for (int i = 8; i < 10; i++) {
            if (!writeBlock(writeBaseData, writeBaseOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeBaseOffset++;
        }

        Log.d(TAG, "write base data success!");
        return true;
    }

    /**
     * 写照片
     */
    private boolean writePicture(byte[] serialNo) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }

        File file = new File(imgUrlCard);
        if (!file.exists()) {
            return false;
        }

        int fileLen = (int) file.length();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(fileLen);
        FileInputStream fis;
        byte[] readData = new byte[fileLen];
        int readLen;
        try {
            fis = new FileInputStream(file);
            while ((readLen = fis.read(readData)) != -1) {
                bos.write(readData, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bitmapData = bos.toByteArray();
        int length = bitmapData.length;
        int needBlock = (int) Math.ceil(length / BLOCK_LENGTH);
        if (needBlock > 60) {
            Log.d(TAG, "picture too large!");
            return false;
        }

        byte[] writePicData = new byte[960];
        Arrays.fill(writePicData, (byte) 0x00);
        System.arraycopy(bitmapData, 0, writePicData, 0, bitmapData.length);

        int writeOffset = 0;
        //验证扇区36(192-206)
        if (!authenticateCard(48, serialNo)) {
            return false;
        }
        for (int i = 192; i < 207; i++) {
            if (!writeBlock(writePicData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区37(208-222)
        if (!authenticateCard(52, serialNo)) {
            return false;
        }

        for (int i = 208; i < 223; i++) {
            if (!writeBlock(writePicData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区38(224-238)
        if (!authenticateCard(56, serialNo)) {
            return false;
        }

        for (int i = 224; i < 239; i++) {
            if (!writeBlock(writePicData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区39(240-254)
        if (!authenticateCard(60, serialNo)) {
            return false;
        }

        for (int i = 240; i < 255; i++) {
            if (!writeBlock(writePicData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        Log.d(TAG, "write picture success!");
        return true;
    }

    /**
     * 写指纹
     */
    private boolean writeFingerprint(byte[] serialNo) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }

        File file = new File(fingerUrlCard);
        if (!file.exists()) {
            return false;
        }

        byte[] writeFingerData = new byte[1392];
        Arrays.fill(writeFingerData, (byte) 0x00);
        FileInputStream stream = null;
        ByteArrayOutputStream out = null;
        try {
            stream = new FileInputStream(file);
            out = new ByteArrayOutputStream(1392);
            byte[] temp = new byte[1392];
            int n;
            while ((n = stream.read(temp)) != -1) {
                out.write(temp, 0, n);
            }

            byte[] validData = out.toByteArray();
            if (validData.length > 1392) {
                Log.d(TAG, "finger data too large!");
                return false;
            }
            System.arraycopy(validData, 0, writeFingerData, 0, validData.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        int writeOffset = 0;
        //验证扇区3(12-15)
        if (!authenticateCard(3, serialNo)) {
            return false;
        }
        for (int i = 12; i < 15; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区4(16-19)
        if (!authenticateCard(4, serialNo)) {
            return false;
        }

        for (int i = 16; i < 19; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区5(20-23)
        if (!authenticateCard(5, serialNo)) {
            return false;
        }

        for (int i = 20; i < 23; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区6(24-27)
        if (!authenticateCard(6, serialNo)) {
            return false;
        }

        for (int i = 24; i < 27; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区7(28-31)
        if (!authenticateCard(7, serialNo)) {
            return false;
        }
        for (int i = 28; i < 31; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区8(32-35)
        if (!authenticateCard(8, serialNo)) {
            return false;
        }

        for (int i = 32; i < 35; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区9(36-39)
        if (!authenticateCard(9, serialNo)) {
            return false;
        }

        for (int i = 36; i < 39; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区10(40-43)
        if (!authenticateCard(10, serialNo)) {
            return false;
        }

        for (int i = 40; i < 43; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区11(44-47)
        if (!authenticateCard(11, serialNo)) {
            return false;
        }
        for (int i = 44; i < 47; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区12(48-51)
        if (!authenticateCard(12, serialNo)) {
            return false;
        }

        for (int i = 48; i < 51; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区13(52-55)
        if (!authenticateCard(13, serialNo)) {
            return false;
        }

        for (int i = 52; i < 55; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区14(56-59)
        if (!authenticateCard(14, serialNo)) {
            return false;
        }

        for (int i = 56; i < 59; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区15(60-63)
        if (!authenticateCard(15, serialNo)) {
            return false;
        }
        for (int i = 60; i < 63; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区16(64-67)
        if (!authenticateCard(BLOCK_LENGTH, serialNo)) {
            return false;
        }

        for (int i = 64; i < 67; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区17(68-71)
        if (!authenticateCard(17, serialNo)) {
            return false;
        }

        for (int i = 68; i < 71; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区18(72-75)
        if (!authenticateCard(18, serialNo)) {
            return false;
        }

        for (int i = 72; i < 75; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区19(76-79)
        if (!authenticateCard(19, serialNo)) {
            return false;
        }
        for (int i = 76; i < 79; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区20(80-83)
        if (!authenticateCard(20, serialNo)) {
            return false;
        }

        for (int i = 80; i < 83; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区21(84-87)
        if (!authenticateCard(21, serialNo)) {
            return false;
        }

        for (int i = 84; i < 87; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区22(88-91)
        if (!authenticateCard(22, serialNo)) {
            return false;
        }

        for (int i = 88; i < 91; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区23(92-95)
        if (!authenticateCard(23, serialNo)) {
            return false;
        }
        for (int i = 92; i < 95; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区24(96-99)
        if (!authenticateCard(24, serialNo)) {
            return false;
        }

        for (int i = 96; i < 99; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区25(100-103)
        if (!authenticateCard(25, serialNo)) {
            return false;
        }

        for (int i = 100; i < 103; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区26(104-107)
        if (!authenticateCard(26, serialNo)) {
            return false;
        }

        for (int i = 104; i < 107; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区27(108-111)
        if (!authenticateCard(27, serialNo)) {
            return false;
        }
        for (int i = 108; i < 111; i++) {
            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
                return false;
            }

            writeOffset++;
        }

        //验证扇区28(112-115)
//        if (!authenticateCard(28, serialNo)) {
//            return;
//        }
//
//        for (int i = 112; i < 115; i++) {
//            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            writeOffset++;
//        }
//
//        //验证扇区29(116-119)
//        if (!authenticateCard(29, serialNo)) {
//            return;
//        }
//
//        for (int i = 116; i < 119; i++) {
//            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            writeOffset++;
//        }
//
//        //验证扇区30(120-123)
//        if (!authenticateCard(30, serialNo)) {
//            return;
//        }
//
//        for (int i = 120; i < 123; i++) {
//            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            writeOffset++;
//        }
//
//        //验证扇区31(124-127)
//        if (!authenticateCard(31, serialNo)) {
//            return;
//        }
//        for (int i = 124; i < 127; i++) {
//            if (!writeBlock(writeFingerData, writeOffset * BLOCK_LENGTH, i)) {
//                return;
//            }
//
//            writeOffset++;
//        }

        Log.d(TAG, "write fingerprint success!");

        return true;
    }

    /**
     * 将数据写到块里去
     *
     * @param srcData   要写的数据
     * @param srcOffset 数据偏移量
     * @param block     要写的块
     */
    private boolean writeBlock(byte[] srcData, int srcOffset, int block) {
        byte[] writeTemp = new byte[BLOCK_LENGTH];
        System.arraycopy(srcData, srcOffset, writeTemp, 0, BLOCK_LENGTH);
        int writeRet = rfid.MifWrite(block, writeTemp);
        if (writeRet != 0) {
            Log.d(TAG, "write block " + block + " failed!");

            return false;
        }

        return true;
    }
}

