package com.opencv;

import android.util.Log;

public class LibImgFun {  
 
static {  
 
        System.loadLibrary("ImgFun");  
 
    }  

	public native static  int mySaveImage(int[] buf, int w, int h , String filename);
	//public native static  int[] getDirection(int[] buf,int HorLevel,int VerLeve);

}
