
//com/example/hellojni
package com.example.jy.demo.passport;

public class CallDecoder{
	public native static int DecodeMj2Data(byte[] inDataArray, int nDataLength, String outfilename);
//	public native static int DecodeMj2Data(char[] inDataArray, String outfilename);
	public native static int Bmp2Pgm(String bmpfilename, String pgmfilename);
	public native static int Bmp2Bmp(String bmpfilename, String bmp256_360);
	
    //�����lib��ȥ��ǰ���lib
    static{
        System.loadLibrary("jp2dec");
    }
}