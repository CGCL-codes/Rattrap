package com.example.ocr;

import java.io.Serializable;

/** 
 * MyBitmap是要被序列化的类 
 * 其中包含了通过BytesBitmap类得到的Bitmap中数据的数组 
 * 和一个保存位图的名字的字符串，用于标识图片 
 * @author joran 
 * 
 */  
class MyBitmap implements Serializable {  
    /** 
     * serialVersionUID解释: 
     * http://www.blogjava.net/invisibletank/archive/2007/11/15/160684.html 
     */  
    private static final long serialVersionUID = 1L;  
    private byte[] bitmapBytes = null;  
    private String name = null;  
  
    public MyBitmap(byte[] bitmapBytes, String name) {  
        // TODO Auto-generated constructor stub  
        this.bitmapBytes = bitmapBytes;  
        this.name = name;  
    }  
  
    public byte[] getBitmapBytes() {  
        return this.bitmapBytes;  
    }  
  
    public String getName() {  
        return this.name;  
    }  
}  

