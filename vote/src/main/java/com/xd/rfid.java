
//com/xd/rfid/funcs
package com.xd;

public class rfid{
	public native static int samOpen(int sLot);	//sLot = 1,2,3,4
	//samNum=1,2,3,4  	samBaud=9600, 19200, 38400, 115200  	samVol=(1=1.8v  2=3.3V  3=5V)		echoATR: give a max input buffer 64
	public native static int samReset(byte samNum, int samBaud, int samVol, byte[] echoATR, byte[] echoATRlen);	//"(BII[B[B)I"
    //cosResponse: give a max input buffer = 255
//	public native static int samApdu(byte samNum, byte[] cosCmd, short cosCmdLen, byte[] cosResponse, short[] cosResLenth, short[] cosSW );	//"(B[BS[B[S[S)I"
	public native static int samApdu(byte samNum, byte[] cosCmd, short cosCmdLen, byte[] cosResponse, short[] cosResLenth, byte[] cosSW );	//"(B[BS[B[S[S)I"

	public native static int RFIDModuleOpen();	//bIndex fixed to 1
	public native static int RFIDMoudleClose();
	public native static int RFIDInit();
	
	public native static int RFIDTypeSet(int type_mode);
	
	public native static int RFIDGetSNR(int mode, byte[] bLen, byte[] bSNR);
	public native static int RFIDTypeARats(int cid, byte[] bResp);
	//cid --- fixed to 0 	bOutData --- : give a max input buffer = 255
	public native static int RFIDRfApdu(int cid, byte[] bInData, int inLen, byte[] bOutData, byte[] bOutLen, byte[] bSw);
	//cid --- fixed to 0 	bOutData --- : give a max input buffer = 255
	public native static int RFIDDesFireApdu(int cid, byte[] bInData, int inLen, byte[] bOutData, byte[] bOutLen);
	
	public native static int RFIDFelicaTransceive(int timeout, byte[] bInData, int inLen, byte[] bOutData, byte[] bOutLen);
	
	//keyAB: 0x0A -> keyA, 0x0B -> keyB; secNo: based from 0
	public native static int MifAuthen(byte keyAB, byte secNo, byte[] bKey, byte[] bSNR);
	public native static int MifRead(int blockNo, byte[] bOutData);
	public native static int MifWrite(int blockNo, byte[] bInData);
	public native static int MifChange(int subcommand,int blockNo, byte[] bInData);
	public native static int MifTransfer(int blockNo);
	
	public native static int PiccREQB(int afi, int param,byte[] bOutData,byte[] bOutLen );
	public native static int PiccAttrib(byte[] uid, int cid,byte[] bOutData,byte[] bOutLen );
	
	public native static int RFIDRfOpen();
	public native static int RFIDRfClose();

	
    //导入的lib名去掉前面的lib
    static{
        System.loadLibrary("emp5500drv");
    }
}