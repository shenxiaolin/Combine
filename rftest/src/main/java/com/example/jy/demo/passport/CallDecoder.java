package com.example.jy.demo.passport;

public class CallDecoder {
    public native static int DecodeMj2Data(byte[] inDataArray, int nDataLength, String outfilename);

    //	public native static int DecodeMj2Data(char[] inDataArray, String outfilename);
    public native static int Bmp2Pgm(String bmpfilename, String pgmfilename);

    public native static int Bmp2Bmp(String bmpfilename, String bmp256_360);

    //导入的lib名去掉前面的lib
    static {
        System.loadLibrary("jp2dec");
    }
}
