
//com/example/hellojni
package com.example.jy.demo.passport;

public class CallFprint{
//    public native static int pgmChangeToXyt(char[] inFile, char[] outFile);
	public native static int pgmChangeToXyt(String inFile, String outFile);
    public native static int fprintCompare(String ScanXytFile, String RolledXytFile);

    //�����lib��ȥ��ǰ���lib
    static{
        System.loadLibrary("fprint");
    }
}