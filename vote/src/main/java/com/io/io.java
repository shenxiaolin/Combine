package com.io;


public class io{
	public native static int IoOpen();
	public native static int IoClose();

    //�����lib��ȥ��ǰ���lib
    static{
        System.loadLibrary("io");
    }
}
