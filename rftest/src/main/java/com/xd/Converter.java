//------------------------------------------------------------------------------
//                         COPYRIGHT 2011 GUIDEBEE
//                           ALL RIGHTS RESERVED.
//                     GUIDEBEE CONFIDENTIAL PROPRIETARY
///////////////////////////////////// REVISIONS ////////////////////////////////
// Date       Name                 Tracking #         Description
// ---------  -------------------  ----------         --------------------------
// 13SEP2011  James Shen                 	          Initial Creation
////////////////////////////////////////////////////////////////////////////////
//--------------------------------- PACKAGE ------------------------------------
package com.xd;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


//[------------------------------ MAIN CLASS ----------------------------------]
//--------------------------------- REVISIONS ----------------------------------
//Date       Name                 Tracking #         Description
//--------   -------------------  -------------      --------------------------
//13SEP2011  James Shen                 	         Initial Creation
////////////////////////////////////////////////////////////////////////////////

/**
 * Convert help class.
 * <hr>
 * <b>&copy; Copyright 2011 Guidebee, Inc. All Rights Reserved.</b>
 *
 * @author Guidebee Pty Ltd.
 * @version 1.00, 13/09/11
 */
public class Converter {

    // Hex help
    private static final byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
            (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'};

    ////////////////////////////////////////////////////////////////////////////
    //--------------------------------- REVISIONS ------------------------------
    // Date       Name                 Tracking #         Description
    // ---------  -------------------  -------------      ----------------------
    // 13SEP2011  James Shen                 	          Initial Creation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * convert a byte arrary to hex string
     *
     * @param raw byte arrary
     * @param len lenght of the arrary.
     * @return hex string.
     */
    public static String getHexString(byte[] raw, int len) {
        byte[] hex = new byte[2 * len];
        int index = 0;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex);
    }

    /**
     * convert a Asc byte arrary to Hex byte arrary, ex. [0x00, 0x00, 0x34, 0x4E] -> [4E]
     *
     * @param raw Asc byte arrary
     * @param len lenght of the arrary.
     * @return hex byte arrary.
     */
    public static int AscToHex(int raw) {
        int value = 0;

        int v = (raw & 0xFF00) >> 8;
        if (v >= '0' && v <= '9') {
            v -= '0';
        } else if (v >= 'A' && v <= 'F') {
            v -= '7';
        } else {
            Log.i("AscToHex", "**** asc num illegal ****");
        }
        value = v << 4;

        v = (raw & 0x00FF);
        if (v >= '0' && v <= '9') {
            v -= '0';
        } else if (v >= 'A' && v <= 'F') {
            v -= '7';
        }
        value |= v;
        return (value);
    }

    /**
     * ��int���͵�����ת��Ϊbyte����
     *
     * @param n int����
     * @return ���ɵ�byte����
     */
    public static byte[] intToBytes(int n) {
        String s = String.valueOf(n);
        return s.getBytes();
    }

    /**
     * ��byte����ת��Ϊint����
     *
     * @param b �ֽ�����
     * @return ���ɵ�int����
     */
    public static int bytesToInt(byte[] b) {
        String s = new String(b);
        return Integer.parseInt(s);
    }

    /**
     * ��int���͵�����ת��Ϊbyte����
     * ԭ����int�����е��ĸ�byteȡ�����ֱ�洢
     *
     * @param n int����
     * @return ���ɵ�byte����
     */
    public static byte[] intToBytes2(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }

    /**
     * ��byte����ת��Ϊint����
     *
     * @param b �ֽ�����
     * @return ���ɵ�int����
     */
    public static int byteToInt2(byte[] b) {
//	  return (((int)b[0]) << 24) + (((int)b[1]) << 16) + (((int)b[2]) << 8) + b[3];
//	  return (( (int)b[0]) << 8) + b[1];
        int n = (int) b[0];
        Log.i("Converter", "n=" + n);
//	  return ((int)b[0]);
        return ((int) b[0]);
    }

//	static public void sprintf (StringBuffer result, String format, String...replace)
//	{
//		result.append(String.format(format, replace));
//	}

    ////////////////////////////////////////////////////////////////////////////
    //--------------------------------- REVISIONS ------------------------------
    // Date       Name                 Tracking #         Description
    // ---------  -------------------  -------------      ----------------------
    // 13SEP2011  James Shen                 	          Initial Creation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * calculate the MD5 hash value.
     *
     * @param input input bytes
     * @return MD5 hash value.
     * @throws NoSuchAlgorithmException
     */
    public static byte[] calculateHash(byte[] input)
            throws NoSuchAlgorithmException {
        MessageDigest digester = MessageDigest.getInstance("MD5");
        digester.update(input, 0, input.length);
        byte[] digest = digester.digest();
        return digest;
    }


    // char[] ת byte[]
    public byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        //��������
        cb.put(chars);
        //��������λ�� ��������
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    // byte[] ת char[]
    public char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    //byte[] ����ת 16�����ַ���
    public static String printHexString(byte[] b) {
        String result = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                //���ֽڵ�����ǰ�油 "0"
                hex = '0' + hex;
            }
            result = result + hex.toUpperCase();
        }
        return result;
    }

    //byte[] ����ת ָ�����ȵ�16�����ַ���, ���� bLen С�ڵ��� byte�����ԭ����С
    //��ú�����Ӧ���� hexStringToBytes()
    public static String printHexLenString(byte[] b, int bLen) {
        String result = "";

        for (int i = 0; i < bLen; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                //���ֽڵ�����ǰ�油 "0"
                hex = '0' + hex;
            }
            result = result + hex.toUpperCase();
        }
        return result;
    }
    //���ַ��� hex ��չ�� size ��С, ��߲��㲿������ַ� c
//	public static String leftPad(String hex, char c, int size) {
//		char[] cs = new char[size];
//		Arrays.fill( cs, c);
//		//�ַ���תchar[] ������ char[] cs
//		System.arraycopy(hex.toCharArray(), 0, cs, cs.length-hex.length(), hex.length());
//		//char[] תΪ String
//		return new String(cs);
//	}


    //byte[] ����ת 16�����ַ��� ---> ������ printHexString() / bytesToHexString() ����
//	public static String toHexString(byte[] data) {
//		StringBuilder buf = new StringBuilder();
//		//���ȡ�� data[] Ԫ�ر��浽 b
//		for (byte b : data) {
//			buf.append(leftPad( Integer.toHexString(b&0xff), '0', 2));
//		}
//		//StringBuilder ת �ַ���
//		return buf.toString();
//	}	


    //char ת byte
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    //16�����ַ���ת byte[] ����
    //��ú�����Ӧ����     printHexString() or printHexLenString()
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        if ((hexString.length() % 2) != 0) {//�������ַ�, �Ҳ�"0" �ճ�ż���ֽ�
            length += 1;
            hexString = hexString + "0";
        }
        Log.i("CommonFunc", "CommonFunc lenth=" + length);
        //�ַ���ת char[] ����
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        int i, pos;
        for (i = 0; i < length; i++) {
            pos = i * 2;
            Log.i("CommonFunc", "CommonFunc i=" + i + " pos=" + pos);
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    //byte[] �������ӽ���
    public static int dataEncDec(byte[] b, int v) {
        int nRet = 0;

        for (int i = 0; i < b.length; i++) {
            if (b[i] != 0 && b[i] != (byte) v) {
                b[i] ^= v;
            }
        }
        return nRet;
    }

    /**
     * short型数据转化为byte[]数组
     */
    public static byte[] short2ByteArray(short data) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((data >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * byte[]数组转换为short
     */
    public static short byteArray2Short(byte[] bytes) {
        return (short) (bytes[1] & 0xff | (bytes[0] & 0xff) << 8);
    }

    /**
     * hex转换为String
     */
    public static String hex2String(byte[] b, int bLen) {
        String result = "";

        for (int i = 0; i < bLen; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                //半字节的情形前面补 "0"
                hex = '0' + hex;
            }
            result = result + hex.toUpperCase();
        }
        return result;
    }
}
