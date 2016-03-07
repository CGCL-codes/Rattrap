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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GraphWindow extends Activity {

	public Intent intent;
	public Button UpdateButton;
	public EditText XMaxText, XMinText, YMinText, YMaxText, XScaleText,
			YScaleText,  StartTheta, EndTheta;
	public TextView StartHeader, EndHeader;
	public boolean polar, param;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphwindow);

		XMaxText = (EditText) findViewById(R.id.xMaxText);
		XMinText = (EditText) findViewById(R.id.xMinText);
		YMinText = (EditText) findViewById(R.id.yMinText);
		YMaxText = (EditText) findViewById(R.id.yMaxText);
		XScaleText = (EditText) findViewById(R.id.xScaleText);
		YScaleText = (EditText) findViewById(R.id.yScaleText);
		StartTheta = (EditText) findViewById(R.id.startTheta);
		EndTheta = (EditText) findViewById(R.id.endTheta);
		StartHeader = (TextView) findViewById(R.id.TextView07);
		EndHeader = (TextView) findViewById(R.id.TextView08);

		intent = getIntent();
		
		if(intent.getStringExtra("mode").equals("rect")){
			StartTheta.setVisibility(View.GONE);
			EndTheta.setVisibility(View.GONE);
			StartHeader.setVisibility(View.GONE);
			EndHeader.setVisibility(View.GONE);
			polar = false;
		}else if(intent.getStringExtra("mode").equals("param")){
			StartHeader.setText(getString(R.string.startT));
			EndHeader.setText(getString(R.string.endT));
			param = true;
			StartTheta.setText("" + intent.getDoubleExtra("start",-5));
			EndTheta.setText("" + intent.getDoubleExtra("end",5));
		}else{
			polar = true;
			StartTheta.setText("" + intent.getDoubleExtra("start",-1*Math.PI));
			EndTheta.setText("" + intent.getDoubleExtra("end",Math.PI));
		}

		XMinText.setText("" + intent.getDoubleExtra("xMin", -3));
		XMaxText.setText("" + intent.getDoubleExtra("xMax", 3));
		YMinText.setText("" + intent.getDoubleExtra("yMin", -5));
		YMaxText.setText("" + intent.getDoubleExtra("yMax", 5));
		XScaleText.setText("" + intent.getDoubleExtra("xScale", 1));
		YScaleText.setText("" + intent.getDoubleExtra("yScale", 1));
		
		

		UpdateButton = (Button) findViewById(R.id.updateButton);
		UpdateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					double xMin = AndyMath.getFunctionVal(getApplicationContext(), XMinText
							.getText().toString(), 0);
					double xMax = AndyMath.getFunctionVal(getApplicationContext(), XMaxText
							.getText().toString(), 0);
					double yMin = AndyMath.getFunctionVal(getApplicationContext(), YMinText
							.getText().toString(), 0);
					double yMax = AndyMath.getFunctionVal(getApplicationContext(), YMaxText
							.getText().toString(), 0);
					double xScale = AndyMath.getFunctionVal(getApplicationContext(), 
							XScaleText.getText().toString(), 0);
					double yScale = AndyMath.getFunctionVal(getApplicationContext(), 
							YScaleText.getText().toString(), 0);
					double startVal = -1 * Math.PI;
					double endVal = -1 * Math.PI;
					if(polar || param){
						startVal = AndyMath.getFunctionVal(getApplicationContext(), StartTheta.getText().toString(),0);
						endVal = AndyMath.getFunctionVal(getApplicationContext(), EndTheta.getText().toString(),0);
					}
					
					if (xMax <= xMin) {
						Toast.makeText(GraphWindow.this,
								getString(R.string.xMinMax),
								Toast.LENGTH_LONG).show();
					} else if (yMax <= yMin) {
						Toast.makeText(GraphWindow.this,
								getString(R.string.yMinMax),
								Toast.LENGTH_LONG).show();
					} else if (xScale <= 0) {
						Toast.makeText(GraphWindow.this,
								getString(R.string.xPos), Toast.LENGTH_LONG)
								.show();
					} else if (yScale <= 0) {
						Toast.makeText(GraphWindow.this,
								getString(R.string.yPos), Toast.LENGTH_LONG)
								.show();
					} else {
						intent.setClass(getApplicationContext(), Graph.class);
						intent.putExtra("xMin", xMin);
						intent.putExtra("xMax", xMax);
						intent.putExtra("yMin", yMin);
						intent.putExtra("yMax", yMax);
						intent.putExtra("xScale", xScale);
						intent.putExtra("yScale", yScale);
						intent.putExtra("prev", "window");
						if(polar){
							intent.putExtra("start",startVal);
							intent.putExtra("end",endVal);
						}else if(param){
							intent.putExtra("start", startVal);
							intent.putExtra("end", endVal);
						}
						startActivity(intent);
						finish();
					}
				} catch (Exception e) {
					Toast.makeText(GraphWindow.this,
									getString(R.string.windowInvalid),
									Toast.LENGTH_LONG).show();
				}
			}
		});

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(getApplicationContext(), Graph.class);
			startActivity(intent);
			finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

}
