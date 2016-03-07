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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.hws.jcm.data.ParseError;

public class Tangent extends AndyActivity {

	private Button addButton, evalButton, genButton;
	private TextView tv04, TanText, evalOutput;
	private EditText xtext, VarText, XText, EvalText,FunText;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.tangent);
		keyboard = (KeyboardView)findViewById(R.id.tangentKey);

		addButton = (Button) findViewById(R.id.addButton);
		genButton = (Button) findViewById(R.id.tanGenerate);
		evalButton = (Button) findViewById(R.id.tanEvaluate);
		tv04 = (TextView) findViewById(R.id.TextView04);
		evalOutput = (TextView) findViewById(R.id.evalOut);
		

		VarText = (EditText) findViewById(R.id.varText);
		XText = (EditText) findViewById(R.id.xText);
		TanText = (TextView) findViewById(R.id.tanText);
		FunText = (EditText) findViewById(R.id.tangentFun);
		EvalText = (EditText) findViewById(R.id.evalIn);
		xtext = (EditText) findViewById(R.id.evalIn);

		setInvisible();
		setUp(new EditText[]{VarText,XText,FunText,EvalText,xtext},keyboard);
		
		Intent intent = getIntent();
		FunText.setText(intent.getStringExtra("function"));
		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString()
					+ intent.getStringExtra("iFunction"));
			String s = null;
			intent.putExtra("add", s);
		}
		updateValidity();
		
		
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(), InsertFunction.class);
				myIntent.putExtra("function", FunText.getText().toString());
				myIntent.putExtra("prev", "Tangent");
				myIntent.putExtra("motive","insert");
				startActivity(myIntent);
				finish();
			}
		});

		genButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				try {
					TanText.setText(AndyMath.tangentLine(getApplicationContext(), FunText.getText().toString(),
							XText.getText().toString(), VarText.getText().toString()));
					setVisible();
				} catch (ParseError e) {
					Toast.makeText(Tangent.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		evalButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				final TextView OutText = (TextView) findViewById(R.id.evalOut);

				try {
					OutText.setText(""+AndyMath.getFunctionVal(getApplicationContext(), TanText.getText().toString(),
							VarText.getText().toString(),EvalText.getText().toString()));
				} catch (ParseError e) {
					Toast.makeText(Tangent.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
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
		} else if(item.getItemId() == 99){
			intent = new Intent(getApplicationContext(), Help.class);
		} else {
			intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid"));
		}
		intent.putExtra("function", FunText.getText().toString());
		startActivity(intent);
		return true;
	}

	// Makes views for the evaluation portion of activity invisible
	private void setInvisible() {
		tv04.setVisibility(View.GONE);
		xtext.setVisibility(View.GONE);
		evalButton.setVisibility(View.GONE);
		evalOutput.setVisibility(View.GONE);
	}
	
	// Makes views for the evaluation portion of activity visible
	private void setVisible() {
		tv04.setVisibility(View.VISIBLE);
		xtext.setVisibility(View.VISIBLE);
		evalButton.setVisibility(View.VISIBLE);
		evalOutput.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onEnter(){
		if(FunText.isFocused()){
			VarText.requestFocus();
		}else if(VarText.isFocused()){
			XText.requestFocus();
		}else if(XText.isFocused()){
			genButton.performClick();
		}else if(xtext.isFocused()){
			evalButton.performClick();
		}
	}
	
	@Override
	public void updateValidity(){
		String f = FunText.getText().toString();
		String v = VarText.getText().toString();
		if(AndyMath.isValid(f,new String[]{v})){
			FunText.setTextColor(Color.rgb(0,127,0));
		}else{
			FunText.setTextColor(Color.rgb(127,0,0));
		}	
	}
}
