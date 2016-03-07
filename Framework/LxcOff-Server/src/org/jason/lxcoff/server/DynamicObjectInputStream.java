package org.jason.lxcoff.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import android.util.Log;
import dalvik.system.DexClassLoader;


/**
 * Custom object input stream to also deal with dynamically loaded classes. The
 * classes can be retrieved from Android Dex files, provided in Apk (android
 * application) files.
 * 
 * @author Andrius
 * 
 */
public class DynamicObjectInputStream extends ObjectInputStream {

	public static ClassLoader mCurrent = ClassLoader.getSystemClassLoader();
	public static DexClassLoader mCurrentDexLoader = null;
	
	public static String currentApk = null;

	public DynamicObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Override the method resolving a class to also look into the constructed
	 * DexClassLoader
	 */
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		try {
			try {
				return mCurrent.loadClass(desc.getName());
			} catch (ClassNotFoundException e) {
				return mCurrentDexLoader.loadClass(desc.getName());
			}
		} catch (ClassNotFoundException e) {
			return super.resolveClass(desc);
		} catch (NullPointerException e) { // Thrown when currentDexLoader is
			// not yet set up
			return super.resolveClass(desc);
		}

	}
	
	protected Class<?> resolveClassName(String className){
		try {
			try {
				return mCurrent.loadClass(className);
			} catch (ClassNotFoundException e) {
				return mCurrentDexLoader.loadClass(className);
			}
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NullPointerException e) { // Thrown when currentDexLoader is
			// not yet set up
			return null;
		}
		
	}

	/**
	 * Add a Dex file to the Class Loader for dynamic class loading for clients
	 * 
	 * @param apkFile
	 *            the apk package
	 */
/*	public void addDex(final File apkFile) {
		if (mCurrentDexLoader == null)
			mCurrentDexLoader = new DexClassLoader(apkFile.getAbsolutePath(),
					apkFile.getParentFile().getAbsolutePath(), null, mCurrent);
		else
			mCurrentDexLoader = new DexClassLoader(apkFile.getAbsolutePath(),
					apkFile.getParentFile().getAbsolutePath(), null,
					mCurrentDexLoader);

	}*/
	public void addDex(final File apkFile, String appName) {
		Log.d("DOIS", "dexoutpath: " + ClientHandler.dexOutputDir);
/*		Log.d("DOIS", "currentAPK: " + DynamicObjectInputStream.currentApk);
		Log.d("DOIS", "want to run appName: " + appName);*/
		try{
			if (mCurrentDexLoader == null || !DynamicObjectInputStream.currentApk.equals(appName)){
				mCurrentDexLoader = new DexClassLoader(apkFile.getAbsolutePath(),
						ClientHandler.dexOutputDir, null, mCurrent);
				DynamicObjectInputStream.currentApk = new String(appName);
			}
			else{
				Log.d("DOIS", "Maybe I dont need a new ClassLoader. (ClientHandler tag for debug)");
/*				mCurrentDexLoader = new DexClassLoader(apkFile.getAbsolutePath(),
						ClientHandler.dexOutputDir, null,
						mCurrentDexLoader);*/
			}
			
		} catch (NullPointerException e){
			Log.d("DOIS", "Error" + e.getMessage());
		}

	}

}
