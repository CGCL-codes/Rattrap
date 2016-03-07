package org.jason.lxcoff.server;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ThinkAirServerActivity extends Activity {
	
	private static final String TAG = "ThinkAir-Server-Activity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		
		ComponentName comp = new ComponentName(getPackageName(), ExecutionServer.class.getName());
		startService(new Intent().setComponent(comp));
		
	}
}
