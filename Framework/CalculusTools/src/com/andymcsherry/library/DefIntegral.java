/*
 * Copyright (C) 2010 Andrew P McSherry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andymcsherry.library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;

import com.andymcsherry.library.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import edu.hws.jcm.data.ParseError;

public class DefIntegral extends AndyActivity implements OnSharedPreferenceChangeListener{

	protected static final int STYLE_HORIZONTAL = 0;
	
	ProgressDialog dialog;

	private EditText aXText, bXText, aYText, bYText, FunText, xVarText, yVarText;
	private Button button, addButton;
	private TextView out, x2, y1, y2;
	private RadioButton rSingle, rDouble;
	KeyboardView keyboard;
	
	private static String TAG = "DefIntegral";
	
	Context context;
	private Configuration		config;
	private ExecutionController executionController;
	private Socket dirServiceSocket = null;
	InputStream is					= null;
	OutputStream os					= null;
	ObjectOutputStream oos			= null;
	ObjectInputStream ois			= null;
	
	public static String[] PreDefFun = {
		"sin(x+y)+sin(x+y)^2+sin(x+y)^3+sin(x+y)^4+sin(x+y)^5",
		"x+y",
		"x+y+x^2+y^2",
		"sin(x+y)+sin(x+y)^2",
		"x+y+x^2+y^2+x^3+y^3+x^4+y^4+x^5+y^5",
		"x+y+x^2+y^2+x^3+y^3",
		"sin(x+y)+sin(x+y)^2+sin(x+y)^3",
		"x+y+x^2+y^2+x^3+y^3+x^4+y^4",
		"sin(x+y)+sin(x+y)^2+sin(x+y)^3+sin(x+y)^4",
		"x+y+x^2+y^2+x^3+y^3+x^4+y^4+x^5+y^5+x^6+y^6",
		"sqrt(x)-sqrt(y)",
		"cos(x^y)*sin(y^x)",
		"8*x-y^2",
		"lnx^y",
		"e^x+y^pi",
		"e^(x/sqrt(y))",
		"1/x*cos(y/x)",
		"y*sin(x)",
		"4x+2",
		"x^2*cos(y)*sin(y)",
	};
	
	public static String[][] PreDefBounds = {
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"0", "1", "x", "x^2"},
		{"1", "2", "x", "x^2"},
		{"pi/2", "pi", "0", "x^2"},
		{"0", "pi/2", "0", "1"},
		{"0", "2", "0", "2"},
		{"0", "1", "0", "pi/2"},
	};
	
	SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.defintegral);
		
		// I wanna use network in main thread, so ...
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        settings = PreferenceManager.getDefaultSharedPreferences(this); 
        settings.registerOnSharedPreferenceChangeListener(this);     
		
		this.context = this;
		ExecutionController.myId = Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        createNotOffloadedFile();
        
		try {
			getInfoFromDirService();

		} catch (FileNotFoundException e) {
			Log.e(TAG, "Could not read the config file: " + ControlMessages.PHONE_CONFIG_FILE);
			return ;
		} catch (UnknownHostException e) {
			Log.e(TAG, "Could not connect: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			//return ;
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Could not find Clone class: " + e.getMessage());
			return;
		}
		// Create an execution controller
		this.executionController = new ExecutionController(
				this.dirServiceSocket,
				is, os, ois, oos,
				context.getPackageName(),
				context.getPackageManager(),
				context);
		
		readPrefs();
		
		keyboard = (KeyboardView)findViewById(R.id.defIntegKey);
		aXText   = (EditText) findViewById(R.id.defXA);
		bXText   = (EditText) findViewById(R.id.defXB);
		aYText   = (EditText) findViewById(R.id.defYA);
		bYText   = (EditText) findViewById(R.id.defYB);
		xVarText = (EditText) findViewById(R.id.defVarX);
		yVarText = (EditText) findViewById(R.id.defVarY);
		FunText  = (EditText) findViewById(R.id.defFunction);
		out = (TextView) findViewById(R.id.defOutput);
		x2  = (TextView) findViewById(R.id.defTextView04);
		y1  = (TextView) findViewById(R.id.defTextView05);
		y2  = (TextView) findViewById(R.id.defTextView06);
		rSingle = (RadioButton) findViewById(R.id.radio1);
		rDouble = (RadioButton) findViewById(R.id.radio2);
		rSingle.toggle();
		setSingle();
		setUp(new EditText[]{FunText,aXText,bXText,
				xVarText,yVarText,bYText,aYText},keyboard);
		Intent intent = getIntent();
		
		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString()
					+ intent.getStringExtra("iFunction"));
		String s = null;
			intent.putExtra("add", s);
		}
		
		rSingle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setSingle();
			}
		});
		
		rDouble.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setDouble();
			}
		});

		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						InsertFunction.class);
				myIntent.putExtra("motive", "insert");
				myIntent.putExtra("function", FunText.getText().toString());
				myIntent.putExtra("prev", "DefIntegral");
				startActivity(myIntent);
				finish();
			}
		});

		button = (Button) findViewById(R.id.defCalculate);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				if(rSingle.isChecked()){
					try {

						final Handler handler = new Handler(){
							@Override
							public void handleMessage(Message m){
								if(m.arg1==1){
									out.setText((String)m.obj);
									dialog.dismiss();
								}else{
									dialog.dismiss();
									Toast.makeText(DefIntegral.this, (String)m.obj,Toast.LENGTH_LONG).show();
								}
							}
						};
						Thread integrate = new Thread(new Runnable(){
							@Override
							public void run() {
								String a = aXText.getText().toString();
								String b = bXText.getText().toString();
								String function = FunText.getText().toString();
								String var = xVarText.getText().toString();
								Message m = new Message();
								try{
									m.arg1 = 1;
									m.obj = "" + AndyMath.integrate(getApplicationContext(), function, var, a, b);
									handler.sendMessage(m);
								}catch(ParseError e){
									m.obj = e.getMessage();
									m.arg1 = 0;
									handler.sendMessage(m);
								}
							}
						});
						integrate.start();
						dialog = ProgressDialog.show(
								DefIntegral.this,"","Calculating...",true);
					} catch (ParseError e) {
						Toast.makeText(DefIntegral.this, e.getMessage(),Toast.LENGTH_LONG).show();
					}
				}else{
					MathHelper.math = new OffMath(executionController);
					try{
						final Handler handler = new Handler(){
							@Override
							public void handleMessage(Message m){
								if(m.arg1==1){
									out.setText((String)m.obj);
									dialog.dismiss();
								}else{
									dialog.dismiss();
									Toast.makeText(DefIntegral.this, (String)m.obj,Toast.LENGTH_LONG).show();
								}
							}
						};
						
						Thread integrate = new Thread(new Runnable(){
							@Override
							public void run() {
								String xVar = xVarText.getText().toString();
								String yVar = yVarText.getText().toString();
								String[] bounds = new String[4];
								bounds[0] = aXText.getText().toString();
								bounds[1] = bXText.getText().toString();
								bounds[2] = aYText.getText().toString();
								bounds[3] = bYText.getText().toString();
								String fun = FunText.getText().toString();
								Message m = new Message();
								try{
									m.arg2 = 1;
									
									for(int i=0; i< 20; i++){
										long startTime = System.nanoTime();
										//m.obj = "" + AndyMath.integrate(getApplicationContext(), fun, xVar, yVar, bounds);
										double result = MathHelper.integrate(PreDefFun[i], xVar, yVar, PreDefBounds[i]);
										long dura = System.nanoTime() - startTime;
										if(Double.isNaN(result)){
											Log.d(TAG, "Predefine data "+ i + " is invalid and get a NAN.");
										}else{
											Log.d(TAG, "Predefine data "+ i + " costs " + dura / 1000000 + " ms.");
										}
										
										try {
											Thread.currentThread().sleep(2500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										startTime = System.nanoTime();
										//m.obj = "" + AndyMath.integrate(getApplicationContext(), fun, xVar, yVar, bounds);
										result = MathHelper.integrate("("+PreDefFun[i]+")^2", xVar, yVar, PreDefBounds[i]);
										dura = System.nanoTime() - startTime;
										if(Double.isNaN(result)){
											Log.d(TAG, "Predefine data "+ i + " is invalid and get a NAN.");
										}else{
											Log.d(TAG, "Predefine data "+ i + "'s function power2 costs " + dura / 1000000 + " ms.");
										}
										
										try {
											Thread.currentThread().sleep(2500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										startTime = System.nanoTime();
										//m.obj = "" + AndyMath.integrate(getApplicationContext(), fun, xVar, yVar, bounds);
										result = MathHelper.integrate("("+PreDefFun[i]+")^3", xVar, yVar, PreDefBounds[i]);
										dura = System.nanoTime() - startTime;
										if(Double.isNaN(result)){
											Log.d(TAG, "Predefine data "+ i + " is invalid and get a NAN.");
										}else{
											Log.d(TAG, "Predefine data "+ i + "'s function power3 costs " + dura / 1000000 + " ms.");
										}
										
										try {
											Thread.currentThread().sleep(2500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										m.obj = "Result is " + result + ". Cost time " + dura/1000000 + "ms.";
									}
									
									handler.sendMessage(m);
								}catch(ParseError e){
									m.obj = e.getMessage();
									m.arg1 = 0;
									handler.sendMessage(m);
								}
							}
						});
						integrate.start();
						dialog = ProgressDialog.show(
								DefIntegral.this,"","Calculating...",true);
					} catch (ParseError e) {
						Toast.makeText(DefIntegral.this, e.getMessage(),Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}
	
	/**
	 * Create an empty file on the phone in order to let the method know
	 * where is being executed (on the phone or on the clone).
	 */
	private void createNotOffloadedFile(){
		try {
			File f = new File(ControlMessages.FILE_NOT_OFFLOADED);
			f.createNewFile();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Read the config file to get the IP and port for DirectoryService.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClassNotFoundException 
	 */
	private void getInfoFromDirService() throws UnknownHostException, IOException, ClassNotFoundException {
		config = new Configuration(ControlMessages.PHONE_CONFIG_FILE);
		config.parseConfigFile(null, null);
		
		try{
			this.dirServiceSocket = new Socket();
			this.dirServiceSocket.connect(new InetSocketAddress(config.getDirServiceIp(), config.getDirServicePort()), 3000);
			this.os = this.dirServiceSocket.getOutputStream();
			this.is = this.dirServiceSocket.getInputStream();

			os.write(ControlMessages.PHONE_CONNECTION);

			oos = new ObjectOutputStream(os);
			ois = new ObjectInputStream(is);

			// Send the name and id to DirService
			os.write(ControlMessages.PHONE_AUTHENTICATION);
			oos.writeObject(ExecutionController.myId);
			oos.flush();
			
		} 
		finally {
			
		}
	}

	@Override
	// Creates menu
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.home));
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		menu.add(Menu.NONE, 100, Menu.NONE, getString(R.string.settings));
		return true;
	}

	@Override
	// Opens new activity based on user selection
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		if (item.getItemId() == 0) {
			intent = new Intent(getApplicationContext(), Derivative.class);
		} else if(item.getItemId() == 99){
			intent = new Intent(getApplicationContext(), Help.class);
		} else{
            intent = new Intent(getApplicationContext(), Preferences.class);
		}
		intent.putExtra("function", FunText.getText().toString());
		startActivity(intent);
		return true;
	}
	
	public void setDouble(){
		y1.setVisibility(View.VISIBLE);
		y2.setVisibility(View.VISIBLE);
		yVarText.setVisibility(View.VISIBLE);
		aYText.setVisibility(View.VISIBLE);
		bYText.setVisibility(View.VISIBLE);
	}
	
	public void setSingle(){
		y1.setVisibility(View.GONE);
		y2.setVisibility(View.GONE);
		yVarText.setVisibility(View.GONE);
		aYText.setVisibility(View.GONE);
		bYText.setVisibility(View.GONE);
	}
	
	@Override
	protected void onEnter(){
		if(rSingle.isChecked()){
			if(FunText.isFocused()){
	    		aXText.requestFocus();
	    	}else if(aXText.isFocused()){
	    		bXText.requestFocus();
	    	}else if(bXText.isFocused()){
	    		button.performClick();
	    	}
		}else{
			if(FunText.isFocused()){
	    		aXText.requestFocus();
	    	}else if(aXText.isFocused()){
	    		bXText.requestFocus();
	    	}else if(bXText.isFocused()){
	    		aYText.requestFocus();
	    	}else if(aYText.isFocused()){
	    		bYText.requestFocus();
	    	}else if(bYText.isFocused()){
	    		button.performClick();
	    	}
		}
	}
	
	@Override
	protected void updateValidity(){
		String x = xVarText.getText().toString();
		String y = yVarText.getText().toString();
		x2.setText(x+"2=");
		y1.setText("1"+"("+x+")=");
		y2.setText(y+"2"+"("+x+")=");
		String f = FunText.getText().toString();
		if(AndyMath.isValid(f,
				new String[] {xVarText.getText().toString(),yVarText.getText().toString()})){
			FunText.setTextColor(Color.rgb(0,127,0));
		}else{
			FunText.setTextColor(Color.rgb(127,0,0));
		}	
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		// TODO Auto-generated method stub
		readPrefs();
	}
	
    private void readPrefs() {
        boolean alwaysLocal = settings.getBoolean("alwaysLocal", false);
        Log.d(TAG, "alwaysLocal is " + alwaysLocal);
        if(alwaysLocal){
        	this.executionController.setUserChoice(ControlMessages.STATIC_LOCAL);
        }else{
        	this.executionController.setUserChoice(ControlMessages.USER_CARES_ONLY_ENERGY);
        }
    }
}