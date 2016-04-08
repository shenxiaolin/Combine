package com.example.helper;


import android.util.Log;

import com.openssl.DesLib;
import com.openssl.DigestEncoder;
import com.xd.rfid;

public class PassportLib {
    private static int MAX_PATH = 1024 * 1024;
    private static byte MRZ_WEIGHT[] = {7, 3, 1};
    private static int MAXCHUNK = 118;
    private static int EF_NULL = 0;
    private static int WS_EX_LAYERED = 0x00080000;
    private static int LWA_COLORKEY = 0x00000001;
    private static int LWA_ALPHA = 0x00000002;
    private static int LEN_OF_KEY = 24;

    private static int DES_ENCRYPT = 1;
    private static int DES_DECRYPT = 0;


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


    private static String strTagFid[] = {"0101", "0102", "0103", "0104", "0105", "0106", "0107", "0108",
            "0109", "010A", "010B", "010C", "010D", "010E", "010F", "0110", "011D", "011E"};

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


    private static String strTagName[] = {"EF_DG1", "EF_DG2", "EF_DG3", "EF_DG4", "EF_DG5", "EF_DG6", "EF_DG7", "EF_DG8", "EF_DG9", "EF_DG10",
            "EF_DG11", "EF_DG12", "EF_DG13", "EF_DG14", "EF_DG15", "EF_DG16", "EF_SOD", "EF_COM"};

    private static int ICAO_SID[] = {EF_NULL, EF_DG1, EF_DG2, EF_DG3, EF_DG4, EF_DG5, EF_DG6, EF_DG7, EF_DG8, EF_DG9, EF_DG10, EF_DG11, EF_DG12, EF_DG13, EF_DG14, EF_DG15, EF_DG16,
            EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_NULL, EF_SOD, EF_COM};


    //
    boolean Simulator = true;
    String m_strPassportNo;
    String m_strLogInfo;
    String m_strLastErrInfo;
    byte[] m_byteLastErrInfo;

    //	证件信息
    String m_StrMRZ1;
    String m_StrMRZ2;

    String m_StrNumber;        //证件号码
    String m_StrDateOfBirth;    //出生日期
    String m_StrExpiry;        //有效期

    String m_StrMRZInfo;
    String m_StrMRZInfoSHA;

    //
    //---------------------------------------------------------
    //---------------------------------------------------------
    String m_StrKmrz;
    String m_StrKseedHash;
    String m_StrKseed;            //Kseed (SHA1 hash digest of kmrz)
    String m_StrKenc;
    String m_StrKmac;

    String m_StrRndIfd;    //随机数	passport.GetRandom(8)
    String m_StrRndIcc;    //随机数	passport.iso_7816_get_challenge(8)
    String m_StrKifd;        //随机数	passport.GetRandom(16)
    String m_StrS;
    String m_StrEifd;
    String m_StrMifd;        //m_StrEifd的MAC
    String m_StrCmdData;    //发送给证件的加密信息。
    String m_StrRespData;    //证件对m_StrCmdData的响应数据
    String m_StrResp;        //证件对m_StrCmdData的响应
    String m_StrRespMac;    //证件对m_StrCmdData的响应MAC

    String m_StrKseed2;
    String m_StrKicc;
    String m_StrKSenc;
    String m_StrKSmac;
    String m_StrSSC;

    public PassportLib() {
        m_StrRndIcc = "4608F91988702212";
        m_StrRndIfd = "781723860C06C226";
        m_StrKifd = "0B795240CB7049B01C19B33E32804F0B";
        m_StrRespData = "46B9342A41396CD7386BF5803104D7CEDC122B9132139BAF2EEDC94EE178534F2F2D235D074D7449";
    }


    int ReadFile(byte[] buf, int bufLen, int nFileID) {
        if (null == buf) {
            return -1;
        }

        String strAPDU, strRes;
        boolean bRead = false;
        String strFileName;
        int nRecvLen = 0;
        int nOffset = 0;
        int nLen = 0;

        int ret = onSRFEX(buf, nFileID);
        if (bufLen < ret) {
            return -1;
        }

        if (ret < 0) {
            return -1;
        }
        bufLen = ret;

        return bufLen;
    }


    //---------------------------------------------------------
    //函数说明:会话密钥的认证和建立
    //---------------------------------------------------------
    boolean AuthRespData(String strRspData) {
        if (Simulator) {
            if (80 == strRspData.length()) {
                m_StrRespData = strRspData;
            }
        }

        m_StrResp = m_StrRespData.substring(0, 64);
        m_StrRespMac = m_StrRespData.substring(m_StrRespData.length() - 16);

        String strDecResp = TDES_Encrypt(m_StrResp, Utils.hexStringTobyte(m_StrKenc), DES_DECRYPT);

        String rnd_icc = strDecResp.substring(0, 16);
        String recifd = strDecResp.substring(16, 32);
        m_StrKicc = strDecResp.substring(32, 64);

        byte[] ucKseed = new byte[128];
        m_StrKseed2 = GetKseed(m_StrKifd, m_StrKicc, ucKseed);

        m_StrKSenc = GetDESKey(ucKseed, 1);    //1:Kenc
        m_StrKSmac = GetDESKey(ucKseed, 2);    //2:Kmac

        m_StrSSC = rnd_icc.substring(8, 16) + recifd.substring(8, 16);

        if (recifd.equals(m_StrRndIfd)) {
            return true;
        } else {
            m_strLastErrInfo = ("RND.ifd比较失败[" + m_StrRndIfd + "-" + recifd + "]");
            return false;
        }
    }


    //---------------------------------------------------------
    String GetKseed(String strKifd, String strKicc, byte[] cKseed) {

        byte[] cKifd = Utils.hexStringTobyte(strKifd);

        byte[] cKicc = Utils.hexStringTobyte(strKicc);

        for (int i = 0; i < 16; i++) {
            cKseed[i] = (byte) (((cKifd[i] & 0xFF) ^ (cKicc[i] & 0xFF)) & 0xFF);
        }

        String strKseed = Utils.byte2HexStr(cKseed, 0, 16);

        return strKseed;
    }


    public String Authenticate(String strCmdData) {
        int nLen = strCmdData.length() / 2;
        byte[] apdu = new byte[nLen + 5];//= {0x00, 0x82, 0x00, 0x00, nLen}
        apdu[0] = 0x00;
        apdu[1] = (byte) 0x82;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte) nLen;

        System.arraycopy(Utils.hexStringTobyte(strCmdData), 0, apdu, 5, nLen);
        nLen += 5;

        byte[] response = new byte[256];
        int responseLen = response.length;
        byte[] sw = {0x00, 0x00};

        int ret = Transmit(apdu, nLen, response, responseLen, sw);
        if (0x90 == (sw[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(response, 0, ret);
            } else {
                return "";
            }
        }

        return "";

    }


    //函数说明:会话密钥的认证和建立
    public String GetCmdData(String strRndICC) {
        if (Simulator) {
            if (16 == strRndICC.length())
                m_StrRndIcc = strRndICC;
        }

        m_StrS = m_StrRndIfd + m_StrRndIcc + m_StrKifd;
        m_StrEifd = TDES_Encrypt(m_StrS, Utils.hexStringTobyte(m_StrKenc), DES_ENCRYPT);

        String strSsc = "";
        String strAlign = AlignString(m_StrEifd, "80");

        m_StrMifd = TDES_MAC(strAlign, m_StrKmac);

        m_StrCmdData = m_StrEifd + m_StrMifd;

        return m_StrCmdData;
    }


    //
    public int SelectMFbyName() {
        byte[] apdu = {0x00, (byte) 0xA4, 0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x02, 0x47, 0x10, 0x01};
        byte[] response = new byte[256];

        int responseLen = response.length;
        byte[] sw = {0x00, 0x00};

        int ret = Transmit(apdu, apdu.length, response, responseLen, sw);
        if (ret >= 0 &&
                (0x90 == (sw[0] & 0xFF) || 0x61 == (sw[0] & 0xFF))) {
            return 0;
        }

        return 1;
    }

    public int Transmit(byte[] cSend, int nSend, byte[] cRecv, int nRecv, byte[] ucSW) {
        if (!rfid.IsOpen()) {
            return -1;
        }

        byte[] bOutData = new byte[MAX_PATH];
        byte[] bOutLen = new byte[1];


        int ret = rfid.RFIDRfApdu(1, cSend, nSend, bOutData, bOutLen, ucSW);
        if (ret != 0) {
            return -1;
        }

        if (ret == 0 && (ucSW[0] & 0xff) == 0X00 && (ucSW[1] & 0xff) == 0X00) {
            Log.e("", "err.....................");
        }

        nRecv = (bOutLen[0] & 0xFF);

        if (nRecv > 0) {
            System.arraycopy(bOutData, 0, cRecv, 0, nRecv);
        }

        return nRecv;
    }


    private boolean SplitMRZ(String strMRZ) {
        m_StrNumber = strMRZ.substring(0, 9);
        m_StrDateOfBirth = strMRZ.substring(13, 19);
        m_StrExpiry = strMRZ.substring(21, 27);

        return true;
    }

    public boolean GetKeys() {
        //通过MRZ获取个人信息
        if (m_StrMRZ2.length() > 0) {
            SplitMRZ(m_StrMRZ2);//截取证件号/有效日期/出生年月
        } else {
            if (0 == m_StrNumber.length()) {
                m_strLastErrInfo = "请输入护照信息[MRZ 或者 证件号码\\出生日期\\有效期]";
                return false;
            }
        }

        String strNumber_cd = CalculateCheckDigit(m_StrNumber);
        String strDateOfBirth_cd = CalculateCheckDigit(m_StrDateOfBirth);
        String strExpiry_cd = CalculateCheckDigit(m_StrExpiry);

        m_StrKmrz = m_StrNumber + strNumber_cd + m_StrDateOfBirth + strDateOfBirth_cd + m_StrExpiry + strExpiry_cd;

        byte ucKseedBCD[] = new byte[128];
        for (int s = 0; s < ucKseedBCD.length; s++) {
            ucKseedBCD[s] = 0x00;
        }

        ucKseedBCD = DigestEncoder.encodeEx("SHA1", m_StrKmrz);

        m_StrKenc = GetDESKey(ucKseedBCD, 1);    //1:Kenc
        m_StrKmac = GetDESKey(ucKseedBCD, 2);    //2:Kmac

        return true;
    }


    public String CalculateCheckDigit(String strData) {
        //
        int nLen = strData.length();
        char[] cData = new char[nLen];

        strData.getChars(0, nLen, cData, 0);

        int nValue;
        int nD = 0;
        for (int i = 0; i < nLen; i++) {
            if ('A' <= cData[i] && cData[i] <= 'Z')
                nValue = cData[i] - 55;
            else if ('<' == cData[i])
                nValue = 0;
            else
                nValue = cData[i] - '0';

            nValue = (nValue * MRZ_WEIGHT[i % 3]);
            nD += nValue;
        }


        nD = nD % 10;

        return ("" + nD);
    }

    //
    public String GetDESKey(byte[] ucKseed, int nType) {
        byte[] ucSHA = new byte[64];
        byte[] ucSHA_ASC = new byte[64];
        Utils.memset(ucSHA, 0, ucSHA.length);

        byte[] uck_dat = new byte[20];
        System.arraycopy(ucKseed, 0, uck_dat, 0, 20);

        uck_dat[16] = 0x00;
        uck_dat[17] = 0x00;
        uck_dat[18] = 0x00;
        uck_dat[19] = 0x00;
        uck_dat[19] = (byte) (nType & 0xFF);    //ucKseed + nType

        ucSHA = DigestEncoder.encodeBytes("SHA1", uck_dat);
        AdjustParity(ucSHA, 16);        //

        String strKey = Utils.byte2HexStr(ucSHA, 0, 16);
        strKey = strKey.toUpperCase();

        return strKey;
    }

    private void AdjustParity(byte[] ucData, int nLen) {
        for (int i = 0; i < nLen; i++) {
            int by2 = (((ucData[i] & 0xFF) ^ 0xff) & 0xFF);

            if (8 == (PARITY[by2] & 0xFF)) {
                ucData[i] = (byte) (((ucData[i] & 0xFF) ^ 1) & 0xFF);
            } else {
                ucData[i] = (byte) (((ucData[i] & 0xFF) ^ 0) & 0xFF);
            }
        }
    }

    public String GetCardRandom(int len) {
        byte apdu[] = {0x00, (byte) 0x84, 0x00, 0x00, (byte) len};
        byte[] response = new byte[256];
        int responseLen = response.length;
        byte sw[] = {0x00, 0x00};

        int ret = Transmit(apdu, apdu.length, response, responseLen, sw);
        if (0x90 == (sw[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(response, 0, ret);
            } else {
                return "";
            }
        } else {
            return "";
        }

    }

    //字符串对齐
    private String AlignString(String strS, String strFill) {
        String strTmp = strS + strFill;
        while (0 != (strTmp.length() % 16)) {
            strTmp += "00";
        }

        return strTmp;
    }


    //函数说明 ： MAC计算
    public String TDES_MAC(String strSource, String strkey) {
        int nLen = strSource.length() / 2;

        byte[] cSource = new byte[nLen];
        byte[] cKey = new byte[16];
        Utils.ASC2BCD(strSource.getBytes(), cSource, nLen);
        Utils.ASC2BCD(strkey.getBytes(), cKey, 16);


        byte[] cKeyA = new byte[8];
        byte[] cKeyB = new byte[8];
        System.arraycopy(cKey, 0, cKeyA, 0, 8);
        System.arraycopy(cKey, 8, cKeyB, 0, 8);


        //
        byte[] cMac = new byte[8];
        byte[] cMac2 = new byte[8];
        Utils.memset(cMac, 0, 8);
        Utils.memset(cMac2, 0, 8);

        DesLib.DES_set_key_checked(cKeyA, 0x0A);
        DesLib.DES_set_key_checked(cKeyB, 0x0B);


        byte[] cData = new byte[8];
        int i = 0;
        while (i < nLen) {
            System.arraycopy(cSource, i, cData, 0, 8);

            for (int x = 0; x < 8; x++) {
                cMac2[x] = (byte) (((cMac[x] & 0xFF) ^ (cData[x] & 0xFF)) & 0xFF);
            }

            DesLib.DES_ecb_encrypt(cMac2, cMac, 0x0A, DES_ENCRYPT);

            i += 8;
        }

        DesLib.DES_ecb_encrypt(cMac, cMac2, 0x0B, DES_DECRYPT);
        DesLib.DES_ecb_encrypt(cMac2, cMac, 0x0A, DES_ENCRYPT);

        return Utils.toHexString(cMac).toUpperCase();
    }


    private String SecSelectFile(String strKey, String strMac, String strFID) {

        String strCmdHeader = AlignString("0CA4020C", "80");
        String strStuff = AlignString(strFID, "80");

        String strStruff3DES = TDES_Encrypt(strStuff, Utils.hexStringTobyte(strKey), DES_ENCRYPT);
        String strDO87 = "870901" + strStruff3DES;
        String strM = strCmdHeader + strDO87;

        String strNextSSC = GetNextSSC();

        String strN = strNextSSC + strM;
        strN = AlignString(strN, "80");

        String strCC = TDES_MAC(strN, strMac);

        String strDO8E = "8E08" + strCC;

        int nLen = strDO87.length() + strDO8E.length();
        String strLen = String.format("%02x", nLen / 2);
        String strAPDU = "0CA4020C" + strLen + strDO87 + strDO8E + "00";


        return strAPDU;
    }


    String SecReadBin(String strMac, int readlen, int offset) {

        String strOffset, strCmdHeader, strLen;
        strOffset = String.format("%04x", offset);
        strLen = String.format("%02x", readlen);

        strCmdHeader = String.format("0CB0%s", strOffset);

        String strCmdHeaderA = AlignString(strCmdHeader, "80");
        String strD097 = "9701" + strLen;
        String strM = strCmdHeaderA + strD097;
        String strNextSSC = GetNextSSC();
        String strN = strNextSSC + strM;
        strN = AlignString(strN, "80");


        String strCC = TDES_MAC(strN, strMac);
        String strDO8E = "8E08" + strCC;


        int nLen = strD097.length() + strDO8E.length();
        strLen = String.format("%02x", nLen / 2);
        strLen = strLen.toUpperCase();

        String strAPDU = strCmdHeader + strLen + strD097 + strDO8E + "00";

        return strAPDU;
    }

    String GetDO(String strDO, String strValue) {
        String strName = strDO.substring(0, 2);
        String strLen = strDO.substring(2, 4);

        int nLen = Utils.AscToInt(strLen);
        int nOffset = 4;

        if (strName.substring(0, 2).equals("87")) {
            nOffset += 2;
            nLen -= 1;
        }

        strValue = strDO.substring(nOffset, nOffset + nLen * 2);
        return strValue;
    }

    int ReadBinAuth(String strRAPDU, int nOffset, int nLen) {
        int nRLen = strRAPDU.length();
        if (nRLen < 32) return -1;


        String strRAPDU_D087 = strRAPDU.substring(0, nRLen - 8 - 20 - 4);
        String strRAPDU_D099 = strRAPDU.substring(nRLen - 8 - 20 - 4, (nRLen - 8 - 20 - 4) + 4 * 2);
        String strRAPDU_D08E = strRAPDU.substring(nRLen - 8 - 20 - 4 + 8, (nRLen - 8 - 20 - 4 + 8) + 10 * 2);

        String strNextSSC = GetNextSSC();
        String strN = strNextSSC + strRAPDU_D087 + strRAPDU_D099;
        strN = AlignString(strN, "80");
        String strCC = TDES_MAC(strN, m_StrKSmac);

        String strRAPDU_D08E_Value = "";
        strRAPDU_D08E_Value = GetDO(strRAPDU_D08E, strRAPDU_D08E_Value);

        if (!strRAPDU_D08E_Value.substring(0, 16).equals(strCC.substring(0, 16)))
            return -1;

        String strRAPDU_D087_Value = "";
        strRAPDU_D087_Value = GetDO(strRAPDU_D087, strRAPDU_D087_Value);

        String strBin = TDES_Encrypt(strRAPDU_D087_Value, Utils.hexStringTobyte(m_StrKSenc), DES_DECRYPT);
        m_strLastErrInfo = strBin.substring(0, nLen * 2);


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

    private int onSRFEX(byte[] buf_recv, int nFileID) {
        int cot_recv_pos = 0;
        String strBin = "";
        int nOffset = 0;
        int nLen = 0;
        int nReadLen, nRes;


        String strRes = "";
        String strAPDU = SecSelectFile(m_StrKSenc, m_StrKSmac, strTagFid[nFileID]);

        strRes = sendAPDU(strAPDU);
        GetNextSSC();

        if (strRes.equals("")) {
            return -1;
        }

        //
        strAPDU = SecReadBin(m_StrKSmac, 4, nOffset);

        if (!Simulator) {
            strRes = "8709019FF0EC34F9922651990290008E08AD55CC17140B2DED9000";
        } else {

            strRes = sendAPDU(strAPDU);

            if (strRes.equals("")) {
                return -1;
            }
        }

        nLen = ReadBinAuth(strRes, nOffset, 4);
        nOffset += 4;

        if (0 > nLen || 0 == nLen) {
            return -1;//return("Binary读取失败：" + m_strLastErrInfo);
        }

        if (nLen > 0) {
            //
            System.arraycopy(Utils.hexStringToBytes(m_strLastErrInfo), 0, buf_recv, cot_recv_pos, m_strLastErrInfo.length() / 2);
            cot_recv_pos += m_strLastErrInfo.length() / 2;
        }


        while (nLen > 0) {
            if (nLen > MAXCHUNK) {
                nReadLen = MAXCHUNK;
            } else {
                nReadLen = nLen;
            }

            strAPDU = SecReadBin(m_StrKSmac, nReadLen, nOffset);
            if (!Simulator) {
                strRes = "871901FB9235F4E4037F2327DCC8964F1F9B8C30F42C8E2FFF224A990290008E08C8B2787EAEA07D749000";
            } else {

                strRes = sendAPDU(strAPDU);

                if (strRes.equals("")) {
                    return -1;
                }
            }

            //
            nRes = ReadBinAuth(strRes, nOffset, nReadLen);
            if (0 > nRes) {
                return -1;
            } else {

                System.arraycopy(Utils.hexStringTobyte(m_strLastErrInfo), 0, buf_recv, cot_recv_pos, m_strLastErrInfo.length() / 2);
                cot_recv_pos += m_strLastErrInfo.length() / 2;
            }

            nOffset += nReadLen;
            nLen -= nReadLen;
        }

        return cot_recv_pos;
    }


    String sendAPDU(String strAPDU) {
        byte[] sw = {0x00, 0x00};

        byte[] response = new byte[MAX_PATH];
        int responseLen = response.length;

        byte[] apdu = Utils.hexStringToBytes(strAPDU);
        int apduLen = apdu.length;

        int ret = Transmit(apdu, apduLen, response, responseLen, sw);
        if (0x90 == (sw[0] & 0xFF)) {
            if (ret > 0) {
                return Utils.byte2HexStr(response, 0, ret) + Utils.toHexString(sw);
            } else {
                return Utils.toHexString(sw);
            }
        }

        return "";
    }


    //函数说明:获取下一个SSC
    public String GetNextSSC() {
        //
        long llTrun = 0;
        byte[] cSSCIn = new byte[9];
        byte[] cSSCOut = new byte[9];

        System.arraycopy(Utils.hexStringTobyte(m_StrSSC), 0, cSSCIn, 0, 8);

        //
        Utils.TurnChar(cSSCIn, cSSCOut, 8);

        //
        long tmp = 0x00;
        for (int t = 7; t >= 0; t--) {
            tmp = (cSSCOut[t] & 0xFF);
            llTrun += (tmp << (t * 8));
        }

        llTrun++;


        //
        for (int t = 0; t < 8; t++) {
            tmp = 0xFF;
            tmp = llTrun & (tmp << (t * 8));
            cSSCIn[t] = (byte) (tmp >> (t * 8));
        }

        Utils.TurnChar(cSSCIn, cSSCOut, 8);

        //
        m_StrSSC = Utils.byte2HexStr(cSSCOut, 0, 8);

        return m_StrSSC;
    }


    //函数说明:3DES加密/解密
    public String TDES_Encrypt(String strSource, byte[] szkey, int nType) {
        byte[] block_key = new byte[9];

        int nSourceLen = strSource.length() / 2;

        byte[] key = new byte[LEN_OF_KEY];
        int key_len = 16;

        byte[] cKey = new byte[LEN_OF_KEY];
        System.arraycopy(szkey, 0, cKey, 0, key_len);


        System.arraycopy(cKey, 0, key, 0, key_len);
        System.arraycopy(cKey, 0, key, key_len, LEN_OF_KEY - key_len);

        //
        Utils.memset(block_key, 0, block_key.length);
        System.arraycopy(key, 0, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0A);

        System.arraycopy(key, 8, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0B);

        System.arraycopy(key, 16, block_key, 0, 8);
        DesLib.DES_set_key_unchecked(block_key, 0x0C);


        int nLen = strSource.length();

        byte[] cSource = new byte[nLen / 2];

        System.arraycopy(Utils.hexStringTobyte(strSource), 0, cSource, 0, nLen / 2);

        byte[] cOut = new byte[nLen / 2 + 1];
        cOut[nLen / 2] = 0x00;

        int i = 0;
        int count = nLen / 2 / 8;

        byte[] IV = new byte[8];
        Utils.memset(IV, 0, 8);
        DesLib.DES_ede3_cbc_encrypt(cSource, cOut, nSourceLen, IV, nType);

        String ret_str = Utils.toHexString(cOut);
        return ret_str.substring(0, ret_str.length() - 2).toUpperCase();
    }


    //外部接口
    private int doAuth(String mrz2) {
        m_StrMRZ2 = mrz2;
        GetKeys();

        int Ret = SelectMFbyName();
        if (0 != Ret) {
            return 3;
        }


        //卡随机数
        String strRndIcc = GetCardRandom(8);
        if (strRndIcc.equals("")) {
            return 4;
        }


        //构建数据
        String strCmdData = GetCmdData(strRndIcc);
        if (strCmdData.equals("")) {
            return 4;
        }


        //发送验证 Authenticate
        String strRes = Authenticate(strCmdData);
        if (strRes.equals("")) {
            return 4;
        }


        if (!AuthRespData(strRes)) {
            return 4;
        }

        return 0;
    }


    public int EMP_ReadPassport_Auth(String mrz2) {
        return doAuth(mrz2);
    }

    public int EMP_ReadPassport_Read(byte[] buf, int bufLen, int nFileID) {

        int ret = ReadFile(buf, bufLen, nFileID);

        return ret;
    }


}
