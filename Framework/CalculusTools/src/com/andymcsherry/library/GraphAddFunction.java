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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class GraphAddFunction extends AndyActivity {

	private Button Update;
	private Button[] X;
	private EditText[] FunTextX, FunTextY;
	private RadioButton Polar, Rect, Param, ThreeD;
	private int prevToggle;
	private TextView XText1,XText2,XText3,XText4,XText5,XText6,
		YText1,YText2,YText3,YText4,YText5,YText6;
	private SharedPreferences sp;
	private KeyboardView keyboard;
	
	Intent intent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("Graph","onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphaddfunction);
		keyboard = (KeyboardView) findViewById(R.id.graphKey);
		sp = getSharedPreferences("functions", 0);
		Log.e("Graph","Shared Preferences");
		Polar = (RadioButton) findViewById(R.id.polar);
		Rect = (RadioButton) findViewById(R.id.rect);
		Param = (RadioButton) findViewById(R.id.param);
		ThreeD = (RadioButton) findViewById(R.id.threed);
		
		FunTextX = new EditText[6];
		FunTextY = new EditText[6];
		X        = new Button[6];
		FunTextX[0] = (EditText) findViewById(R.id.funText1);
		FunTextX[1] = (EditText) findViewById(R.id.funText2);
		FunTextX[2] = (EditText) findViewById(R.id.funText3);
		FunTextX[3] = (EditText) findViewById(R.id.funText4);
		FunTextX[4] = (EditText) findViewById(R.id.funText5);
		FunTextX[5] = (EditText) findViewById(R.id.funText6);
		FunTextY[0] = (EditText) findViewById(R.id.yText1);
		FunTextY[1] = (EditText) findViewById(R.id.yText2);
		FunTextY[2] = (EditText) findViewById(R.id.yText3);
		FunTextY[3] = (EditText) findViewById(R.id.yText4);
		FunTextY[4] = (EditText) findViewById(R.id.yText5);
		FunTextY[5] = (EditText) findViewById(R.id.yText6);
		XText1 = (TextView) findViewById(R.id.TextView01);
		XText2 = (TextView) findViewById(R.id.TextView02);
		XText3 = (TextView) findViewById(R.id.TextView03);
		XText4 = (TextView) findViewById(R.id.TextView04);
		XText5 = (TextView) findViewById(R.id.TextView05);
		XText6 = (TextView) findViewById(R.id.TextView06);
		YText1 = (TextView) findViewById(R.id.TextView11);
		YText2 = (TextView) findViewById(R.id.TextView12);
		YText3 = (TextView) findViewById(R.id.TextView13);
		YText4 = (TextView) findViewById(R.id.TextView14);
		YText5 = (TextView) findViewById(R.id.TextView15);
		YText6 = (TextView) findViewById(R.id.TextView16);
		X[0] = (Button) findViewById(R.id.x1);
		X[1] = (Button) findViewById(R.id.x2);
		X[2] = (Button) findViewById(R.id.x3);
		X[3] = (Button) findViewById(R.id.x4);
		X[4] = (Button) findViewById(R.id.x5);
		X[5] = (Button) findViewById(R.id.x6);
		setUp(new EditText[]{FunTextX[0],FunTextX[1],FunTextX[2],
		                     FunTextX[3],FunTextX[4],FunTextX[5],
		                     FunTextY[0],FunTextY[1],FunTextY[2],
		                     FunTextY[3],FunTextY[4],FunTextY[5]},keyboard);
		Log.e("Graph","Set Up Finished");
		intent = getIntent();
		if(intent.getIntExtra("mode",-1)==GraphView.RECT){
			Rect.toggle();
			setRect();
			prevToggle = 0;
		}else if(intent.getIntExtra("mode",-1)==GraphView.POLAR){
			Polar.toggle();
			setPolar();
			prevToggle = 1;
		}else if(intent.getIntExtra("mode",-1)==GraphView.PARAM){
			Param.toggle();
			setParam();
			prevToggle = 2;
		}else if(intent.getIntExtra("mode",-1)==GraphView.THREED){
			ThreeD.toggle();
			setThreeD();
		}
		Log.e("Graph","Display Updated");

		Update = (Button) findViewById(R.id.update);
		Update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Graph.class);
				String[] f = new String[6];
				String[] y = new String[6];
				for(int i = 0; i < 6; i++){
					f[i] = FunTextX[i].getText().toString();
					y[i] = FunTextY[i].getText().toString();
				}
				
				if(Rect.isChecked()){
					intent.putExtra("mode", GraphView.RECT);
					for(int i = 0; i < 6; i ++){
						if(!f[i].equals("")){
							if (!AndyMath.isValid(f[i],new String[]{"x"})) {
								Toast.makeText(GraphAddFunction.this, getString(R.string.warningFun) + (i+1) + getString(R.string.isInvalid),
										Toast.LENGTH_SHORT).show();
							}
						}
					}
					updateRect();
				}else if(Polar.isChecked()){
					intent.putExtra("mode",GraphView.POLAR);
					updatePolar();
					for(int i = 0; i < 6; i ++){
						if(!f[i].equals("")){
							if (!AndyMath.isValid(f[i],new String[]{"x"})) {
								Toast.makeText(GraphAddFunction.this, getString(R.string.warningFun) + (i+1) + getString(R.string.isInvalid),
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				}else if(Param.isChecked()){
					intent.putExtra("mode", GraphView.PARAM);
					updateParam();
					for(int i = 0; i < 6; i ++){
						if(!f[i].equals("") && !y[i].equals("")){
							if(!AndyMath.isValid(f[i],new String[]{"t"})){
								Toast.makeText(GraphAddFunction.this, getString(R.string.warningFun) + " X " + (i+1) +
										getString(R.string.isInvalid), Toast.LENGTH_SHORT).show();
							}
							if(!AndyMath.isValid(y[i],new String[]{"t"})){
								Toast.makeText(GraphAddFunction.this, getString(R.string.warningFun) + " Y " + (i+1) +
										getString(R.string.isInvalid), Toast.LENGTH_SHORT).show();
							}
						}else{
							if(!f[i].equals("") || !y[i].equals("")){
								Toast.makeText(GraphAddFunction.this, getString(R.string.warning2)
										+ i + ".", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}else{
					intent.putExtra("mode", GraphView.THREED);
					updateThreeD();
					for(int i = 0; i < 6; i ++){
						if(!f[i].equals("")){
							if (!AndyMath.isValid(f[i],new String[]{"x","y"})) {
								Toast.makeText(GraphAddFunction.this, getString(R.string.warningFun) + (i+1) + getString(R.string.isInvalid),
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
				intent.putExtra("prev", "fun");
				startActivity(intent);
				finish();
			}
		});
		
		for(int i = 0; i < 6; i++){
			final int j = i;
			X[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FunTextX[j].setText("");
					FunTextY[j].setText("");
				}
			});
		}
		
		Rect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(prevToggle == 1){
					updatePolar();
				}else if (prevToggle == 2){
					updateParam();
				}else if(prevToggle == 3){
					updateThreeD();
				}
				prevToggle = 0;
				setRect();
			}
		});
		
		Polar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(prevToggle == 0){
					updateRect();
				}else if (prevToggle == 2){
					updateParam();
				}else if(prevToggle == 3){
					updateThreeD();
				}
				prevToggle = 1;
				setPolar();
			}
		});
		
		Param.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(prevToggle == 0){
					updateRect();
				}else if (prevToggle == 1){
					updatePolar();
				}else if(prevToggle == 3){
					updateThreeD();
				}
				prevToggle = 2;
				setParam();
			}
		});
		
		ThreeD.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(prevToggle == 0){
					updateRect();
				}else if (prevToggle == 1){
					updatePolar();
				}else if(prevToggle ==2){
					updateParam();
				}
				prevToggle = 3;
				setThreeD();
			}
		});
		
		Log.e("Graph","Listeners added");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode== KeyEvent.KEYCODE_BACK && keyboard.getVisibility()==View.GONE){
			Intent newIntent = new Intent(getApplicationContext(), Graph.class);
			startActivity(newIntent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setParam(){
		for(int i = 0; i < 6; i++){
			FunTextY[i].setVisibility(View.VISIBLE);
			FunTextX[i].setText(sp.getString("x" + (i+1), ""));
			FunTextY[i].setText(sp.getString("y" + (i+1), ""));
		}
		YText1.setVisibility(View.VISIBLE);
		YText2.setVisibility(View.VISIBLE);
		YText3.setVisibility(View.VISIBLE);
		YText4.setVisibility(View.VISIBLE);
		YText5.setVisibility(View.VISIBLE);
		YText6.setVisibility(View.VISIBLE);
		XText1.setText("x1=");
		XText2.setText("x2=");
		XText3.setText("x3=");
		XText4.setText("x4=");
		XText5.setText("x5=");
		XText6.setText("x6=");
		updateValidity();
	}
	
	private void setPolar(){
		for(int i = 0; i < 6; i++){
			FunTextY[i].setVisibility(View.GONE);
			FunTextX[i].setText(sp.getString("r" + (i+1), ""));
		}
		YText1.setVisibility(View.GONE);
		YText2.setVisibility(View.GONE);
		YText3.setVisibility(View.GONE);
		YText4.setVisibility(View.GONE);
		YText5.setVisibility(View.GONE);
		YText6.setVisibility(View.GONE);
		XText1.setText("r1=");
		XText2.setText("r2=");
		XText3.setText("r3=");
		XText4.setText("r4=");
		XText5.setText("r5=");
		XText6.setText("r6=");
		updateValidity();
	}
	
	private void setRect(){
		for(int i = 0; i < 6; i++){
			FunTextY[i].setVisibility(View.GONE);
			FunTextX[i].setText(sp.getString("f" + (i+1), ""));
			Log.e("setRect","found: " + i);
		}
		Log.e("setRect","text updated");
		YText1.setVisibility(View.GONE);
		YText2.setVisibility(View.GONE);
		YText3.setVisibility(View.GONE);
		YText4.setVisibility(View.GONE);
		YText5.setVisibility(View.GONE);
		YText6.setVisibility(View.GONE);
		Log.e("setRect","set visibility");
		XText1.setText("f1=");
		XText2.setText("f2=");
		XText3.setText("f3=");
		XText4.setText("f4=");
		XText5.setText("f5=");
		XText6.setText("f6=");
		Log.e("setRect","set text");
		updateValidity();
		Log.e("setRect","update validity");
	}
	
	private void setThreeD(){
		for(int i = 0; i < 6; i++){
			FunTextY[i].setVisibility(View.GONE);
			FunTextX[i].setText(sp.getString("3d" + (i+1), ""));
		}
		YText1.setVisibility(View.GONE);
		YText2.setVisibility(View.GONE);
		YText3.setVisibility(View.GONE);
		YText4.setVisibility(View.GONE);
		YText5.setVisibility(View.GONE);
		YText6.setVisibility(View.GONE);
		XText1.setText("f1=");
		XText2.setText("f2=");
		XText3.setText("f3=");
		XText4.setText("f4=");
		XText5.setText("f5=");
		XText6.setText("f6=");
		updateValidity();
	}
	
	public void updateParam(){
		SharedPreferences.Editor edit = sp.edit();
		for(int i = 0; i < 6; i ++){
			edit.putString("x" + (i+1),FunTextX[i].getText().toString());
			edit.putString("y" + (i+1),FunTextY[i].getText().toString());
		}
		edit.commit();
	}
	
	public void updatePolar(){
		SharedPreferences.Editor edit = sp.edit();
		for(int i = 0; i < 6; i ++){
			edit.putString("r" + (i+1),FunTextX[i].getText().toString());
		}
		edit.commit();

	}
	
	public void updateRect(){
		SharedPreferences.Editor edit = sp.edit();
		for(int i = 0; i < 6; i ++){
			edit.putString("f" + (i+1),FunTextX[i].getText().toString());
		}
		edit.commit();
	}
	
	public void updateThreeD(){
		SharedPreferences.Editor edit = sp.edit();
		for(int i = 0; i < 6; i ++){
			edit.putString("3d" + (i+1),FunTextX[i].getText().toString());
		}
		edit.commit();
	}

	@Override
	public void updateValidity(){
		if(Param.isChecked()){
			boolean[] validX = AndyMath.isValid(new String[]{
					FunTextX[0].getText().toString(),
					FunTextX[1].getText().toString(),
					FunTextX[2].getText().toString(),
					FunTextX[3].getText().toString(),
					FunTextX[4].getText().toString(),
					FunTextX[5].getText().toString()},new String[]{"t"});
			boolean[] validY = AndyMath.isValid(new String[]{
					FunTextY[0].getText().toString(),
					FunTextY[1].getText().toString(),
					FunTextY[2].getText().toString(),
					FunTextY[3].getText().toString(),
					FunTextY[4].getText().toString(),
					FunTextY[5].getText().toString()},new String[]{"t"});
			for(int i = 0; i < 6; i ++){
				if(validX[i]){
					FunTextX[i].setTextColor(Color.rgb(0,127,0));
				}else{
					FunTextX[i].setTextColor(Color.rgb(127,0,0));
				}
				if(validY[i]){
					FunTextY[i].setTextColor(Color.rgb(0,127,0));
				}else{
					FunTextY[i].setTextColor(Color.rgb(127,0,0));
				}
			}
		}else if(ThreeD.isChecked()){
			boolean[] valid = AndyMath.isValid(new String[]{
					FunTextX[0].getText().toString(),
					FunTextX[1].getText().toString(),
					FunTextX[2].getText().toString(),
					FunTextX[3].getText().toString(),
					FunTextX[4].getText().toString(),
					FunTextX[5].getText().toString()},new String[]{"x","y"});
			for(int i = 0; i < 6; i ++){
				if(valid[i]){
					FunTextX[i].setTextColor(Color.rgb(0,127,0));
				}else{
					FunTextX[i].setTextColor(Color.rgb(127,0,0));
				}
			}
		}else{
			boolean[] valid = AndyMath.isValid(new String[]{
					FunTextX[0].getText().toString(),
					FunTextX[1].getText().toString(),
					FunTextX[2].getText().toString(),
					FunTextX[3].getText().toString(),
					FunTextX[4].getText().toString(),
					FunTextX[5].getText().toString()},new String[]{"x"});
			for(int i = 0; i < 6; i ++){
				if(valid[i]){
					FunTextX[i].setTextColor(Color.rgb(0,127,0));
				}else{
					FunTextX[i].setTextColor(Color.rgb(127,0,0));
				}
			}
		}
	}

	@Override
	protected void onEnter(){
		if(Rect.isChecked() || Polar.isChecked()){
			if(FunTextX[5].isFocused()){
	    		Update.performClick();
	    	}else if(FunTextX[0].isFocused()){
	    		FunTextX[1].requestFocus();
	    	}else if(FunTextX[1].isFocused()){
	    		FunTextX[2].requestFocus();
	    	}else if(FunTextX[2].isFocused()){
	    		FunTextX[3].requestFocus();
	    	}else if(FunTextX[3].isFocused()){
	    		FunTextX[4].requestFocus();
	    	}else if(FunTextX[4].isFocused()){
	    		FunTextX[5].requestFocus();
	    	}
		}else{
			if(FunTextY[5].isFocused()){
	    	    Update.performClick();
	    	}else if(FunTextX[0].isFocused()){
	    		FunTextY[0].requestFocus();
	    	}else if(FunTextX[1].isFocused()){
	    		FunTextY[1].requestFocus();
	    	}else if(FunTextX[2].isFocused()){
	    		FunTextY[2].requestFocus();
	    	}else if(FunTextX[3].isFocused()){
	    		FunTextY[3].requestFocus();
	    	}else if(FunTextX[4].isFocused()){
	    		FunTextY[4].requestFocus();
	    	}else if(FunTextX[5].isFocused()){
	    		FunTextY[5].requestFocus();
	    	}else if(FunTextY[0].isFocused()){
	    		FunTextX[1].requestFocus();
	    	}else if(FunTextY[1].isFocused()){
	    		FunTextX[2].requestFocus();
	    	}else if(FunTextY[2].isFocused()){
	    		FunTextX[3].requestFocus();
	    	}else if(FunTextY[3].isFocused()){
	    		FunTextX[4].requestFocus();
	    	}else if(FunTextY[4].isFocused()){
	    		FunTextX[5].requestFocus();
	    	}
		}
	}
}