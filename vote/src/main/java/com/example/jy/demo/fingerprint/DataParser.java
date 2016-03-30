
//com/example/hellojni
package com.example.jy.demo.fingerprint;

public class DataParser{
	public native static int OpenCardFile(String outfilename);
	public native static int CloseCardFile();
	public native static int GetCSN(char[] strCSN);
	public native static int GetVIN(char[] strVIN);
	public native static int GetMAC(char[] strMAC);
	public native static int GetMAC2(char[] strMAC2);
	public native static int GetBirthDay(char[] strBirthDay);
	public native static int GetCODE(char[] strCODE);
	public native static int GetOCCUPATION(char[] strOCCUPATION);
	public native static int GetGENDER(char[] strGENDER);
	public native static int GetNAME(char[] strNAME);
	public native static int GetJP2IMAGE(String jp2FileName);
	public native static int GetFingerXYT(String xytFilePath);

	//导入的lib名去掉前面的lib
	static{
		System.loadLibrary("parser");
	}
}

//GetJP2IMAGE
//GetFingerXYT