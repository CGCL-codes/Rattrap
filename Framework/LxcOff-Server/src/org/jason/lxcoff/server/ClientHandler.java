package org.jason.lxcoff.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jason.lxcoff.lib.ControlMessages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * The server-side class handling client requests for invocation.
 * Works in a separate thread for each client.
 * This can handle requests coming from the phone (when behaving as the main clone)
 * or requests coming from the main clone when behaving as a clone helper. 
 */
public class ClientHandler {

	private static final String 	TAG = "ClientHandler";

	private int						cloneId = -1;					// The main thread has cloneId == -1
	// the clone helpers have cloneId \in [0, nrClones-2]
	private final Socket 			mClient;
	private final Context 			mContext;
	private final int 				BUFFER = 8192;

	static int 						numberOfCloneHelpers = 0;		// The number of clone helpers requested (not considering the main clone)
	private static boolean 			withMultipleClones = false;	// True if more than one clone is requested, False otherwise.

	static 	Boolean[] 				syncObject;						// Needed for synchronization with the clone helpers
	static	int 					requestFromMainServer 	= 0;	// The main clone sends commands to the clone helpers
	static 	String 					appName;						// the app name sent by the phone
	static 	Object 					objToExecute = new Object();	// the object to be executed sent by the phone
	static 	String 					methodName;						// the method to be executed
	static 	Class<?>[] 				pTypes;							// the types of the parameters passed to the method
	static 	Object[] 				pValues;						// the values of the parameteres to be passed to the method
	private Class<?> 				returnType;						// the return type of the method
	static	String 					apkFilePath;					// the path where the apk is installed

	static	Object					responsesFromServers;			// array of partial results returned by the clone helpers

	static 	AtomicInteger 			nrClonesReady = new AtomicInteger(0); // The main thread waits for all the clone helpers to finish execution 
	public static String			dexOutputDir = null;
	
	private static String logFileName = "/mnt/sdcard/nqueens_server.txt";
	private static FileWriter logFileWriter;

	public ClientHandler(Socket pClient, final Context cW) {
		Log.d(TAG, "New Client connected");
		this.mClient = pClient;
		this.mContext = cW;
		//this.config = config;
		this.startNewLog();
		dexOutputDir = this.mContext.getDir("dex", 0).getAbsolutePath();
		
		if(! CloneThread.inuse){
		//if(true){
			CloneThread.inuse = true;
			
			Executer communicator;
			communicator = new Executer();
			communicator.start();	
		}else{
			try {
				this.mClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Method to retrieve an apk of an application that needs to be executed
	 * 
	 * @param objIn
	 *            Object input stream to simplify retrieval of data
	 * @return the file where the apk package is stored
	 * @throws IOException
	 *             throw up an exception thrown if socket fails
	 */
	private File receiveApk(DynamicObjectInputStream objIn, String apkFilePath)
			throws IOException {
		// Receiving the apk file
		// Get the length of the file receiving
		int apkLen = objIn.readInt();
		Log.d(TAG, "Read apk len - " + apkLen);

		// Get the apk file
		byte[] tempArray = new byte[apkLen];
		Log.d(TAG, "Read apk");
		long startTime = System.nanoTime();
		objIn.readFully(tempArray);
		long dura = System.nanoTime() - startTime;

		startTime = System.nanoTime();
		// Write it to the filesystem
		File dexFile = new File(apkFilePath);
		FileOutputStream fout = new FileOutputStream(dexFile);

		BufferedOutputStream bout = new BufferedOutputStream(fout, BUFFER);
		bout.write(tempArray);
		bout.close();
		dura = System.nanoTime() - startTime;

		return dexFile;
	}
	
	/* This is For test I/O and bandwidth*/
	/* private File receiveApk(DynamicObjectInputStream objIn, String apkFilePath)
			throws IOException {
		// Receiving the apk file
		// Get the length of the file receiving
		int apkLen = objIn.readInt();
		Log.d(TAG, "Read apk len - " + apkLen);

		// Get the apk file
		byte[] tempArray = new byte[apkLen];
		Log.d(TAG, "Read apk");
		long startTime = System.nanoTime();
		objIn.readFully(tempArray);
		long dura = System.nanoTime() - startTime;
		Log.d(TAG, "Received apk costs " + dura/1000000 + "ms. The data transfer rate is " + apkLen/(dura/1000000) + " bytes/ms");

		startTime = System.nanoTime();
		// Write it to the filesystem
		File dexFile = new File(apkFilePath);
		FileOutputStream fout = new FileOutputStream(dexFile);

		BufferedOutputStream bout = new BufferedOutputStream(fout, BUFFER);
		for(int i=0; i<100; i++){
			bout.write(tempArray);
		}

		bout.close();
		dura = System.nanoTime() - startTime;
		Log.d(TAG, "Write apk costs " + dura/1000000 + "ms. The I/O write rate is " + apkLen/1024*100/(dura/1000000) + " KB/ms");

		startTime = System.nanoTime();
		File testFile = new File("/mnt/sdcard/test");
		FileInputStream fin = new FileInputStream(testFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		int bytesRead = 0;
		byte[] buffer = new byte[1024];
		while ((bytesRead = bis.read(buffer)) != -1) {
		} 
		dura = System.nanoTime() - startTime;
		Log.d(TAG, "Read apk costs " + dura/1000000 + "ms. The I/O read rate is " + apkLen/1024*100/(dura/1000000) + " KB/ms");
		bis.close();
		
		
		return dexFile;
	}*/

	/**
	 * Extract native libraries for the x86 platform included in the .apk file
	 * (which is actually a zip file).
	 * 
	 * @param dexFile
	 *            the apk file
	 * @return the list of shared libraries
	 */

	@SuppressWarnings("unchecked")
	private LinkedList<File> addLibraries(File dexFile) {
		Long startTime = System.nanoTime();

		ZipFile apkFile;
		LinkedList<File> libFiles = new LinkedList<File>();
		try {
			apkFile = new ZipFile(dexFile);
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) apkFile
					.entries();
			ZipEntry entry;
			while (entries.hasMoreElements()) {
				entry = entries.nextElement();
				// Zip entry for a lib file is in the form of
				// lib/platform/library.so
				// But only load x86 libraries on the server side
				if (entry.getName().matches("lib/x86/(.*).so")) {
					Log.d(TAG, "Matching APK entry - " + entry.getName());
					// Unzip the lib file from apk
					BufferedInputStream is = new BufferedInputStream(apkFile
							.getInputStream(entry));

					File libFile = new File(mContext.getFilesDir()
							.getAbsolutePath()
							+ "/" + entry.getName().replace("lib/x86/", ""));
					// Create the file if it does not exist
					if (!libFile.exists()) {
						// Let the error propagate if the file cannot be created
						// - handled by IOException
						libFile.createNewFile();
						Log.d(TAG, "Writing lib file to "
								+ libFile.getAbsolutePath());
						FileOutputStream fos = new FileOutputStream(libFile);
						BufferedOutputStream dest = new BufferedOutputStream(fos,
								BUFFER);

						byte data[] = new byte[BUFFER];
						int count = 0;
						while ((count = is.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();
					}else{
						Log.d(TAG, "Lib file " + libFile.getAbsolutePath() + " exists");
					}

					is.close();

					// Store the library to the list
					libFiles.add(libFile);
				}
			}

		}
		catch (IOException e) {
			Log.d(TAG, "ERROR: File unzipping error " + e);
		} 
		Log.d(TAG, "Duration of unzipping libraries - "
				+ ((System.nanoTime() - startTime) / 1000000) + "ms");
		return libFiles;

	}

	/**
	 * Reads in the object to execute an operation on, name of the method to be
	 * executed and executes it
	 * 
	 * @param objIn
	 *            Dynamic object input stream for reading an arbitrary object
	 *            (class loaded from a previously obtained dex file inside an
	 *            apk)
	 * @return result of executing the required method
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws OptionalDataException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 */
	private Object retrieveAndExecute(DynamicObjectInputStream objIn,
			LinkedList<File> libraries) {
		Long startTime = System.nanoTime();
		// Read the object in for execution
		Log.d(TAG, "Read Object");
		Gson gson = new Gson();
		try {

			// Receive the number of clones needed
			/*numberOfCloneHelpers = objIn.readInt();
			Log.i(TAG, "The user is asking for " + numberOfCloneHelpers + " clones");
			numberOfCloneHelpers--;
			withMultipleClones = numberOfCloneHelpers > 0;*/
			
			String className = (String) objIn.readObject();
			
			Log.d(TAG, "Object class name : " + className);
			
			// Get the object
			String objStr = (String) objIn.readObject();
			
			objToExecute = gson.fromJson(objStr, objIn.resolveClassName(className));
			
			//objToExecute = fromString(strExecute);

			// Get the class of the object, dynamically
			Class<?> objClass = objToExecute.getClass();
			Log.d(TAG, "objClass name is " + objClass.getName());

			// Set up server-side ExecutionController for the object
			java.lang.reflect.Field fController = objClass.getDeclaredField("controller");
			fController.setAccessible(true);

			Class<?> controllerType = fController.getType();
			Constructor<?> cons = controllerType.getConstructor((Class[]) null);
			Object controller = null;
			try {
				controller = cons.newInstance((Object[]) null);
			} catch (InstantiationException e) {
				// too bad. still try to carry on.
				e.printStackTrace();
			}

			fController.set(objToExecute, controller);

			Log.d(TAG, "Read Method");
			// Read the name of the method to be executed
			methodName = (String) objIn.readObject();
			
			Log.d(TAG, "MethodName :" + methodName);
			
			Object tempTypesObj = objIn.readObject();
			String[] tempTypes = (String[]) tempTypesObj;
			pTypes = new Class<?>[tempTypes.length];
			for(int i = 0; i < tempTypes.length; i++){
				Log.d(TAG, "pTypes num " + i + " : " + tempTypes[i]);
				if(tempTypes[i].equals("int")){
					pTypes[i] = int.class;
				}else if(tempTypes[i].equals("long")){
					pTypes[i] = long.class;
				}else if(tempTypes[i].equals("boolean")){
					pTypes[i] = boolean.class;
				}else{
					pTypes[i] = objIn.resolveClassName(tempTypes[i]);
				}
		    }
			
			pValues = new Object[tempTypes.length];
			
			String tempValues = (String) objIn.readObject();
			Log.d(TAG, "tempValues : " + tempValues);
			JsonParser parser = new JsonParser();
			JsonArray jsonArray = parser.parse(tempValues).getAsJsonArray();
		    for (int i = 0; i < jsonArray.size(); i++) {
	            JsonElement el = jsonArray.get(i);
	            pValues[i]  = gson.fromJson(el, pTypes[i]);
	            Log.d(TAG, "pValues class " + i + " is " + pValues[i].getClass().getName());
	        }
			
			// Get the method to be run by reflection
			Method runMethod = objClass.getDeclaredMethod(methodName, pTypes);
			Log.d(TAG, "Runmethod name is " + runMethod.getName());
			
			// And force it to be accessible (quite often would be declared
			// private originally)
			runMethod.setAccessible(true); // Set the method to be accessible

			// Run the method and retrieve the result
			Object result = null;
			Long execDuration = null;
			try {
				Method libLoader = objClass.getMethod("loadLibraries", LinkedList.class);
				libLoader.invoke(objToExecute, libraries);
				Long startExecTime = System.nanoTime();
				result = runMethod.invoke(objToExecute, pValues);
				execDuration = System.nanoTime() - startExecTime;
				traceLog("" + execDuration/1000000);
				Log.d(TAG, runMethod.getName() + ": pure execution time - " + (execDuration / 1000000) + "ms");
			} catch (InvocationTargetException e) {
				// The method might have failed if the required shared library
				// had
				// not been loaded before, try loading the apk's libraries and
				// restarting the method
				if (e.getTargetException() instanceof UnsatisfiedLinkError) {
					Log
					.d(TAG, "UnsatisfiedLinkError thrown, loading libs and retrying");
					Method libLoader = objClass.getMethod("loadLibraries",
							LinkedList.class);
					try {
						libLoader.invoke(objToExecute, libraries);
						Long startExecTime = System.nanoTime();
						result = runMethod.invoke(objToExecute, pValues);
						execDuration = System.nanoTime() - startExecTime;
						traceLog("" + execDuration / 1000000);
						Log.d(TAG, runMethod.getName() + ": pure execution time - " + (execDuration / 1000000) + "ms");
					} catch (InvocationTargetException e1) {
						Log.d(TAG,"InvocationTargetException", e1.getTargetException());
						result = e1;
					}
				} else {
					Log.e(TAG, "InvocationError", e.getTargetException());
					result = e;
				}
			} catch (Exception e){
				Log.d(TAG, "invoke failed with error : " + e.toString());
			}

			Log.d(TAG, runMethod.getName() + ": retrieveAndExecute time - "
					+ ((System.nanoTime() - startTime) / 1000000) + "ms");
			
			Class<?> retClass = runMethod.getReturnType();
			String retClassName = retClass.getName();
			
			HashMap<String ,String> resMap = new HashMap<String ,String>(); 
			resMap.put("retType", retClassName);
			String resStr = gson.toJson(result);
			resMap.put("retVal", resStr);

			return resMap;
			
			// If this is the main clone send back also the object to execute,
			// otherwise the helper clones don't need to send it back.
			/*if (cloneId == -1)
				return new ResultContainer(objToExecute, result, execDuration);
			else
				return new ResultContainer(null, result, execDuration);*/

		} catch (Exception e) {
			// catch and return any exception since we do not know how to handle
			// them on the server side
			Log.e(TAG,  Log.getStackTraceString(e.fillInStackTrace()));
			//String resStr = gson.toJson( new ResultContainer(null, e, null, null) );
			return null;
		}

	}
   
  

	private void waitForThreadsToBeReady() throws InterruptedException {
		// Wait for the threads to be ready
		synchronized (nrClonesReady) {
			while(nrClonesReady.get() < numberOfCloneHelpers)
				nrClonesReady.wait();

			nrClonesReady.set(0);
		}
	}

	private void sendCommandToAllThreads(int command) {
		synchronized (syncObject) {
			for (int i = 0; i < numberOfCloneHelpers; i++)
				syncObject[i] = false;
			requestFromMainServer = command;
			syncObject.notifyAll();
		}
	}

	/**
	 * The Executer of remote code, which deals with the control protocol, flow
	 * of control, etc.
	 * 
	 */
	private class Executer extends Thread {
		private LinkedList<File> libraries;

		@Override
		public void run() {
			try {
				Log.d(TAG, "Start to run ClientHandler-Executor");
				InputStream in = null;
				OutputStream out = null;
				DynamicObjectInputStream objIn = null;
				ObjectOutputStream objOut = null;

				try {
					in = mClient.getInputStream();
					out = mClient.getOutputStream();

					objOut = new ObjectOutputStream(out);
					objOut.flush();
					objIn = new DynamicObjectInputStream(in);
				} catch (IOException e1) {
					e1.printStackTrace();
					Log.d(TAG, "Connection failed" + e1.getMessage());
					return;
				}
				
				Log.d(TAG, "Init stream finished");
				
				int request = 0;
				while (request != -1) {

					request = in.read();
					Log.d(TAG, "Request - " + request);

					switch(request) {
					case ControlMessages.PHONE_COMPUTATION_REQUEST:					
						Log.d(TAG, "Execute request - " + request);
						
						appName = (String) objIn.readObject();

						apkFilePath = ControlMessages.CONTAINER_APK_DIR + appName + ".apk";
						if (apkPresent(apkFilePath) ) {
							Log.d(TAG, "APK present");
							out.write(ControlMessages.APK_PRESENT);
						} else {
							// this should never be executed
							Log.d(TAG, "request APK"+apkFilePath);
							out.write(ControlMessages.APK_REQUEST);
							// Receive the apk file from the client
							//receiveApk(objIn, apkFilePath);
							receiveApk(objIn, apkFilePath);
						}
						File dexFile = new File(apkFilePath);
						libraries = addLibraries(dexFile);
						objIn.addDex(dexFile, appName);
						
						HashMap<String, String> result = (HashMap<String, String>) retrieveAndExecute(objIn, libraries);
						if(result == null)
							return;

						try {
							// Send back over the socket connection
							Log.d(TAG, "Send result back");
							objOut.writeObject(result.get("retType"));
							objOut.writeObject(result.get("retVal"));
							// Clear ObjectOutputCache - Java caching unsuitable
							// in this case
							objOut.flush();
							objOut.reset();

							Log.d(TAG, "Result successfully sent");
						} catch (IOException e) {
							Log.d(TAG, "Connection failed");
							e.printStackTrace();
							return;
						}

						break;

					case ControlMessages.PING:
						Log.d(TAG, "Reply to PING");
						out.write(ControlMessages.PONG);
						break;
						
					case ControlMessages.SEND_FILE_FIRST:
						Log.d(TAG, "The offloading need to send file first");
						out.write(ControlMessages.SEND_FILE_REQUEST);
						String filePath = (String) objIn.readObject();
						String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
						filePath = ControlMessages.CONTAINER_APK_DIR + "off-file/" + fileName;
						receiveApk(objIn, filePath);
						break;

/*					case ControlMessages.CLONE_ID_SEND:
						cloneId = in.read();
						ControlMessages.writeCloneId(cloneId);
						break;*/
					}
				}
				Log.d(TAG, "Client disconnected");
				try {
					mClient.close();
				} catch (IOException e) {
					// Don't care too much
					e.printStackTrace();
				}

			} catch (Exception e) {
				// We don't want any exceptions to escape from here,
				// hide everything silently if we didn't foresee them cropping
				// up... Since we don't want the server to die because
				// somebody's program is misbehaving
				Log.e(TAG, "Exception not caught properly - " + e);
			} catch (Error e) {
				// We don't want any exceptions to escape from here,
				// hide everything silently if we didn't foresee them cropping
				// up... Since we don't want the server to die because
				// somebody's program is misbehaving
				Log.e(TAG, "Error not caught properly - " + e);
			}  finally {
				CloneThread.inuse = false;
			}
		}
	}

	/**
	 * Check if the application is already present on the machine
	 * 
	 * @param filename
	 *            filename of the apk file (used for identification)
	 * @return true if the apk is present, false otherwise
	 */
	private boolean apkPresent(String filename) {
		// return false;
		// TODO: more sophisticated checking for existence
		File apkFile = new File(filename);
		return apkFile.exists();
	}

	public void startNewLog(){
		if (logFileWriter != null){
			try {
				logFileWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			File logFile = new File(logFileName);
			logFile.createNewFile(); // Try creating new, if doesn't exist
			logFileWriter = new FileWriter(logFile, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void traceLog(String log){
		if (logFileWriter != null) {
			try {
				logFileWriter.append(log + "\n");
				logFileWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
