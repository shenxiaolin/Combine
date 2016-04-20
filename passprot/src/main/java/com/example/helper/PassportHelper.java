package com.example.helper;


import android.util.Log;

import com.openssl.DesLib;
import com.openssl.DigestEncoder;
import com.xd.rfid;

import java.util.Arrays;

public class PassportHelper {
    private static String TAG = "moubiao";

    private static int MAX_PATH = 1024 * 1024;
    private static byte MRZ_WEIGHT[] = {7, 3, 1};
    private static int MAXREADLENGTH = 118;//最大长度
    private static int EF_NULL = 0;
    private static int WS_EX_LAYERED = 0x00080000;
    private static int LWA_COLORKEY = 0x00000001;
    private static int LWA_ALPHA = 0x00000002;
    private static int LEN_OF_KEY = 24;

    private static int DES_ENCRYPT = 1;//加密
    private static int DES_DECRYPT = 0;//解密

    private static byte PARITY[] = {
            8, 1, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 2, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 3,
            0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8,
            0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8,
            8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0,
            0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8,
            8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0,
            8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0,
            4, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 8, 5, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 6, 8
    };

    private static final int EF_DG1 = 0;
    private static final int EF_DG2 = 1;
    private static final int EF_DG3 = 2;
    private static final int EF_DG4 = 3;
    private static final int EF_DG5 = 4;
    private static final int EF_DG6 = 5;
    private static final int EF_DG7 = 6;
    private static final int EF_DG8 = 6;
    private static final int EF_DG9 = 7;
    private static final int EF_DG10 = 8;
    private static final int EF_DG11 = 9;
    private static final int EF_DG12 = 10;
    private static final int EF_DG13 = 11;
    private static final int EF_DG14 = 12;
    private static final int EF_DG15 = 13;
    private static final int EF_DG16 = 14;
    private static final int EF_SOD = 15;
    private static final int EF_COM = 16;

    private static int ICAO_SID[] = {EF_NULL, EF_DG1, EF_DG2, EF_DG3, EF_DG4, EF_DG5, EF_DG6, EF_DG7, EF_DG8, EF_DG9, EF_DG10,
            EF_DG11, EF_DG12, EF_DG13, EF_DG14, EF_DG15, EF_DG16, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL,
            EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_SOD, EF_COM};

    private static String filesFid[] = {"0101", "0102", "0103", "0104", "0105", "0106", "0107", "0108",
            "0109", "010A", "010B", "010C", "010D", "010E", "010F", "0110", "011D", "011E"};
    private static String filesName[] = {"EF_DG1", "EF_DG2", "EF_DG3", "EF_DG4", "EF_DG5", "EF_DG6", "EF_DG7", "EF_DG8", "EF_DG9", "EF_DG10",
            "EF_DG11", "EF_DG12", "EF_DG13", "EF_DG14", "EF_DG15", "EF_DG16", "EF_SOD", "EF_COM"};

    boolean simulate = true;
    String readContent;//每条命令读取到的需要的信息

    //证件信息
    String originMRZ;   //证件信息，包含了证件号码，出生日期，有效期等
    String dealMRZ;   //经过处理后的护照信息
    String IDNumber;        //证件号码
    String birthdayDate;   //出生日期
    String expiryDate;        //有效期

    //射频模块和卡交互相关的数据
    String modelRandomNum;    //读卡器产生的随机数（16为）
    String cardRandomNum;    //卡产生的随机数（16为）
    String modelIntermediateNum;      //随机数	passport.GetRandom(16)
    String cardIntermediateNum;      //卡返回的数据解码后的一部分，和m_StrKifd相对应
    String totalRandomNum;         //完整的随机数
    String encryptDataToCard;      //读卡器对卡的随机数加密后的数据(密文)
    String encryptMacToCard;      //读卡器对卡的随机数响应的MAC值（MAC）
    String dataToCard;   //读卡器对卡的随机数的响应，由读卡器对卡的随机数加密后的数据 和 读卡器对卡的随机数响应的MAC值 组成（读卡器的密钥）
    String dataFromCard;  //卡对读卡器发送的随机数的响应 由卡对读卡器的随机数加密后的数据 和 卡对读卡器的随机数响应的MAC值 组成（卡的密钥）
    String encryptDataFromCard;      //卡对读卡器发送的随机数加密后返回来的数据（密文）
    String encryptMacFromCard;   //卡对读卡器发送的随机数响应的MAC（MAC）

    //密钥
    String keyToCardData;   //第一个key 读卡器发送的密文的密钥
    String keyMacToCardData;   //第二个key 校验读卡器发送的密文的MAC的密钥
    String keyFromCardData;     //卡返回密文的密钥
    String keyMacFromCardData;     //卡返回的密文的MAC的密钥
    String SSCStr;       //卡的随机数的后八位 + 读卡器的随机数的后八位

    byte[] orderSW = {0x00, 0x00};//apdu指令运行状态码

    public PassportHelper() {
        cardRandomNum = "4608F91988702212";
        modelRandomNum = "781723860C06C226";
        modelIntermediateNum = "0B795240CB7049B01C19B33E32804F0B";
        dataFromCard = "46B9342A41396CD7386BF5803104D7CEDC122B9132139BAF2EEDC94EE178534F2F2D235D074D7449";
    }

    /**
     * 获取密钥种子
     * 将 keyifd 和 keyicc 的前16位异或后赋给 cKseed
     *
     * @param keyifd 异或数组1
     * @param keyicc 异或数组2
     * @param cKseed 接收数组
     * @return 计算出来的密钥种子
     */
    String getKeySeed(String keyifd, String keyicc, byte[] cKseed) {
        byte[] keyifdData = Utils.hexStringTobyte(keyifd);
        byte[] keyiccData = Utils.hexStringTobyte(keyicc);
        for (int i = 0; i < 16; i++) {
            cKseed[i] = (byte) (((keyifdData[i] & 0xFF) ^ (keyiccData[i] & 0xFF)) & 0xFF);
        }

        return Utils.byte2HexStr(cKseed, 0, 16);
    }

    /**
     * 生成外部认证的数据（密文 + mac）
     *
     * @param cardRandom 随机数
     * @return 读卡器加密随机数的数据及相应的MAC值
     */
    public String getCipherData(String cardRandom) {
        if (simulate) {
            if (16 == cardRandom.length())
                cardRandomNum = cardRandom;
        }

        totalRandomNum = modelRandomNum + cardRandomNum + modelIntermediateNum;//构建完整的随机数
        encryptDataToCard = encryptOrDecryption(totalRandomNum, Utils.hexStringTobyte(keyToCardData), DES_ENCRYPT);//将随机数和生成的第一个key关联起来并加密

        String strAlign = alignString(encryptDataToCard, "80");
        encryptMacToCard = getMAC(strAlign, keyMacToCardData);//将mac加密 将加密后的随机数的密钥和生成的第二个可以关联起来

        dataToCard = encryptDataToCard + encryptMacToCard;

        return dataToCard;
    }

    /**
     * 内部认证，读卡器验证卡返回的密钥
     *
     * @param strRspData 卡返回的密钥
     * @return true：成功；false：失败
     */
    boolean verifyInside(String strRspData) {
        if (simulate) {
            if (80 == strRspData.length()) {
                dataFromCard = strRspData;
            }
        }

        encryptDataFromCard = dataFromCard.substring(0, 64);//密文
        encryptMacFromCard = dataFromCard.substring(dataFromCard.length() - 16);//MAC（本来应该先验证MAC值，验证通过了才解码）

        String strDecResp = encryptOrDecryption(encryptDataFromCard, Utils.hexStringTobyte(keyToCardData), DES_DECRYPT);//解码密文

        String rnd_icc = strDecResp.substring(0, 16);//卡的随机数
        String recifd = strDecResp.substring(16, 32);//读卡器的随机数
        cardIntermediateNum = strDecResp.substring(32, 64);

        byte[] keySeedData = new byte[128];
        getKeySeed(modelIntermediateNum, cardIntermediateNum, keySeedData);

        keyFromCardData = getDESKey(keySeedData, 1);//1:Kenc
        keyMacFromCardData = getDESKey(keySeedData, 2);//2:Kmac

        SSCStr = rnd_icc.substring(8, 16) + recifd.substring(8, 16);

        if (recifd.equals(modelRandomNum)) {
            return true;
        } else {
            readContent = ("RND.ifd比较失败[" + modelRandomNum + "-" + recifd + "]");
            return false;
        }
    }

    /**
     * 外部认证，卡验证读卡器发送的密钥
     *
     * @param CipherStr 发送给卡密钥
     * @return 卡返回来的密钥
     */
    public String verifyOutside(String CipherStr) {
        int nLen = CipherStr.length() / 2;
        byte[] apdu = new byte[nLen + 5];//= {0x00, 0x82, 0x00, 0x00, nLen}
        apdu[0] = 0x00;
        apdu[1] = (byte) 0x82;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte) nLen;

        System.arraycopy(Utils.hexStringTobyte(CipherStr), 0, apdu, 5, nLen);
        nLen += 5;

        byte[] response = new byte[256];
        int ret = transmit(apdu, nLen, response, response.length, orderSW);
        if (0x90 == (orderSW[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(response, 0, ret);
            } else {
                return "";
            }
        }

        return "";
    }

    /**
     * 通过名字选择MF(主文件)
     *
     * @return 非0失败
     */
    public int selectMFByName() {
        byte[] apdu = {0x00, (byte) 0xA4, 0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x02, 0x47, 0x10, 0x01};
        byte[] response = new byte[256];
        int ret = transmit(apdu, apdu.length, response, response.length, orderSW);
        if (ret >= 0 && (0x90 == (orderSW[0] & 0xFF) || 0x61 == (orderSW[0] & 0xFF))) {
            return 0;
        }

        return 1;
    }

    /**
     * 发送APDU指令
     *
     * @param cSend 指令数据
     * @param nSend 指令长度
     * @param cRecv 输出数据
     * @param nRecv 输出数据长度
     * @param ucSW  执行状态码
     * @return 输出数据的长度
     */
    byte[] receiveData = new byte[MAX_PATH];
    byte[] receiveDataLength = new byte[1];

    public int transmit(byte[] cSend, int nSend, byte[] cRecv, int nRecv, byte[] ucSW) {
        if (!rfid.IsOpen()) {
            return -1;
        }

        int ret = rfid.RFIDRfApdu(1, cSend, nSend, receiveData, receiveDataLength, ucSW);
        if (ret != 0) {
            return -1;
        }

        if ((ucSW[0] & 0xff) == 0X00 && (ucSW[1] & 0xff) == 0X00) {
            Log.e(TAG, "err.....................");
        }

        nRecv = (receiveDataLength[0] & 0xFF);

        if (nRecv > 0) {
            System.arraycopy(receiveData, 0, cRecv, 0, nRecv);
        }

        return nRecv;
    }


    /**
     * 分割证件信息
     */
    private boolean splitMRZ(String strMRZ) {
        IDNumber = strMRZ.substring(0, 9);
        birthdayDate = strMRZ.substring(13, 19);
        expiryDate = strMRZ.substring(21, 27);

        return true;
    }

    /**
     * 获取密钥
     *
     * @return
     */
    public boolean getKeys() {
        //通过MRZ获取个人信息
        if (originMRZ.length() > 0) {
            splitMRZ(originMRZ);//截取证件号/有效日期/出生年月
        } else {
            if (0 == IDNumber.length()) {
                readContent = "请输入护照信息[MRZ 或者 证件号码\\出生日期\\有效期]";
                return false;
            }
        }

        String strNumber_cd = calculateCheckDigit(IDNumber);
        String strDateOfBirth_cd = calculateCheckDigit(birthdayDate);
        String strExpiry_cd = calculateCheckDigit(expiryDate);

        dealMRZ = IDNumber + strNumber_cd + birthdayDate + strDateOfBirth_cd + expiryDate + strExpiry_cd;//处理后的护照信息

        byte[] ucKseedBCD = new byte[128];//护照信息的信息摘要
        Arrays.fill(ucKseedBCD, (byte) 0x00);
        ucKseedBCD = DigestEncoder.encodeEx("SHA1", dealMRZ);

        keyToCardData = getDESKey(ucKseedBCD, 1);    //1:Kenc
        keyMacToCardData = getDESKey(ucKseedBCD, 2);    //2:Kmac

        return true;
    }


    /**
     * 计算出strData的标志，用来区分证件号码，出生日期，有效期
     */
    public String calculateCheckDigit(String strData) {
        int nLen = strData.length();
        char[] cData = new char[nLen];

        strData.getChars(0, nLen, cData, 0);

        int nValue;
        int nD = 0;
        for (int i = 0; i < nLen; i++) {
            if ('A' <= cData[i] && cData[i] <= 'Z') {
                nValue = cData[i] - 55;
            } else if ('<' == cData[i]) {
                nValue = 0;
            } else {
                nValue = cData[i] - '0';
            }
            nValue = (nValue * MRZ_WEIGHT[i % 3]);
            nD += nValue;
        }

        nD = nD % 10;

        return ("" + nD);
    }

    /**
     * 获取一个key
     *
     * @param keySeed
     * @param nType
     * @return
     */
    public String getDESKey(byte[] keySeed, int nType) {
        byte[] ucSHA = new byte[64];
        Arrays.fill(ucSHA, (byte) 0);

        byte[] uck_dat = new byte[20];
        System.arraycopy(keySeed, 0, uck_dat, 0, 20);
        uck_dat[16] = 0x00;
        uck_dat[17] = 0x00;
        uck_dat[18] = 0x00;
        uck_dat[19] = 0x00;
        uck_dat[19] = (byte) (nType & 0xFF);    //ucKseed + nType

        ucSHA = DigestEncoder.encodeBytes("SHA1", uck_dat);
        adjustParity(ucSHA, 16);

        String strKey = Utils.byte2HexStr(ucSHA, 0, 16);
        strKey = strKey.toUpperCase();

        return strKey;
    }

    /**
     * 调整奇偶性
     */
    private void adjustParity(byte[] ucData, int nLen) {
        for (int i = 0; i < nLen; i++) {
            int by2 = (((ucData[i] & 0xFF) ^ 0xff) & 0xFF);
            if (8 == (PARITY[by2] & 0xFF)) {
                ucData[i] = (byte) (((ucData[i] & 0xFF) ^ 1) & 0xFF);
            } else {
                ucData[i] = (byte) (((ucData[i] & 0xFF) ^ 0) & 0xFF);
            }
        }
    }

    /**
     * 获取卡的随机数
     *
     * @return 获取到的随机数
     */
    public String getCardRandom(int len) {
        byte apdu[] = {0x00, (byte) 0x84, 0x00, 0x00, (byte) len};
        byte[] response = new byte[256];

        int ret = transmit(apdu, apdu.length, response, response.length, orderSW);
        if (0x90 == (orderSW[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(response, 0, ret);
            } else {
                return "";
            }
        } else {
            return "";
        }

    }

    /**
     * 将字符串的长度对齐为16（8）的倍数
     */
    private String alignString(String strS, String strFill) {
        String strTmp = strS + strFill;
        while (0 != (strTmp.length() % 16)) {
            strTmp += "00";
        }

        return strTmp;
    }


    /**
     * 生成MAC值
     *
     * @param strSource
     * @param keyMAC
     * @return
     */
    byte[] macKey = new byte[16];
    byte[] cMac = new byte[8];
    byte[] cMac2 = new byte[8];
    byte[] cData = new byte[8];

    public String getMAC(String strSource, String keyMAC) {
        int nLen = strSource.length() / 2;

        byte[] cSource = new byte[nLen];
        Utils.ASC2BCD(strSource.getBytes(), cSource, nLen);
        Utils.ASC2BCD(keyMAC.getBytes(), macKey, 16);

        Arrays.fill(cMac, (byte) 0);
        Arrays.fill(cMac2, (byte) 0);

        DesLib.DES_set_key_checked(Arrays.copyOfRange(macKey, 0, 8), 0x0A);//设置密钥并检验（第二个key）
        DesLib.DES_set_key_checked(Arrays.copyOfRange(macKey, 8, 16), 0x0B);//设置密钥并检验（第二个key）

        int i = 0;
        while (i < nLen) {
            System.arraycopy(cSource, i, cData, 0, 8);

            for (int x = 0; x < 8; x++) {
                cMac2[x] = (byte) (((cMac[x] & 0xFF) ^ (cData[x] & 0xFF)) & 0xFF);
            }

            DesLib.DES_ecb_encrypt(cMac2, cMac, 0x0A, DES_ENCRYPT);//加密mac2

            i += 8;
        }

        DesLib.DES_ecb_encrypt(cMac, cMac2, 0x0B, DES_DECRYPT);//解密mac
        DesLib.DES_ecb_encrypt(cMac2, cMac, 0x0A, DES_ENCRYPT);//加密mac

        return Utils.toHexString(cMac).toUpperCase();
    }


    /**
     * 生成一个选择要操作的文件的APDU指令
     *
     * @param strKey
     * @param strMac
     * @param strFID
     * @return APDU指令
     */
    private String getSelectFileOrder(String strKey, String strMac, String strFID) {
        String strCmdHeader = alignString("0CA4020C", "80");
        String strStuff = alignString(strFID, "80");

        String strStruff3DES = encryptOrDecryption(strStuff, Utils.hexStringTobyte(strKey), DES_ENCRYPT);//加密文件描述符
        String strDO87 = "870901" + strStruff3DES;
        String strM = strCmdHeader + strDO87;

        String strN = getNextSSC() + strM;
        strN = alignString(strN, "80");

        String strCC = getMAC(strN, strMac);

        String strDO8E = "8E08" + strCC;

        int nLen = strDO87.length() + strDO8E.length();
        String strLen = String.format("%02x", nLen / 2);

        return "0CA4020C" + strLen + strDO87 + strDO8E + "00";
    }


    /**
     * 得到一个读取文件的APDU指令
     */
    String getReadFileOrder(String strMac, int readlen, int offset) {
        String strCmdHeader, strLen;
        strLen = String.format("%02x", readlen);

        strCmdHeader = String.format("0CB0%s", String.format("%04x", offset));
        String strD097 = "9701" + strLen;//文件描述符
        String strN = getNextSSC() + alignString(strCmdHeader, "80") + strD097;
        strN = alignString(strN, "80");

        String strDO8E = "8E08" + getMAC(strN, strMac);//文件描述符

        strLen = String.format("%02x", (strD097.length() + strDO8E.length()) / 2);
        strLen = strLen.toUpperCase();

        return strCmdHeader + strLen + strD097 + strDO8E + "00";
    }

    /**
     * 获取字符串的子字符串
     *
     * @return 子字符串
     */
    String getSubString(String strDO) {
        String strName = strDO.substring(0, 2);
        String strLen = strDO.substring(2, 4);

        int nLen = Utils.AscToInt(strLen);
        int nOffset = 4;

        if (strName.substring(0, 2).equals("87")) {
            nOffset += 2;
            nLen -= 1;
        }

        return strDO.substring(nOffset, nOffset + nLen * 2);
    }

    /**
     * 解码读到的文件内容
     *
     * @param binContent 上次读取的文件内容（apdu指令）
     * @param nOffset    偏移量
     * @param nLen       错误信息的长度
     * @return 有效文件的长度
     */
    int decodeReadContent(String binContent, int nOffset, int nLen) {
        int nRLen = binContent.length();
        if (nRLen < 32) {
            return -1;
        }

        //D087和D099用来生成MAC，D08E用来验证生成的MAC是否正确，D087也用来生成apdu指令
        String strRAPDU_D087 = binContent.substring(0, nRLen - 8 - 20 - 4);
        String strRAPDU_D099 = binContent.substring(nRLen - 8 - 20 - 4, (nRLen - 8 - 20 - 4) + 4 * 2);
        String strRAPDU_D08E = binContent.substring(nRLen - 8 - 20 - 4 + 8, (nRLen - 8 - 20 - 4 + 8) + 10 * 2);

        String strN = getNextSSC() + strRAPDU_D087 + strRAPDU_D099;
        strN = alignString(strN, "80");
        String strCC = getMAC(strN, keyMacFromCardData);//MAC

        String strRAPDU_D08E_Value = getSubString(strRAPDU_D08E);

        if (!strRAPDU_D08E_Value.substring(0, 16).equals(strCC.substring(0, 16)))
            return -1;

        String strRAPDU_D087_Value = getSubString(strRAPDU_D087);

        String strBin = encryptOrDecryption(strRAPDU_D087_Value, Utils.hexStringTobyte(keyFromCardData), DES_DECRYPT);
        readContent = strBin.substring(0, nLen * 2);

        int FileLen = 0;
        if (0 == nOffset) {
            FileLen = Utils.AscToInt(strBin.substring(2, 4));
            if ((0x80 & FileLen) != 0) {
                int lenFieldLen = 0x7F & FileLen;
                FileLen = Utils.AscToInt(strBin.substring(4, 4 + 2 * lenFieldLen)) - (nLen - 2 - lenFieldLen);
            } else {
                FileLen -= (nLen - 2);
            }
        }

        return FileLen;
    }

    /**
     * 读取文件
     *
     * @param receiveBuf 保存读取数据的数组
     * @param fileID     文件标志符
     * @return 读取到的数据的长度
     */
    private int readFile(byte[] receiveBuf, int fileID) {
        int orderOffset = 0;
        int receiveOffset = 0;
        int fileLength;//要读取的文件的整个长度
        int onceReadLength;//某次读取的文件的长度
        int decodeResult;//解码结果

        String orderResult;
        String order = getSelectFileOrder(keyFromCardData, keyMacFromCardData, filesFid[fileID]);//得到一个选择文件的APDU指令
        orderResult = sendOrder(order);//发送选择文件的APDU指令，并返回结果
        getNextSSC();
        if ("".equals(orderResult)) {
            return -1;
        }

        order = getReadFileOrder(keyMacFromCardData, 4, orderOffset);//得到读bin文件的apdu指令

        if (!simulate) {
            orderResult = "8709019FF0EC34F9922651990290008E08AD55CC17140B2DED9000";
        } else {
            orderResult = sendOrder(order);//读bin文件 理解为：先读取一个文件，这个文件里放的是读取真正数据的apdu指令
            if ("".equals(orderResult)) {
                return -1;
            }
        }

        fileLength = decodeReadContent(orderResult, orderOffset, 4);//解码读取到的文件
        orderOffset += 4;

        if (0 > fileLength || 0 == fileLength) {
            return -1;
        }

        if (fileLength > 0) {
            System.arraycopy(Utils.hexStringToBytes(readContent), 0, receiveBuf, receiveOffset, readContent.length() / 2);
            receiveOffset += readContent.length() / 2;
        }

        while (fileLength > 0) {
            if (fileLength > MAXREADLENGTH) {
                onceReadLength = MAXREADLENGTH;
            } else {
                onceReadLength = fileLength;
            }

            //获取apdu指令消耗的时间
            order = getReadFileOrder(keyMacFromCardData, onceReadLength, orderOffset);
            if (!simulate) {
                orderResult = "871901FB9235F4E4037F2327DCC8964F1F9B8C30F42C8E2FFF224A990290008E08C8B2787EAEA07D749000";
            } else {
                orderResult = sendOrder(order);
                if ("".equals(orderResult)) {
                    return -1;
                }
            }

            decodeResult = decodeReadContent(orderResult, orderOffset, onceReadLength);
            if (0 > decodeResult) {
                return -1;
            } else {
                System.arraycopy(Utils.hexStringTobyte(readContent), 0, receiveBuf, receiveOffset, readContent.length() / 2);
                receiveOffset += readContent.length() / 2;
            }

            orderOffset += onceReadLength;
            fileLength -= onceReadLength;
        }

        return receiveOffset;
    }


    /**
     * 发送APDU指令
     *
     * @param strAPDU APDU指令
     * @return apdu指令得到的数据
     */
    byte[] readCardResponse = new byte[MAX_PATH];

    String sendOrder(String APDUStr) {
        byte[] apdu = Utils.hexStringToBytes(APDUStr);

        int ret = transmit(apdu, apdu.length, readCardResponse, readCardResponse.length, orderSW);//发送apdu指令
        if (0x90 == (orderSW[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(readCardResponse, 0, ret) + Utils.toHexString(orderSW);
            } else {
                return Utils.toHexString(orderSW);
            }
        }

        return "";
    }


    /**
     * 获取下一个SSC
     */
    byte[] cSSCIn = new byte[8];
    byte[] cSSCOut = new byte[8];

    public String getNextSSC() {
        System.arraycopy(Utils.hexStringTobyte(SSCStr), 0, cSSCIn, 0, 8);
        Utils.TurnChar(cSSCIn, cSSCOut, 8);

        long tmp;
        long llTrun = 0;
        for (int t = 7; t >= 0; t--) {
            tmp = (cSSCOut[t] & 0xFF);
            llTrun += (tmp << (t * 8));
        }

        llTrun++;
        for (int t = 0; t < 8; t++) {
            tmp = 0xFF;
            tmp = llTrun & (tmp << (t * 8));
            cSSCIn[t] = (byte) (tmp >> (t * 8));
        }

        Utils.TurnChar(cSSCIn, cSSCOut, 8);
        SSCStr = Utils.byte2HexStr(cSSCOut, 0, 8);

        return SSCStr;
    }


    /**
     * 3DES加密解密
     *
     * @param strSource 要加解密的字符
     * @param szkey     根据护照信息生成的key
     * @param nType     加密解密的判断标志
     * @return 加解密后的结果
     */
    public String encryptOrDecryption(String strSource, byte[] szkey, int nType) {
        byte[] block_key = new byte[9];

        int nSourceLen = strSource.length() / 2;

        byte[] key = new byte[LEN_OF_KEY];
        int key_len = 16;

        byte[] cKey = new byte[LEN_OF_KEY];
        System.arraycopy(szkey, 0, cKey, 0, key_len);

        System.arraycopy(cKey, 0, key, 0, key_len);
        System.arraycopy(cKey, 0, key, key_len, LEN_OF_KEY - key_len);

        Arrays.fill(block_key, (byte) 0);
        System.arraycopy(key, 0, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0A);

        System.arraycopy(key, 8, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0B);

        System.arraycopy(key, 16, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0C);//设置密钥，不需要校验

        int nLen = strSource.length();

        byte[] cSource = new byte[nLen / 2];

        System.arraycopy(Utils.hexStringTobyte(strSource), 0, cSource, 0, nLen / 2);

        byte[] cOut = new byte[nLen / 2 + 1];
        cOut[nLen / 2] = 0x00;

        byte[] IV = new byte[8];
        Arrays.fill(IV, (byte) 0);
        DesLib.DES_ede3_cbc_encrypt(cSource, cOut, nSourceLen, IV, nType);//加解密

        String ret_str = Utils.toHexString(cOut);
        return ret_str.substring(0, ret_str.length() - 2).toUpperCase();
    }

    /**
     * 验证护照信息
     *
     * @param information 护照信息
     */
    public int verifyPassportInfo(String information) {
        originMRZ = information;
        //根据护照信息生成两个不同的key
        getKeys();

        //选择主目录
        int Ret = selectMFByName();
        if (0 != Ret) {
            return 3;
        }

        //获取随机数
        String strRndIcc = getCardRandom(8);
        if (strRndIcc.equals("")) {
            return 4;
        }

        //生成外部认证的数据
        String cipherData = getCipherData(strRndIcc);
        if (cipherData.equals("")) {
            return 4;
        }

        //外部认证
        String strRes = verifyOutside(cipherData);
        if (strRes.equals("")) {
            return 4;
        }

        //内部认证
        if (!verifyInside(strRes)) {
            return 4;
        }

        return 0;
    }

    /**
     * 读护照
     *
     * @param buf     保存数据的数组
     * @param bufLen  数据的长度
     * @param nFileID 文件标志符
     * @return 读取的文件长度
     */
    public int readPassportFile(byte[] buf, int bufLen, int nFileID) {
        int ret = readFile(buf, nFileID);//moubiao expend time here
        if (bufLen < ret || ret < 0) {
            return -1;
        }

        bufLen = ret;

        return bufLen;
    }

}

