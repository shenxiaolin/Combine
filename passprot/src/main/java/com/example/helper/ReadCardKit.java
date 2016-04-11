package com.example.helper;

import android.graphics.Bitmap;
import android.util.Log;

import com.xd.Converter;
import com.xd.rfid;

import java.util.Arrays;

import at.mroland.android.apps.imagedecoder.CxImage;

public class ReadCardKit {
    private static String TAG = "moubiao";
    private static int MAX_PATH = 1024 * 64;

    private String m_strName;
    private String m_strMRZ1;//	证件信息
    private String m_strMRZ2;
    private String m_strMRZ3;
    private String m_strIssuer;
    private String m_strDocType;
    private String m_strBirthDate;
    private String m_strDocNumber;
    private String m_strNationlity;
    private String m_strOptData;
    private String m_strSex;
    private String m_strValiduntil;

    private String m_strSurname;
    private String m_strChipAtr;
    private String m_strChipUID;


    byte[] photo_dat = new byte[MAX_PATH];
    private int photo_dat_len = 0;

    private byte[] ATRData = new byte[64];

    private PassportLib mPassPort = new PassportLib();

    public void SplitMRZ(String strMRZ1, String strMRZ2) {

        String str = "";
        m_strDocType = strMRZ1.substring(0, 2);
        m_strDocNumber = strMRZ2.substring(0, 9);
        m_strNationlity = strMRZ2.substring(10, 13);
        m_strBirthDate = strMRZ2.substring(13, 19);
        str = strMRZ2.substring(20, 21);
        if (str.equals("F")) {
            m_strSex = "女/" + str;
        } else {
            m_strSex = "男/" + str;
        }

        m_strValiduntil = "20" + strMRZ2.substring(21, 27);
        m_strIssuer = m_strNationlity;
        m_strOptData = strMRZ2.substring(28, 28 + 14);
        if (m_strOptData.substring(m_strOptData.length() - 1).equals("9")) {
            m_strBirthDate = "19" + m_strBirthDate;
        } else {
            m_strBirthDate = "20" + m_strBirthDate;
        }

        m_strBirthDate = m_strBirthDate.substring(0, 4) + "/" + m_strBirthDate.substring(4, 6) + "/" + m_strBirthDate.substring(6, 8);
        m_strValiduntil = m_strValiduntil.substring(0, 4) + "/" + m_strValiduntil.substring(4, 6) + "/" + m_strValiduntil.substring(6, 8);
    }


    private String FindTagStr(byte[] buf, int bufLen, byte[] tag, int tagLen) {

        byte[] value = new byte[MAX_PATH];
        int len = value.length;
        len = getTagFromTlvEx(buf, 0, bufLen, tag, tagLen, value, len);
        if (len != -1) {
            return Utils.byte2HexStr(value, 0, len);
        }

        return "";
    }

    public int getTagFromTlvEx(byte[] data, int pos, int dataLen, byte[] tag, int tagLen, byte[] value, int valueLen) {
        if (!((null != data) && (null != tag) && (0 != dataLen) && (0 != tagLen))) {
            return -1;
        }

        if (pos >= data.length) return -1;

        int tmpTagLen = 0;   //现在只处理TAG域最多为两字节

        int tmpLenLen = 0;

        int tmpValueLen = 0;

        int tmpTlvLen = 0;


        if (0x1F == ((data[pos] & 0xFF) & 0x1F)) {
            tmpTagLen = 2;
        } else {
            tmpTagLen = 1;
        }

        if (dataLen <= tmpTagLen) {
            return -1;
        }

        int ttt = pos + tmpTagLen;
        if (0 != ((data[ttt] & 0xFF) & 0x80)) {
            tmpLenLen = (data[ttt] & 0xFF) & 0x7F;
            tmpValueLen = 0;
            for (int i = 0; i < tmpLenLen; i++) {
                tmpValueLen = (tmpValueLen << 8) + (data[ttt + i + 1] & 0xFF);
            }
            tmpLenLen += 1;
        } else {
            tmpLenLen = 1;
            tmpValueLen = (data[ttt] & 0xFF);
        }

        tmpTlvLen = tmpTagLen + tmpLenLen + tmpValueLen;
        if (dataLen >= tmpTlvLen)//是一个有效的TLV
        {
            ttt = pos + tmpTagLen + tmpLenLen;

            boolean bcmp = true;
            for (int t = 0; t < tmpTagLen; t++) {
                if ((tag[t] & 0xFF) != (data[pos + t] & 0xFF)) {
                    bcmp = false;
                    break;
                }
            }

            if (tagLen == tmpTagLen && bcmp) {
                System.arraycopy(data, ttt, value, 0, data.length - ttt);
                valueLen = tmpValueLen;
                return valueLen;
            } else if (0x20 == ((data[pos] & 0xFF) & 0x20)) //在子TLV中查找
            {
                int ret = getTagFromTlvEx(data, ttt, tmpValueLen, tag, tagLen, value, valueLen);

                if (ret != -1) {
                    valueLen = ret;
                    return valueLen;
                }
            }

            if (dataLen > (tmpTagLen + tmpLenLen + tmpValueLen))//data中可能包含多个TLV
            {
                return getTagFromTlvEx(data, pos + tmpTlvLen, dataLen - tmpTlvLen, tag, tagLen, value, valueLen);
            }
        }

        return -1;
    }


    /**
     * 读卡
     */
    public int ReadCard() {
        int ret;
        //激活卡
        byte[] tmpATRData = new byte[64];//激活cpu卡时的ATS响应
        Utils.memset(tmpATRData, 0, tmpATRData.length);
        int ATRlen = tmpATRData.length;//ATR长度
        ret = ReaderCardReset(tmpATRData);

        //判断激活卡的结果
        if (ret != 0x90) {
            Utils.memset(ATRData, 0x00, ATRData.length);
            return ret;
        }
        if (Utils.memcmp(ATRData, tmpATRData, ATRlen)) {
            return ret;
        }

        //处理激活卡后得到的数据
        System.arraycopy(tmpATRData, 0, ATRData, 0, ATRlen);
        if (ATRData[2] != 0x00 || ATRData[3] != 0x00 || ATRData[4] != 0x00 || (ATRData[6] != 0x00)) {
            ret = ReadPassPortInf(0, 0);//moubiao expend time here
            if (ret != 0) {
                Utils.memset(ATRData, 0, ATRData.length);
            }
        }

        return ret;
    }

    /**
     * 激活cpu卡
     * 读护照前的一些初始化操作，初始化射频模块，设置射频协议，设置射频模块的状态（打开还是关闭），获取卡的序列号，激活cpu卡
     *
     * @param bRats ATS 保存卡的响应数据
     */
    public int ReaderCardReset(byte[] bRats) {
        //初始化射频模块
        int nRet = rfid.RFIDInit();
        if (nRet != 0) {
            return 1;
        }
        //设置射频协议
        nRet = rfid.RFIDTypeSet(0);
        if (nRet != 0) {
            return 1;
        }
        //标记射频模块的状态
        rfid.SetOpenStatu(true);

        byte sak;//卡的响应
        short atqa;//模块发出的指令
        String cardType;//卡的类型
        String strOut = "";
        byte[] bIdLen = new byte[1];
        byte[] bSNR = new byte[64];//序列号
        //获取卡的序列号
        nRet = rfid.RFIDGetSNR(0, bIdLen, bSNR);
        if (nRet != 0) {
            return 1;
        }

        sak = bSNR[bIdLen[0] - 3];
        atqa = (short) (bSNR[bIdLen[0] - 1] * 256 + bSNR[bIdLen[0] - 2]);
        if (((sak & 0x20) == 0x20) || (sak == 0x53)) {
            if (atqa == 0x0344) {
                cardType = ", Desfire card";
            } else {
                cardType = ", CPU card";
            }
        } else if (atqa == 0x0044) {
            cardType = ", UL card";
        } else if ((sak == 0x08) || (sak == 0x18)) {
            if (sak == 0x08) {
                cardType = ", S50 card";
            } else {
                cardType = ", S70 card";
            }
        } else {
            cardType = ", Unknown card";
        }

        strOut += cardType;
        String snr = Converter.printHexLenString(bSNR, bSNR.length);

        Arrays.fill(bRats, (byte) 0);
        //激活cpu卡
        nRet = rfid.RFIDTypeARats(1, bRats);
        if (nRet != 0) {
            return 1;//失败
        }

        return 0x90;//成功
    }

    /**
     * 读取护照信息
     */
    public int ReadPassPortInf(int m_iPort, int m_iIfOpen) {
        int ret;
        String strLog = "";
        byte tag[] = new byte[2];
        long StartTime, STime, ETime, EndTime;//DWORD
        String m_strMRZ;

        //当前读到的信息载取后赋值给下面三个变量
        String m_PassPortNum = "G80014686";//"G80013706";
        String m_BirthDate = "19910817";//"19740901";
        String m_Validuntil = "20250922";
        //判断护照信息是否正确
        if (m_PassPortNum.length() != 9) {
            return -2;
        }
        if (m_BirthDate.length() != 8) {
            return -3;
        }
        if (m_Validuntil.length() != 8) {
            return -4;
        }
        m_strMRZ = m_PassPortNum + "<<<<" + m_BirthDate.substring(m_BirthDate.length() - 6) + "<<"
                + m_Validuntil.substring(m_Validuntil.length() - 6) + "<<<<<<<<<<<<<<<<<";

        //验证护照
        ret = mPassPort.EMP_ReadPassport_Auth(m_strMRZ);
        if (ret != 0) {
            Log.d(TAG, "Passport doBAC Failed!");
            return 2;
        }

        //读护照 EFDG1
        int bufLen = MAX_PATH;//1024*64;
        byte[] buf = new byte[bufLen];
        ret = mPassPort.EMP_ReadPassport_Read(buf, bufLen, 0);
        bufLen = ret;
        if (ret == -1) {
            Log.d(TAG, "Passport Read EFDG1 Failed!");
            return 3;
        }
        Log.d(TAG, "Passport Read EFDG1 Success!");

        tag[0] = 0x5F; //MRZ//电子护照
        tag[1] = 0x1F;
        String MRZstr = FindTagStr(buf, bufLen, tag, 2);
        if (!MRZstr.substring(0, 2).equals("CS")) {
            m_strMRZ1 = MRZstr.substring(0, 88);
            m_strMRZ2 = MRZstr.substring(MRZstr.length() - 88);

            m_strMRZ1 = Utils.hexStrToAscStr(m_strMRZ1);
            m_strMRZ2 = Utils.hexStrToAscStr(m_strMRZ2);

            SplitMRZ(m_strMRZ1, m_strMRZ2);
        }


        //EFDG11
        bufLen = MAX_PATH;
        ret = mPassPort.EMP_ReadPassport_Read(buf, bufLen, 10);
        bufLen = ret;
        if (ret == -1) {
            Log.d(TAG, "Passport Read EFDG11 Failed!");
            return 5;
        }
        Log.d(TAG, "Passport Read EFDG11 Success!");

        tag[0] = 0x5F;//名字TAG
        tag[1] = 0x0E;

        //
        m_strName = FindTagStr(buf, bufLen, tag, 2);
        m_strName = Utils.hexStrToAscStr(m_strName);

        tag[0] = 0x5F;//英文姓名
        tag[1] = 0x0F;

        //
        m_strSurname = FindTagStr(buf, bufLen, tag, 2);
        m_strSurname = Utils.hexStrToAscStr(m_strSurname);

        int pos = m_strName.indexOf("<<");
        if (pos > 0) {
            m_strName = m_strName.replace("<<", " ");
        }

        if (m_strSurname.equals("")) {
            if (!MRZstr.substring(0, 2).equals("CS")) {//双程证
                m_strSurname = m_strMRZ1.substring(m_strMRZ1.length() - (44 - 5));
                pos = m_strSurname.indexOf("<<");
                if (pos > 0) {
                    m_strSurname = m_strSurname.replace("<<", ",");
                }
            }
        } else {
            pos = m_strSurname.indexOf("<<");
            if (pos > 0) {
                m_strSurname = m_strSurname.replace("<<", ",");
            }
        }

        //EFDG2
        bufLen = MAX_PATH;//MAX_PATH = 2014 * 64
        ret = mPassPort.EMP_ReadPassport_Read(buf, bufLen, 1);//moubiao expend time here  读取照片
        bufLen = ret;
        if (ret == -1) {
            Log.d(TAG, "Passport Read EFDG2 Failed!");
            Log.e("读相片失败", strLog);
            return 4;
        }
        Log.d(TAG, "Passport Read EFDG2 Success!");

        //查找图片tag，并保存为jpg
        tag[0] = 0x5F;//照片TAG
        tag[1] = 0x2E;

        //
        int v_len = 0;
        v_len = getTagFromTlvEx(buf, 0, bufLen, tag, 2, photo_dat, v_len);
        if (v_len == -1) {
            Log.d(TAG, "Passport getPhote Failed!");
            return 4;
        }

        photo_dat_len = v_len;

        Log.d(TAG, "Passport Read Success!");

        return 0;
    }

    public boolean openModule() {
        //
        int nRet = rfid.RFIDModuleOpen();
        if (nRet != 0) {
            return false;
        }

        return true;
    }

    public boolean closeModule() {
        int nRet = rfid.RFIDMoudleClose();
        if (nRet != 0) {
            return false;
        }

        nRet = rfid.RFIDRfClose();
        if (nRet != 0) {
            return false;
        }

        rfid.SetOpenStatu(false);
        return true;
    }


    public byte[] getPhoto() {
        return photo_dat;
    }

    public Bitmap getPhotoBmp() {
        if (null == photo_dat) return null;

        CxImage fac = CxImage.Decode(photo_dat, 46, photo_dat_len - 46);//moubiao

        if (fac != null) {
            Bitmap map = fac.getBitmap();

            return map;
        }

        return null;
    }

    public String getName() {
        return m_strName;
    }

    public String getMRZ1() {
        return m_strMRZ1;
    }

    public String getMRZ2() {
        return m_strMRZ2;
    }

    public String getMRZ3() {
        return m_strMRZ3;
    }

    public String getIssuer() {
        return m_strIssuer;
    }

    public String getDocType() {
        return m_strDocType;
    }

    public String getBirthDate() {
        return m_strBirthDate;
    }

    public String getChipAtr() {
        return m_strChipAtr;
    }

    public String getChipUID() {
        return m_strChipUID;
    }

    public String getDocNumber() {
        return m_strDocNumber;
    }

    public String getNationlity() {
        return m_strNationlity;
    }

    public String getOptData() {
        return m_strOptData;
    }

    public String getSex() {
        return m_strSex;
    }

    public String getSurname() {
        return m_strSurname;
    }

    public String getValiduntil() {
        return m_strValiduntil;
    }
}
