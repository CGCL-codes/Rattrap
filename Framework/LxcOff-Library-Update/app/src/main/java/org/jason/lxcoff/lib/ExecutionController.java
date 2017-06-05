package org.jason.lxcoff.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.jason.lxcoff.lib.db.DatabaseQuery;
import org.jason.lxcoff.lib.profilers.DeviceProfiler;
import org.jason.lxcoff.lib.profilers.LogRecord;
import org.jason.lxcoff.lib.profilers.NetworkProfiler;
import org.jason.lxcoff.lib.profilers.Profiler;
import org.jason.lxcoff.lib.profilers.ProgramProfiler;

public class ExecutionController {
	private static final String TAG = "ExecutionController";
	public static final int REGIME_CLIENT = 1;
	public static final int REGIME_SERVER = 2;
	public LogRecord lastLogRecord;
	public LogRecord lastLocalLogRecord;
	public LogRecord lastRemoteLogRecord;
	private Long mPureExecutionDuration;
	private static int mRegime;
	private Clone clone;
	private String mAppName;
	private Context mContext;
	private PackageManager mPManager;
	private int nrClones;
	private boolean onLine;
	private ExecutionSolver mSolver;
	private DeviceProfiler mDevProfiler;
	private NetworkProfiler netProfiler;
	private Socket mSocket;
	private OutputStream mOutStream;
	private ObjectOutputStream mObjOutStream;
	private InputStream mInStream;
	private ObjectInputStream mObjInStream;
	private Socket dirSocket;
	private DatabaseQuery query;
	public static String myId = null;

	public ExecutionController(Socket dirSocket, InputStream is, OutputStream os, ObjectInputStream ois, ObjectOutputStream oos, String appName, PackageManager pManager, Context context) {
		Log.d("ExecutionController", "ExecutionController Created");
		mRegime = 1;
		this.dirSocket = dirSocket;
		this.mAppName = appName;
		this.mPManager = pManager;
		this.mContext = context;
		this.mSocket = this.dirSocket;
		this.mOutStream = os;
		this.mObjOutStream = oos;
		this.mInStream = is;
		this.mObjInStream = ois;
		this.mDevProfiler = new DeviceProfiler(context);
		this.mDevProfiler.trackBatteryLevel();
		this.netProfiler = new NetworkProfiler(context);
		this.netProfiler.registerNetworkStateTrackers();
		this.query = new DatabaseQuery(context);

		try {
			this.query.destroy();
		} catch (Throwable var10) {
			var10.printStackTrace();
		}

		this.establishConnection();
	}

	public ExecutionController() {
		mRegime = 2;
		this.mSolver = new ExecutionSolver(1);
	}

	private void establishConnection() {
		try {
			Long sTime = Long.valueOf(System.nanoTime());
			synchronized(this) {
				this.onLine = true;
			}

			Long dur = Long.valueOf(System.nanoTime() - sTime.longValue());
			Log.d("PowerDroid-Profiler", "Socket and streams set-up time - " + dur.longValue() / 1000000L + "ms");
			NetworkProfiler.rttPing(this.mInStream, this.mOutStream);
			this.mSolver = new ExecutionSolver(3);
			Log.d("ExecutionController", "Getting apk data");
			String apkName = this.mPManager.getApplicationInfo(this.mAppName, 0).sourceDir;
			Log.d("ExecutionController", "Apk name - " + apkName);
			this.mOutStream.write(21);
			this.mObjOutStream.writeObject(this.mAppName);
			int response = this.mInStream.read();
			if(response == 23) {
				for(int i = 0; i < 5; ++i) {
					this.sendApk(apkName, this.mObjOutStream);
				}
			}
		} catch (UnknownHostException var7) {
			this.fallBackToLocalExecution("Connection setup to server failed: " + var7.getMessage());
		} catch (IOException var8) {
			this.fallBackToLocalExecution("Connection setup to server failed: " + var8.getMessage());
		} catch (NameNotFoundException var9) {
			this.fallBackToLocalExecution("Application not found: " + var9.getMessage());
		} catch (Exception var10) {
			this.fallBackToLocalExecution("Could not connect: " + var10.getMessage());
		}

	}

	private void fallBackToLocalExecution(String message) {
		Log.d("ExecutionController", message);
		this.mSolver = new ExecutionSolver(2);
		synchronized(this) {
			this.onLine = false;
		}
	}

	private void reestablishConnection() {
		try {
			Configuration config = new Configuration("/mnt/sdcard/thinkAir/config-phone.dat");
			config.parseConfigFile((ArrayList)null, (ArrayList)null);
			this.dirSocket = new Socket(config.getDirServiceIp(), config.getDirServicePort());
			this.mOutStream = this.dirSocket.getOutputStream();
			this.mInStream = this.dirSocket.getInputStream();
			this.mOutStream.write(30);
			this.mObjOutStream = new ObjectOutputStream(this.mOutStream);
			this.mObjInStream = new ObjectInputStream(this.mInStream);
			this.mOutStream.write(36);
			this.mObjOutStream.writeObject(myId);
			this.mObjOutStream.flush();
			this.establishConnection();
		} catch (UnknownHostException var2) {
			this.fallBackToLocalExecution("Connection setup to server failed: " + var2.getMessage());
		} catch (IOException var3) {
			this.fallBackToLocalExecution("Connection setup to server failed: " + var3.getMessage());
		} catch (Exception var4) {
			this.fallBackToLocalExecution("Could not connect: " + var4.getMessage());
		}

	}

	public Object execute(Method m, Object o) throws Throwable {
		return this.execute(m, (Object[])null, o);
	}

	public Object execute(Method m, Object[] pValues, Object o) throws IllegalArgumentException, SecurityException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
		String classMethodName = o.getClass().toString() + m.getName();
		ProgramProfiler progProfiler = new ProgramProfiler(classMethodName);

		try {
			Object result;
			Profiler profiler1;
			if(!this.netProfiler.noConnectivity() && this.mSolver.executeRemotely(this.mContext, classMethodName)) {
				profiler1 = new Profiler(mRegime, this.mContext, progProfiler, this.netProfiler, this.mDevProfiler);
				profiler1.startExecutionInfoTracking();
				result = this.executeRemotely(m, pValues, o);
				profiler1.stopAndLogExecutionInfoTracking(this.mPureExecutionDuration);
				this.lastLogRecord = profiler1.lastLogRecord;
				return result;
			} else {
				if(this.netProfiler.noConnectivity()) {
					this.onLine = false;
				}

				profiler1 = new Profiler(mRegime, this.mContext, progProfiler, (NetworkProfiler)null, this.mDevProfiler);
				profiler1.startExecutionInfoTracking();
				result = this.executeLocally(m, pValues, o);
				profiler1.stopAndLogExecutionInfoTracking(this.mPureExecutionDuration);
				this.lastLogRecord = profiler1.lastLogRecord;
				return result;
			}
		} catch (InvocationTargetException var8) {
			var8.printStackTrace();
			Log.d("ExecutionController", "InvocationTargetException " + var8);
			return var8;
		}
	}

	public Object execute(Method m, Object[] pValues, Object o, String fileName) throws IllegalArgumentException, SecurityException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
		String classMethodName = o.getClass().toString() + m.getName();
		ProgramProfiler progProfiler = new ProgramProfiler(classMethodName);

		try {
			Object result;
			Profiler profiler1;
			if(!this.netProfiler.noConnectivity() && this.mSolver.executeRemotely(this.mContext, classMethodName)) {
				profiler1 = new Profiler(mRegime, this.mContext, progProfiler, this.netProfiler, this.mDevProfiler);
				profiler1.startExecutionInfoTracking();
				result = this.executeRemotely(m, pValues, o, fileName);
				profiler1.stopAndLogExecutionInfoTracking(this.mPureExecutionDuration);
				this.lastLogRecord = profiler1.lastLogRecord;
				return result;
			} else {
				if(this.netProfiler.noConnectivity()) {
					this.onLine = false;
				}

				profiler1 = new Profiler(mRegime, this.mContext, progProfiler, (NetworkProfiler)null, this.mDevProfiler);
				profiler1.startExecutionInfoTracking();
				result = this.executeLocally(m, pValues, o);
				profiler1.stopAndLogExecutionInfoTracking(this.mPureExecutionDuration);
				this.lastLogRecord = profiler1.lastLogRecord;
				return result;
			}
		} catch (InvocationTargetException var9) {
			var9.printStackTrace();
			Log.d("ExecutionController", "InvocationTargetException " + var9);
			return var9;
		}
	}

	private Object executeLocally(Method m, Object[] pValues, Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object result = null;
		Long startTime = Long.valueOf(System.nanoTime());
		m.setAccessible(true);
		result = m.invoke(o, pValues);
		this.mPureExecutionDuration = Long.valueOf(System.nanoTime() - startTime.longValue());
		Log.d("ExecutionLocation", "LOCAL " + m.getName() + ": Actual Invocation duration - " + this.mPureExecutionDuration.longValue() / 1000000L + "ms");
		return result;
	}

	private Object executeRemotely(Method m, Object[] pValues, Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, ClassNotFoundException, NoSuchMethodException {
		Object result = null;

		ExecutionController.ConnectionRepair repair;
		try {
			Long startTime = Long.valueOf(System.nanoTime());
			this.mOutStream.write(38);
			result = this.sendAndExecute(m, pValues, o, this.mObjInStream, this.mObjOutStream);
			Long duration = Long.valueOf(System.nanoTime() - startTime.longValue());
			Log.d("ExecutionLocation", "REMOTE " + m.getName() + ": Actual Send-Receive duration - " + duration.longValue() / 1000000L + "ms");
		} catch (NullPointerException var7) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var7);
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		} catch (UnknownHostException var8) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var8);
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		} catch (IOException var9) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var9);
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		}

		return result;
	}

	private Object executeRemotely(Method m, Object[] pValues, Object o, String filename) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, ClassNotFoundException, NoSuchMethodException {
		Object result = null;

		ExecutionController.ConnectionRepair repair;
		try {
			Long startTime = Long.valueOf(System.nanoTime());
			this.mOutStream.write(43);
			this.mObjOutStream.writeObject(filename);
			int response = this.mInStream.read();
			if(response == 41) {
				this.sendFile(filename, this.mObjOutStream);
			}

			result = this.sendAndExecute(m, pValues, o, this.mObjInStream, this.mObjOutStream);
			Long duration = Long.valueOf(System.nanoTime() - startTime.longValue());
			Log.d("ExecutionLocation", "REMOTE " + m.getName() + ": Actual Send-Receive duration - " + duration.longValue() / 1000000L + "ms");
		} catch (NullPointerException var9) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var9);
			var9.printStackTrace();
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		} catch (UnknownHostException var10) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var10);
			var10.printStackTrace();
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		} catch (IOException var11) {
			Log.e("ExecutionController", "ERROR " + m.getName() + ": " + var11);
			var11.printStackTrace();
			result = this.executeLocally(m, pValues, o);
			repair = new ExecutionController.ConnectionRepair();
			repair.start();
		}

		return result;
	}

	private void sendApk(String apkName, ObjectOutputStream objOut) throws IOException {
		File apkFile = new File(apkName);
		FileInputStream fin = new FileInputStream(apkFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		byte[] tempArray = new byte[(int)apkFile.length()];
		bis.read(tempArray, 0, tempArray.length);
		Log.d("ExecutionController", "Sending apk length - " + tempArray.length);
		objOut.writeInt(tempArray.length);
		long startTime = System.nanoTime();
		Log.d("ExecutionController", "Sending apk");
		objOut.write(tempArray);
		objOut.flush();
		long estimatedTime = System.nanoTime() - startTime;
		Double estimatedBandwidth = Double.valueOf((double)tempArray.length / (double)estimatedTime * 1.0E9D);
		NetworkProfiler.addNewBandwidthEstimate(estimatedBandwidth);
		Log.d("ExecutionController", tempArray.length + " bytes sent in " + estimatedTime + " ns");
		Log.d("ExecutionController", "Estimated bandwidth - " + NetworkProfiler.bandwidth + " Bps");
		bis.close();
	}

	private void sendFile(String fileName, ObjectOutputStream objOut) throws IOException {
		File sentFile = new File(fileName);
		FileInputStream fin = new FileInputStream(sentFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		byte[] tempArray = new byte[(int)sentFile.length()];
		bis.read(tempArray, 0, tempArray.length);
		Log.d("ExecutionController", "Sending File length - " + tempArray.length);
		objOut.writeInt(tempArray.length);
		long startTime = System.nanoTime();
		Log.d("ExecutionController", "Sending File");
		objOut.write(tempArray);
		objOut.flush();
		long estimatedTime = System.nanoTime() - startTime;
		Double estimatedBandwidth = Double.valueOf((double)tempArray.length / (double)estimatedTime * 1.0E9D);
		NetworkProfiler.addNewBandwidthEstimate(estimatedBandwidth);
		Log.d("ExecutionController", tempArray.length + " bytes sent in " + estimatedTime + " ns");
		Log.d("ExecutionController", "Estimated bandwidth - " + NetworkProfiler.bandwidth + " Bps");
		bis.close();
	}

	private void sendObject(Object o, Method m, Object[] pValues, ObjectOutputStream objOut) throws IOException {
		objOut.reset();
		Log.d("ExecutionController", "Write Object and data");
		Long startSend = Long.valueOf(System.nanoTime());
		Long startRx = NetworkProfiler.getProcessRxBytes();
		Long startTx = NetworkProfiler.getProcessTxBytes();
		objOut.writeObject(o.getClass().getName());
		Log.d("ExecutionController", "Write classname: " + o.getClass().getName());
		Gson gson = new Gson();
		String objStr = gson.toJson(o);
		Log.d("ExecutionController", "object string : " + objStr);
		objOut.writeObject(objStr);
		Log.d("ExecutionController", "Write Method - " + m.getName());
		objOut.writeObject(m.getName());
		Log.d("ExecutionController", "Write method parameter types");
		Class[] paramType = m.getParameterTypes();
		String[] paramTypeName = new String[paramType.length];

		for(int i = 0; i < paramType.length; ++i) {
			Log.d("ExecutionController", "paramTypename " + i + " : " + paramType[i].getName());
			paramTypeName[i] = paramType[i].getName();
		}

		objOut.writeObject(paramTypeName);
		String pvalueStr = gson.toJson(pValues);
		objOut.writeObject(pvalueStr);
		objOut.flush();
		NetworkProfiler.addNewBandwidthEstimate(Long.valueOf(NetworkProfiler.getProcessRxBytes().longValue() - startRx.longValue() + NetworkProfiler.getProcessTxBytes().longValue() - startTx.longValue()), Long.valueOf(System.nanoTime() - startSend.longValue()));
	}

	private Object sendAndExecute(Method m, Object[] pValues, Object o, ObjectInputStream objIn, ObjectOutputStream objOut) throws IOException, ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		this.sendObject(o, m, pValues, objOut);
		Log.d("ExecutionController", "Read Result");
		Long startSend = Long.valueOf(System.nanoTime());
		Long startRx = NetworkProfiler.getProcessRxBytes();
		Long startTx = NetworkProfiler.getProcessTxBytes();
		Log.d("ExecutionController", "Read Result 1");
		String retType = (String)objIn.readObject();
		Log.d("ExecutionController", "response type : " + retType);
		if(retType.equals("int")) {
			retType = "java.lang.Integer";
		} else if(retType.equals("double")) {
			retType = "java.lang.Double";
		} else if(retType.equals("boolean")) {
			retType = "java.lang.Boolean";
		}

		String retVal = (String)objIn.readObject();
		Log.d("ExecutionController", "response value : " + retVal);
		NetworkProfiler.addNewBandwidthEstimate(Long.valueOf(NetworkProfiler.getProcessRxBytes().longValue() - startRx.longValue() + NetworkProfiler.getProcessTxBytes().longValue() - startTx.longValue()), Long.valueOf(System.nanoTime() - startSend.longValue()));
		Gson gson = new Gson();
		Object result = gson.fromJson(retVal, Class.forName(retType));
		return result;
	}

	public void setUserChoice(int userChoice) {
		this.mSolver.setUserChoice(userChoice);
	}

	public void onDestroy() {
		this.mDevProfiler.onDestroy();
		this.netProfiler.onDestroy();
	}

	public static int getRegime() {
		return mRegime;
	}

	public void setNrClones(int nrClones) {
		Log.i("ExecutionController", "Changing nrClones to: " + nrClones);
		this.nrClones = nrClones;
	}

	public class ConnectionRepair extends Thread {
		public ConnectionRepair() {
		}

		public void run() {
			ExecutionController.this.onLine = false;
			Log.d("ExecutionController", "Trying to reestablish connection to the server");
			ExecutionController.this.reestablishConnection();
			synchronized(this) {
				if(!ExecutionController.this.onLine) {
					Log.d("ExecutionController", "Reestablishing failed - register listeners for reconnecting");
					final ConnectivityManager connectivityManager = (ConnectivityManager)ExecutionController.this.mContext.getSystemService("connectivity");
					BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
						public void onReceive(Context context, Intent intent) {
							context.unregisterReceiver(this);
							NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
							if(netInfo != null) {
								Log.d("ExecutionController", "Network back up, try reestablishing the connection");
								ExecutionController.this.reestablishConnection();
							}

						}
					};
					IntentFilter networkStateFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
					ExecutionController.this.mContext.registerReceiver(networkStateReceiver, networkStateFilter);
				}

			}
		}
	}
}
