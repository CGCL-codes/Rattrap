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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
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

public class DerivativeActivity extends AndyActivity {
	private Button addButton, insertButton, calButton, evalButton;
	private TextView DerText, evalOutput, tv04;
	private EditText FunText, FVarText, VarText, xtext;
	private RadioButton r1, r2, r3, r4;

	ClipboardManager clipboard;
	private boolean copyable = false;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.derivative);
		
		
		clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

		keyboard = (KeyboardView) findViewById(R.id.derivativeKey);
		FunText  = (EditText) findViewById(R.id.functionText);
		xtext    = (EditText) findViewById(R.id.xText);
		VarText  = (EditText) findViewById(R.id.varText);
		FVarText = (EditText) findViewById(R.id.fVarText);
		insertButton = (Button) findViewById(R.id.addButton);
		addButton    = (Button) findViewById(R.id.addGraph);
		evalButton   = (Button) findViewById(R.id.derEvaluate);
		tv04       = (TextView) findViewById(R.id.TextView04);
		evalOutput = (TextView) findViewById(R.id.evaluateText);
		DerText    = (TextView) findViewById(R.id.derText);
		Intent intent = getIntent();
		FunText.setText(intent.getStringExtra("function"));
		r1 = (RadioButton) findViewById(R.id.radio1);
		r2 = (RadioButton) findViewById(R.id.radio2);
		r3 = (RadioButton) findViewById(R.id.radio3);
		r4 = (RadioButton) findViewById(R.id.radio4);
		r1.toggle();
		
		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString() + intent.getStringExtra("iFunction"));
		    String s = null;
			intent.putExtra("add", s);
		}
		
		updateValidity();
		setInvisible();
		setUp(new EditText[]{FunText,VarText,FVarText,xtext},keyboard);
		
		insertButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						InsertFunction.class);
				myIntent.putExtra("function", FunText.getText().toString());
				myIntent.putExtra("prev", "DerivativeActivity");
				myIntent.putExtra("motive", "insert");
				myIntent.putExtra("motive", "insert");
				startActivity(myIntent);
				finish();
			}
		});
		
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						InsertFunction.class);
				myIntent.putExtra("function", DerText.getText().toString());
				myIntent.putExtra("prev", "DerivativeActivity");
				myIntent.putExtra("motive", "add");
				startActivity(myIntent);
				finish();
			}
		});

		calButton = (Button) findViewById(R.id.calculate);
		calButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				String funVars = FVarText.getText().toString();
				int commas = 0;
				for(int i = 0; i < funVars.length(); i++){
					if(funVars.charAt(i)==','){
						commas++;
					}
				}
				String[] vars = new String[commas+1];
				for(int i = 0; i <= commas; i++){
					int count = 0;
					vars[i] = "";
					for(int j = 0; j < funVars.length(); j++){
						if(count == i){
							if(funVars.charAt(j)==','){
								count++;
							}else{
								if(funVars.charAt(j) != ' '){
									vars[count] += funVars.charAt(j);
								}
							}
						}else{
							if(funVars.charAt(j)==','){
								count++;
							}
						}
					}
				}
				try {
					String s = AndyMath.getDerivative(getApplicationContext(), FunText.getText().toString(),
							vars, VarText.getText().toString());
					DerText.setText(s);
					if(r2.isChecked() || r3.isChecked() || r4.isChecked()){
						s = AndyMath.getDerivative(getApplicationContext(), s,
								vars, VarText.getText().toString());
						DerText.setText(s);
					}
					if(r3.isChecked() || r4.isChecked()){
						s = AndyMath.getDerivative(getApplicationContext(), s,
								vars, VarText.getText().toString());
						DerText.setText(s);
					}
					if(r4.isChecked()){
						s = AndyMath.getDerivative(getApplicationContext(), s,
								vars, VarText.getText().toString());
						DerText.setText(s);
					}
					copyable = true;
					setVisible();
				} catch (ParseError e) {
					Toast.makeText(DerivativeActivity.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		evalButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				String funVars = FVarText.getText().toString();
				int commas = 0;
				for(int i = 0; i < funVars.length(); i++){
					if(funVars.charAt(i)==','){
						commas++;
					}
				}
				String[] vars = new String[commas+1];
				for(int i = 0; i <= commas; i++){
					int count = 0;
					vars[i] = "";
					for(int j = 0; j < funVars.length(); j++){
						if(count == i){
							if(funVars.charAt(j)==','){
								count++;
							}else{
								if(funVars.charAt(j) != ' '){
									vars[count] += funVars.charAt(j);
								}
							}
						}else{
							if(funVars.charAt(j)==','){
								count++;
							}
						}
					}
				}
				String funVals = xtext.getText().toString();
				commas = 0;
				for(int i = 0; i < funVals.length(); i++){
					if(funVals.charAt(i)==','){
						commas++;
					}
				}
				String[] vals = new String[commas+1];
				for(int i = 0; i <= commas; i++){
					int count = 0;
					vals[i] = "";
					for(int j = 0; j < funVals.length(); j++){
						if(count == i){
							if(funVals.charAt(j)==','){
								count++;
							}else{
								if(funVals.charAt(j) != ' '){
									vals[count] += funVals.charAt(j);
								}
							}
						}else{
							if(funVals.charAt(j)==','){
								count++;
							}
						}
					}
				}
				try {
					evalOutput.setText(AndyMath.getFunctionVal(getApplicationContext(), DerText.getText().toString(),
									vars, VarText.getText().toString(),vals));
				} catch (Exception e) {
					Toast.makeText(DerivativeActivity.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	// Creates menu
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if(copyable){
			menu.add(Menu.NONE,1,Menu.NONE,getString(R.string.copy));
		}
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
			intent.putExtra("function", FunText.getText().toString());
			intent.putExtra("prev", "DerivativeActivity");
			startActivity(intent);
		}else if(item.getItemId()==1){ 
			clipboard.setText(DerText.getText().toString());
		}else if (item.getItemId() == 99){
			intent = new Intent(getApplicationContext(), Help.class);
			intent.putExtra("function", FunText.getText().toString());
			intent.putExtra("prev", "DerivativeActivity");
			startActivity(intent);
		} else {
			intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid"));
			intent.putExtra("function", FunText.getText().toString());
			intent.putExtra("prev", "DerivativeActivity");
			startActivity(intent);
		}
		return true;
	}

	// Makes views for the evaluation portion of activity invisible
	private void setInvisible() {
		tv04.setVisibility(View.GONE);
		xtext.setVisibility(View.GONE);
		evalButton.setVisibility(View.GONE);
		evalOutput.setVisibility(View.GONE);
		addButton.setVisibility(View.GONE);
	}

	// Makes views for the evaluation portion of activity visible
	private void setVisible() {
		addButton.setVisibility(View.VISIBLE);
		tv04.setVisibility(View.VISIBLE);
		evalButton.setVisibility(View.VISIBLE);
		xtext.setVisibility(View.VISIBLE);
		evalOutput.setVisibility(View.VISIBLE);
	}
	
    @Override
    protected void onEnter(){
    	if(FVarText.isFocused()){
    		FunText.requestFocus();
    	}else if(FunText.isFocused()){
    		VarText.requestFocus();
    	}else if(VarText.isFocused()){
    		calButton.performClick();
    		xtext.requestFocus();
    	}else if(xtext.isFocused()){
    		evalButton.performClick();
    		evalOutput.requestFocus();
    	}
    }
    
    @Override
    protected void updateValidity(){
    	String f = FunText.getText().toString();
		String v = FVarText.getText().toString();
		int commas = 0;
		for(int i = 0; i < v.length(); i++){
			if(v.charAt(i)==','){
				commas++;
			}
		}
		String[] vars = new String[commas+1];
		for(int i = 0; i <= commas; i++){
			int count = 0;
			vars[i] = "";
			for(int j = 0; j < v.length(); j++){
				if(count == i){
					if(v.charAt(j)==','){
						count++;
					}else{
						if(v.charAt(j) != ' '){
							vars[count] += v.charAt(j);
						}
					}
				}else{
					if(v.charAt(j)==','){
						count++;
					}
				}
			}
		}
		if(AndyMath.isValid(f,vars)){
			FunText.setTextColor(Color.rgb(0,127,0));
		}else{
			FunText.setTextColor(Color.rgb(127,0,0));
		}	
    }
    
}