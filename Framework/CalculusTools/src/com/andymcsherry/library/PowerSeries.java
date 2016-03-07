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
import android.widget.TextView;
import android.widget.Toast;
import edu.hws.jcm.data.ParseError;

public class PowerSeries extends AndyActivity {

	private Button addButton, button;
	private EditText FunText, VarText, CenText, IText;
	private TextView SeriesText;
	ClipboardManager clipboard;
	private boolean copyable = false;
	KeyboardView keyboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.powerseries);
		clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		keyboard = (KeyboardView)findViewById(R.id.seriesKey);
		button = (Button) findViewById(R.id.powerGenerate);
		VarText = (EditText) findViewById(R.id.powerVarText);
		CenText = (EditText) findViewById(R.id.powerCenterText);
		IText   = (EditText) findViewById(R.id.powerIText);
		FunText = (EditText) findViewById(R.id.powerFunctionText);
		Intent intent = getIntent();
		FunText.setText(intent.getStringExtra("function"));
		if (intent.getStringExtra("add") != null) {
			FunText.setText(FunText.getText().toString()
					+ intent.getStringExtra("iFunction"));
			String s = null;
			intent.putExtra("add", s);
		}
		setUp(new EditText[]{FunText,CenText,IText,VarText},keyboard);
		updateValidity();
		
		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						InsertFunction.class);
				myIntent.putExtra("function", FunText.getText()
						.toString());
				myIntent.putExtra("prev", "PowerSeries");
				myIntent.putExtra("motive", "insert");
				startActivity(myIntent);
				finish();
			}
		});

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				keyboard.setVisibility(View.GONE);
				try{
				int count = Integer.parseInt(IText.getText().toString());

				if (count <= 0) { // check to make sure the number of series
					// terms is > 0
					Toast.makeText(PowerSeries.this,
							getString(R.string.iterPos),
							Toast.LENGTH_LONG).show();
				} else {
					String var = VarText.getText().toString();
					String cen = CenText.getText().toString();
					String fun = FunText.getText().toString();
					try{
					String output = AndyMath.getPowerSeries(getApplicationContext(), 
							fun, var, cen, count);

					SeriesText = (TextView) findViewById(R.id.seriesText);
					SeriesText.setText(output);
					copyable=true;
					}catch(ParseError e){
						Toast.makeText(PowerSeries.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
				}catch(NumberFormatException e){
					Toast.makeText(PowerSeries.this, getString(R.string.iterInt), Toast.LENGTH_LONG).show();
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
			startActivity(intent);
		}else if(item.getItemId()==1){ 
			clipboard.setText(SeriesText.getText().toString());
		} else if(item.getItemId() == 99){
			intent = new Intent(getApplicationContext(), Help.class);
			intent.putExtra("function", FunText.getText().toString());
			startActivity(intent);
		} else {
			intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid"));
			intent.putExtra("function", FunText.getText().toString());
			startActivity(intent);
		}
		return true;
	}
	
	@Override
	protected void onEnter(){
		if(FunText.isFocused()){
			VarText.requestFocus();
		}else if(VarText.isFocused()){
			CenText.requestFocus();
		}else if(CenText.isFocused()){
			IText.requestFocus();
		}else if(IText.isFocused()){
			button.performClick();
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