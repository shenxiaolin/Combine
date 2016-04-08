package com.example.helper;

import java.io.ByteArrayOutputStream;


public class Utils {


    //16锟斤拷锟斤拷锟街凤拷转锟斤拷锟斤拷锟斤拷锟斤拷
    public static byte[] hexStringTobyte(String hex) {
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        String temp = "";
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
            temp += result[i] + ",";
        }
        // uiHandler.obtainMessage(206, hex + "=read=" + new String(result))
        // .sendToTarget();
        return result;
    }

    public static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }


    //锟斤拷锟斤拷转锟斤拷16锟斤拷锟斤拷锟街凤拷
    public static String toHexString(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }


    /**
     * 设置指定数组的值
     *
     * @param buf   指定的数组
     * @param value 要设置的值
     * @param size  指定数组的长度
     */
    public static void memset(byte[] buf, int value, int size) {
        for (int t = 0; t < size; t++) {
            buf[t] = (byte) value;
        }
    }

    /**
     * 比较两个数组指定长度范围内的元素是否相等
     *
     * @param ary_src 数组 1
     * @param ary_des 数组 2
     * @param len     比较的长度
     * @return true：相等；false：不相等
     */
    public static boolean memcmp(byte[] ary_src, byte[] ary_des, int len) {

        for (int i = 0; i < len; i++) {
            if (ary_src[i] != ary_des[i]) {
                return false;
            }
        }

        return true;
    }


    ///
    public static boolean ASC2BCD(byte[] strASC, byte[] strBCD, int lenBCD) {
        int i, nTemp, nP;
        boolean bOne = true;
        byte[] chrTemp = new byte[lenBCD * 2 + 2];

        nP = 0;
        //memcpy(chrTemp , strASC, lenBCD*2);

        //memcpy(chrTemp , strASC, strlen(strASC));
        System.arraycopy(strASC, 0, chrTemp, 0, strASC.length);


        for (i = 0; i < lenBCD * 2; i++) {
            nTemp = AscToInt((byte) (chrTemp[i] & 0xFF));
            if (nTemp < 16) {
                if (bOne) {
                    nTemp = nTemp * 16;
                    strBCD[nP] = (byte) nTemp;
                } else {
                    strBCD[nP] = (byte) ((strBCD[nP] & 0xFF) + nTemp);
                    nP = nP + 1;
                }
                bOne = !bOne;
            }
        }

        return true;
    }


    ///
    public static int AscToInt(byte cIn) {
        int nRet;
        if (cIn <= '9' && cIn >= '0')
            nRet = cIn - '0';
        else if (cIn <= 'f' && cIn >= 'a')
            nRet = cIn - 'a' + 10;
        else if (cIn <= 'F' && cIn >= 'A')
            nRet = cIn - 'A' + 10;
        else
            nRet = cIn + 16;//锟斤拷锟轿拷欠锟斤拷址锟斤拷锟斤拷锟斤拷锟斤拷锟�16

        return (nRet);
    }

    public static int AscToInt(String str) {
        int nLen = str.length();
        byte[] cTmp = str.getBytes();

        int nRet = 0;
        for (int i = 0; i < nLen; i++)
            nRet = nRet * 16 + AscToInt(cTmp[i]);

        return (nRet);
    }

    ///
    public static String byte2HexStr(byte[] b, int offset, int length) {
        String hs = "";
        String stmp = "";

        for (int n = offset; n < offset + length; ++n) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }

        return hs.toUpperCase();
    }


    public static byte[] hexStringToBytes(String hexString) {
        if ((hexString == null) || (hexString.equals(""))) {
            return null;
        }

        hexString = hexString.toUpperCase();

        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];

        for (int i = 0; i < length; ++i) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)]));
        }

        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static void TurnChar(byte[] cDataIn, byte[] cDataOut, int nLen) {
        for (int i = 0; i < nLen; i++)
            cDataOut[i] = cDataIn[nLen - i - 1];
    }

    public static boolean chrcmp(byte[] chr1, byte[] chr2, int nlen) {
        for (int i = 0; i < nlen; i++) {
            if (chr1[i] != chr2[i]) return false;
        }

        return true;
    }

    //
    public static String hexStrToAscStr(String bytes) {
        String hexString = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //锟斤拷每2位16锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷装锟斤拷一锟斤拷锟街斤拷
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }


}
