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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddRK extends AndyActivity {

	Button AddButton, ClearButton, InsertButton;
	EditText FunText, XText, YText;

	Intent intent;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rkadd);
		keyboard = (KeyboardView)findViewById(R.id.slopeKey);
		FunText = (EditText) findViewById(R.id.funText);
		XText = (EditText) findViewById(R.id.xText);
		YText = (EditText) findViewById(R.id.yText);

		setUp(new EditText[]{FunText,XText,YText},keyboard);
		intent = getIntent();

		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString()
					+ intent.getStringExtra("iFunction"));
			String s = null;
			intent.putExtra("add", s);
		}
		
		ClearButton = (Button) findViewById(R.id.clear);
		ClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.putExtra("rFunction","clear");
				intent.setClass(getApplicationContext(), Graph.class);
				intent.putExtra("prev", "rk");
				startActivity(intent);
				
			}
		});

		AddButton = (Button) findViewById(R.id.addButton);
		AddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String fun = FunText.getText().toString();
				if (AndyMath.isValid(fun, new String[]{"x","y"})) {
					intent.setClass(getApplicationContext(), Graph.class);
					SharedPreferences.Editor edit = AddRK.this.getSharedPreferences("functions",0).edit();
					edit.putString("rkFun", fun);
					String x = XText.getText().toString();
					String y = YText.getText().toString();
					if (AndyMath.isValid(x, new String[]{})
							&& AndyMath.isValid(y, new String[]{})) {
						edit.putString("rkX", XText.getText().toString());
						edit.putString("rkY", YText.getText().toString());
						edit.commit();
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(AddRK.this, getString(R.string.invalidbounds),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(AddRK.this, getString(R.string.invalidfunctionrk),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		InsertButton = (Button) findViewById(R.id.insertButton);
		InsertButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.setClass(getApplicationContext(), InsertFunction.class);
				intent.putExtra("gFunction", FunText.getText().toString());
				intent.putExtra("prev", "addrk");
				intent.putExtra("motive", "insert");
				startActivity(intent);
				finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && keyboard.getVisibility()==View.GONE) {
			Intent intent = new Intent(getApplicationContext(), Graph.class);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onEnter(){
		if(FunText.isFocused()){
			XText.requestFocus();
		}else if(XText.isFocused()){
			YText.requestFocus();
		}else if(YText.isFocused()){
			AddButton.performClick();
		}
	}
	
	@Override
	public void updateValidity(){
		if(AndyMath.isValid(FunText.getText().toString(),new String[]{"x","y"})){
			FunText.setTextColor(Color.rgb(0,127,0));
		}else{
			FunText.setTextColor(Color.rgb(127,0,0));
		}
	}
}
