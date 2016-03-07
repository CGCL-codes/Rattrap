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

import com.andymcsherry.library.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class ArcLength extends AndyActivity {

	public ProgressDialog dialog;

	protected static final int STYLE_HORIZONTAL = 0;
	
	private Button addButton;

	private EditText aXText, bXText, aYText, bYText, FunText, 
	                 xVarText, yVarText;
	private Button button;
	private TextView out, x2, y1, y2, title;
	private RadioButton rSingle, rDouble;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arclength);
		keyboard = (KeyboardView) findViewById(R.id.arcLengthKey);
		
		aXText   = (EditText) findViewById(R.id.defXA);
		bXText   = (EditText) findViewById(R.id.defXB);
		aYText   = (EditText) findViewById(R.id.defYA);
		bYText   = (EditText) findViewById(R.id.defYB);
		xVarText = (EditText) findViewById(R.id.defVarX);
		yVarText = (EditText) findViewById(R.id.defVarY);
		FunText  = (EditText) findViewById(R.id.defFunction);
		title = (TextView) findViewById(R.id.defTextView01);
		out   = (TextView) findViewById(R.id.defOutput);
		x2    = (TextView) findViewById(R.id.defTextView04);
		y1    = (TextView) findViewById(R.id.defTextView05);
		y2    = (TextView) findViewById(R.id.defTextView06);
		rSingle = (RadioButton) findViewById(R.id.radio1);
		rDouble = (RadioButton) findViewById(R.id.radio2);
		rSingle.toggle();
		setSingle();
		setUp(new EditText[]{aXText,bXText,aYText,bYText
				,xVarText,yVarText,FunText},keyboard);
		
		Intent intent = getIntent();
		
		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString()
					+ intent.getStringExtra("iFunction"));
		String s = null;
			intent.putExtra("add", s);
		}
		
		rSingle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setSingle();
			}
		});
		
		rDouble.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDouble();
			}
		});
		
		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						InsertFunction.class);
				myIntent.putExtra("function", FunText.getText().toString());
				myIntent.putExtra("prev", "DefIntegral");
				myIntent.putExtra("motive", "insert");
				startActivity(myIntent);
				finish();
			}
		});

		button = (Button) findViewById(R.id.defCalculate);
		button.setOnClickListener(new OnClickListener() {
			@Override
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
									Toast.makeText(ArcLength.this, (String)m.obj,Toast.LENGTH_LONG).show();
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
									m.obj = "" + AndyMath.arcLength(getApplicationContext(), function, var, a, b);
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
								ArcLength.this,"","Calculating...",true);
					} catch (ParseError e) {
						Toast.makeText(ArcLength.this, e.getMessage(),Toast.LENGTH_LONG).show();
					}
				}else{

					try{
						final Handler handler = new Handler(){
							@Override
							public void handleMessage(Message m){
								if(m.arg1==1){
									out.setText((String)m.obj);
									dialog.dismiss();
								}else{
									dialog.dismiss();
									Toast.makeText(ArcLength.this, (String)m.obj,Toast.LENGTH_LONG).show();
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
									m.obj = "" + AndyMath.surfaceArea(getApplicationContext(), fun, xVar, yVar, bounds);
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
								ArcLength.this,"","Calculating...",true);
					} catch (ParseError e) {
						Toast.makeText(ArcLength.this, e.getMessage(),Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

	@Override
	// Creates menu
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.home));
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		return true;
	}

	@Override
	// Opens new activity based on user selection
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		if (item.getItemId() == 0) {
			intent = new Intent(getApplicationContext(), Derivative.class);
		} else if (item.getItemId()==99){
			intent = new Intent(getApplicationContext(), Help.class);
		} else {
			intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid"));
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
		title.setText(getString(R.string.findSurface));
	}
	
	public void setSingle(){
		y1.setVisibility(View.GONE);
		y2.setVisibility(View.GONE);
		yVarText.setVisibility(View.GONE);
		aYText.setVisibility(View.GONE);
		bYText.setVisibility(View.GONE);
		title.setText(getString(R.string.findArc));
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
}