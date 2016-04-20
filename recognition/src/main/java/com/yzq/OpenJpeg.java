package com.yzq;

/**
 * Created by YZQ on 2016/4/13.
 */
public class OpenJpeg {
    public native String GetLibVersion();
    public native String CompressImageToJ2K(String FilePathAndName);/*yzq: 压缩,将普通图像格式转换为.j2k */
    public native String DecompressJ2KtoImage(String FilePathAndName);/*yzq: 解压,将j2k图像格式转换为普通图像 */

    static {
        System.loadLibrary("jniopenjp2");
    }
}
