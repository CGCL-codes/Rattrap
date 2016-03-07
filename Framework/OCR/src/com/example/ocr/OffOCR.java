package com.example.ocr;

import org.jason.lxcoff.lib.Remoteable;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OffOCR extends Remoteable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static ClassLoader mCurrent;
	public static DexClassLoader mCurrentDexLoader = null;
	 // myBitmap1是要被序列化的对象  
    private MyBitmap myBitmap = null; 
    public ScanResult result = new ScanResult();
    transient private static String TAG = "OffOCR";
	transient private ExecutionController controller;
	
	public OffOCR (ExecutionController controller){
		this.controller = controller;
	}
	
	public TessOCR getOCR(){
		TessOCR ocrc = new TessOCR();
		return ocrc;
	}
	
	public ScanResult doOCR(Bitmap bitmap){
		Method toExecute;		
		myBitmap = new MyBitmap(BytesBitmap.getBytes(bitmap), "SourceImage");  
		
		Class<?>[] paramTypes = {MyBitmap.class};
		Object[] paramValues = {myBitmap};

		long starttime = System.nanoTime();
		try {
			toExecute = this.getClass().getDeclaredMethod("localgetOCR", paramTypes);
			result.ocrResult = (String) controller.execute(toExecute, paramValues, this);
		} catch (SecurityException e) {
			// Should never get here
			e.printStackTrace();
			throw e;
		} catch (NoSuchMethodException e) {
			// Should never get here
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result.costTime = (System.nanoTime()-starttime)/1000000;
		Log.e(TAG,"Cost :"+ result.costTime +"ms");
		return result;
	}
	
	public boolean fileIsLocal(){
        try{
                File f=new File(ControlMessages.FILE_NOT_OFFLOADED);
                if(!f.exists()){
                        return false;
                }
                
        }catch (Exception e) {
                // TODO: handle exception
                return false;
        }
        return true;
	}
	
	
	public String localgetOCR(MyBitmap myBitmap){
		Log.d(TAG, "Execution localgetOCR");		
		
		String result =null;
		String apkFilePath = ControlMessages.CONTAINER_APK_DIR +  "com.example.ocr.apk";
				
		if(!fileIsLocal()){
		try {		
			File dexFile = new File(apkFilePath);
		    mCurrent = ClassLoader.getSystemClassLoader();
		      
			if (mCurrentDexLoader == null)
				mCurrentDexLoader = new DexClassLoader(dexFile.getAbsolutePath(),
						 Environment.getExternalStorageDirectory().getPath() , null, mCurrent);
			else
				mCurrentDexLoader = new DexClassLoader(dexFile.getAbsolutePath(),
						 Environment.getExternalStorageDirectory().getPath() , null, mCurrentDexLoader);
		    
			
		    Class<?> cls = mCurrent.loadClass("com.googlecode.tesseract.android.TessBaseAPI");  
			 Method init = cls.getDeclaredMethod("init", String.class,String.class);
			 Method getUTF8Text = cls.getDeclaredMethod("getUTF8Text",Bitmap.class);
			 
			 try {
				 Object Tess = cls.newInstance();
				 String datapath = ControlMessages.CONTAINER_APK_DIR + "/tesseract/";
		 		 String language = "eng";			 
				 
		 		 Bitmap bitmap = BytesBitmap.getBitmap(myBitmap.getBitmapBytes());
		 		 
		 		 init.invoke(Tess, datapath,language);
		         result = (String) getUTF8Text.invoke(Tess, bitmap);
		         
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 			 
			 
	         
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         //参数类型
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
		}
		
		else{
			TessOCR ocrc = new TessOCR();
			Bitmap bitmap = BytesBitmap.getBitmap(myBitmap.getBitmapBytes());
			result = ocrc.getOCRResult(bitmap);
			ocrc.onDestroy();
		}
		
		return result;
	}
	
	
	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
