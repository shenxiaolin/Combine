package com.io;


public class io{
	public native static int IoOpen();
	public native static int IoClose();

    //导入的lib名去掉前面的lib
    static{
        System.loadLibrary("io");
    }
}
