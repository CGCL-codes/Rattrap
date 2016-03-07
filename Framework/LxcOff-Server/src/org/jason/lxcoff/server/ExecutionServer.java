package org.jason.lxcoff.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Execution server which waits for incoming connections and starts a separate
 * thread for each of them, leaving the ClientHandler to actually deal with the
 * clients
 * 
 * @author Andrius
 * 
 */
public class ExecutionServer extends Service {
	
	private static final String TAG = "ExecutionServer";

	/** Called when the service is first created. */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Server created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Server destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Create server socket
		Log.d(TAG, "Start server socket");
		
		new Thread(new CloneThread(this.getApplicationContext())).start();
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
