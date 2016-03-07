package org.jason.lxcoff.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Listens to android.intent.action.BOOT_COMPLETED (defined in
 * AndroidManifest.xml) and starts the execution server when the system has
 * finished booting
 * 
 * @author Andrius
 * 
 */
public class ExecutionServerAutoStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("PowerDroid-Server", "onReceiveIntent");

		Log.d("PowerDroid-Server", "Start Execution Service");

		Intent serviceIntent = new Intent(context, ExecutionServer.class);
		//serviceIntent.setAction("de.tlabs.thinkAir.server.ExecutionServer");
		context.startService(serviceIntent);
	}
}
