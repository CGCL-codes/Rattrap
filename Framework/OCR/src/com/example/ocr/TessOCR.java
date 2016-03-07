package com.example.ocr;

import java.io.File;

import org.jason.lxcoff.lib.ControlMessages;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class TessOCR {
	private TessBaseAPI mTess;
	
	public TessOCR() {
		// TODO Auto-generated constructor stub
		Log.d("TessOCR","Start to run TessOCR");
		mTess = new TessBaseAPI();
		String datapath = ControlMessages.CONTAINER_APK_DIR  + "tesseract/";
		String language = "eng";
		File dir = new File(datapath + "tessdata/");
		if (!dir.exists()) 
			dir.mkdirs();
		mTess.init(datapath, language);
	}
	
	public String getOCRResult(Bitmap bitmap) {
		
		mTess.setImage(bitmap);
		String result = mTess.getUTF8Text();

		return result;
    }
	
	public void onDestroy() {
		if (mTess != null)
			mTess.end();
	}
	
}
