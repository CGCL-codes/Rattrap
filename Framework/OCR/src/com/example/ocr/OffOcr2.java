package com.example.ocr;

import java.io.File;
import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remoteable;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class OffOcr2 extends Remoteable{
	
    transient private static String TAG = "OffOCR";
	transient private ExecutionController controller;
	
	transient public static ClassLoader mCurrent;
	transient public static DexClassLoader mCurrentDexLoader = null;
	
	public OffOcr2 (ExecutionController controller){
		this.controller = controller;
	}
	
	public String DoOCR(String fileName){
		Method toExecute;
		Class<?>[] paramTypes = {String.class};
		Object[] paramValues = {fileName};
		
		String result = null;
		
		long starttime = System.nanoTime();
		try {
			toExecute = this.getClass().getDeclaredMethod("localDoOCR", paramTypes);
			// I need to send file first, so invoke the 4-params-version execute
			result = (String) controller.execute(toExecute, paramValues, this, fileName);
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
		
		long dura = System.nanoTime()-starttime;
		Log.d(TAG, "OCR Duration in OffOcr2 is " + dura/1000000 + "ms");

		return result;
	}
	
	@SuppressLint("NewApi")
	public String localDoOCR(String fileName){
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap imageBitmap = BitmapFactory.decodeFile(fileName, bfo);
		
		Log.d("OffOcr2","Start to run TessOCR");
		
		String result =null;
		/*if (!fileIsLocal()) {
			try {
				Log.d("OffOcr2", "Remote to run TessOCR");
				String apkFilePath = ControlMessages.CONTAINER_APK_DIR + "com.example.ocr.apk";
				File dexFile = new File(apkFilePath);
				mCurrent = ClassLoader.getSystemClassLoader();

				if (mCurrentDexLoader == null)
					mCurrentDexLoader = new DexClassLoader(
							dexFile.getAbsolutePath(), ControlMessages.DEX_OUT_PATH, null, mCurrent);
				else
					mCurrentDexLoader = new DexClassLoader(
							dexFile.getAbsolutePath(), ControlMessages.DEX_OUT_PATH, null, mCurrentDexLoader);

				Class<?> cls = mCurrentDexLoader.loadClass("com.googlecode.tesseract.android.TessBaseAPI");
				Method init = cls.getDeclaredMethod("init", String.class,String.class);
				Method setImage = cls.getDeclaredMethod("setImage", Bitmap.class);
				Method getUTF8Text = cls.getDeclaredMethod("getUTF8Text");

				try {
					Object Tess = cls.newInstance();
					String datapath = ControlMessages.CONTAINER_APK_DIR + "tesseract/";
					String language = "eng";
					init.invoke(Tess, datapath, language);
					setImage.invoke(Tess, imageBitmap);
					result = (String) getUTF8Text.invoke(Tess);

				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			TessOCR ocrc = new TessOCR();
			result = ocrc.getOCRResult(imageBitmap);
			ocrc.onDestroy();
		}

		return result;*/
		Log.d(TAG, "I'm starting new the TESSOCR object");
		TessOCR ocrc = new TessOCR();
		result = ocrc.getOCRResult(imageBitmap);
		ocrc.onDestroy();
		return result;
	}  

	public boolean fileIsLocal() {
		try {
			File f = new File(ControlMessages.FILE_NOT_OFFLOADED);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub
		
	}
}
