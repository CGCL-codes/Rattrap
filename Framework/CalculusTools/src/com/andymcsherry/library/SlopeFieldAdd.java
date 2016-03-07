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

public class SlopeFieldAdd extends AndyActivity {

	Button AddButton, ClearButton, InsertButton;
	EditText FunText;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slope);
		
		keyboard = (KeyboardView)findViewById(R.id.slopeKey);
		keyboard.setVisibility(View.GONE);
		FunText = (EditText) findViewById(R.id.funText);
		setUp(new EditText[]{FunText},keyboard);

		SharedPreferences sp = getSharedPreferences("functions",0);
		FunText.setText(sp.getString("slopeFun", ""));
		
		ClearButton = (Button) findViewById(R.id.clear);
		ClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor edit = SlopeFieldAdd.this.getSharedPreferences("functions",0).edit();
				edit.putString("slopeFun", "");
				edit.commit();
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), Graph.class);
				intent.putExtra("prev", "slope");
				startActivity(intent);
				
			}
		});

		AddButton = (Button) findViewById(R.id.addButton);
		AddButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (AndyMath.isValid(FunText.getText().toString(),new String[]{"x","y"})) {
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), Graph.class);
					SharedPreferences.Editor edit = SlopeFieldAdd.this.getSharedPreferences("functions",0).edit();
					edit.putString("slopeFun", FunText.getText().toString());
					edit.commit();
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(SlopeFieldAdd.this, getString(R.string.invalidfunctionrk),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		InsertButton = (Button) findViewById(R.id.insertButton);
		InsertButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), InsertFunction.class);
				intent.putExtra("gFunction", FunText.getText().toString());
				intent.putExtra("prev", "slopeadd");
				intent.putExtra("motive","insert");
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
		AddButton.performClick();
	}
	
	@Override
	public void updateValidity(){
		String f = FunText.getText().toString();
		if(AndyMath.isValid(f,new String[]{"x","y"})){
			FunText.setTextColor(Color.rgb(0,127,0));
		}else{
			FunText.setTextColor(Color.rgb(127,0,0));
		}	
	}
}