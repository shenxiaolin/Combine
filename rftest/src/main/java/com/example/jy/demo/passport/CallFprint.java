
//com/example/hellojni
package com.example.jy.demo.passport;

public class CallFprint{
//    public native static int pgmChangeToXyt(char[] inFile, char[] outFile);
	public native static int pgmChangeToXyt(String inFile, String outFile);
    public native static int fprintCompare(String ScanXytFile, String RolledXytFile);

    //导入的lib名去掉前面的lib
    static{
        System.loadLibrary("fprint");
    }
}