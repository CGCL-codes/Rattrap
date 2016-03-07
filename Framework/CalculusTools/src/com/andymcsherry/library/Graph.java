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
import java.io.FileOutputStream;
import java.util.Date;

import com.andymcsherry.library.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Graph extends Activity {

	public GraphView g;
	public Graph3DView g3d;
	public Intent intent;
	public int width, height, mode;
	private boolean trace = false, deriv = false;
	public String[] stanFun, polarFun, paramFunX, paramFunY;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intent = getIntent();
		if(intent.getIntExtra("mode",-1)==GraphView.THREED){
			mode = GraphView.THREED;
			g3d = new Graph3DView(this);
			setContentView(g3d);
		}else{
			mode = intent.getIntExtra("mode",GraphView.RECT);
			g = new GraphView(mode,this);
			setContentView(g);
			if (intent.getDoubleExtra("xMin", 3.224) != 3.224) {
				g.setWindow(intent.getDoubleExtra("xMin", -3), intent
						.getDoubleExtra("yMin", 3), intent.getDoubleExtra("xMax",
								-5), intent.getDoubleExtra("yMax", 5), intent
								.getDoubleExtra("xScale", 1), intent.getDoubleExtra(
										"yScale", 1));
				if(g.getMode()==GraphView.POLAR){
					g.setPolarBounds(intent.getDoubleExtra("start", g.getPolarStart()),
							intent.getDoubleExtra("end", g.getPolarEnd()));
				}else if(g.getMode()==GraphView.PARAM){
					g.setParamBounds(intent.getDoubleExtra("start", g.getParamStart()),
							intent.getDoubleExtra("end", g.getParamEnd()));
				}
			}
			g.drawGraph();
		}
	}

	@Override
	// Creates menu
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.functions));
		if(mode != GraphView.THREED){
			menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.window));
		}
		menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.zoomin));
		menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.zoomout));
		if(mode== GraphView.RECT){
			menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.slopefield));
			menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.diffysol));
			menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.trace));
			menu.add(Menu.NONE, 7, Menu.NONE, getString(R.string.dydx));
		}
		if(mode != GraphView.THREED){
			menu.add(Menu.NONE,8,Menu.NONE,getString(R.string.saveimage));
		}
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		return true;
	}

	@Override
	// Opens help
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			intent.setClass(getApplicationContext(), GraphAddFunction.class);
			intent.putExtra("mode",mode);
			startActivity(intent);
			finish();
		} else if (item.getItemId() == 1) {
			intent.setClass(getApplicationContext(), GraphWindow.class);
			double[] values = g.getWindow();
			intent.putExtra("xMin", values[0]);
			intent.putExtra("xMax", values[1]);
			intent.putExtra("yMin", values[2]);
			intent.putExtra("yMax", values[3]);
			intent.putExtra("xScale", values[4]);
			intent.putExtra("yScale", values[5]);
			if (mode == GraphView.RECT) {
				intent.putExtra("mode", "rect");
			} else if (mode == GraphView.POLAR) {
				intent.putExtra("mode", "polar");
				intent.putExtra("start", g.getPolarStart());
				intent.putExtra("end", g.getPolarEnd());
			} else if(mode == GraphView.PARAM){
				intent.putExtra("mode", "param");
				intent.putExtra("start", g.getParamStart());
				intent.putExtra("end", g.getParamEnd());
			}
			startActivity(intent);
			finish();
		} else if (item.getItemId() == 2) {
			if(mode!=GraphView.THREED){
				g.zoom((float) -.5);
			}else{
				g3d.zoom((float)-.5);
			}
		} else if (item.getItemId() == 3) {
			if(mode!=GraphView.THREED){
				g.zoom((float) .5);
			}else{
				g3d.zoom((float).5);
			}
		} else if (item.getItemId() == 4) {
			intent.setClass(getApplicationContext(), SlopeFieldAdd.class);
			double[] values = g.getWindow();
			intent.putExtra("xMin", values[0]);
			intent.putExtra("xMax", values[1]);
			intent.putExtra("yMin", values[2]);
			intent.putExtra("yMax", values[3]);
			intent.putExtra("xScale", values[4]);
			intent.putExtra("yScale", values[5]);
			startActivity(intent);
			finish();
		} else if (item.getItemId() == 5) {
			intent.setClass(getApplicationContext(), AddRK.class);
			double[] values = g.getWindow();
			intent.putExtra("xMin", values[0]);
			intent.putExtra("xMax", values[1]);
			intent.putExtra("yMin", values[2]);
			intent.putExtra("yMax", values[3]);
			intent.putExtra("xScale", values[4]);
			intent.putExtra("yScale", values[5]);
			startActivity(intent);
			finish();
		} else if (item.getItemId() == 6) {
			trace = !trace;
			deriv = false;
			boolean result = g.setTrace(trace);
			g.setDeriv(false);
			if (!result) {
				trace = false;
				Toast.makeText(Graph.this, getString(R.string.noFunDisp),
						Toast.LENGTH_LONG).show();
			} else if (trace) {
				Toast.makeText(Graph.this, getString(R.string.tapFun),
						Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == 7) {
			trace = false;
			deriv = !deriv;
			boolean result = g.setDeriv(deriv);
			g.setTrace(false);
			if (!result) {
				deriv = false;
				Toast.makeText(Graph.this, getString(R.string.noFunDisp),
						Toast.LENGTH_LONG).show();
			} else if (deriv) {
				Toast.makeText(Graph.this, getString(R.string.tapFun),
						Toast.LENGTH_LONG).show();
			}
		}else if(item.getItemId()==8){ 
			try {
				File root = Environment.getExternalStorageDirectory();
				if(root.canWrite()){
					Date d = new Date();
					File imageLoc = new File(root,"graph/" + d.getMonth() + d.getDay() + 
			 				d.getHours() + d.getMinutes() + d.getSeconds()+".png");
				    FileOutputStream out = new FileOutputStream(imageLoc);
				    g.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
				    
					
				}else{
					Toast.makeText(Graph.this, getString(R.string.cannotwrite), Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
			      Toast.makeText(Graph.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == 99){
			Intent myIntent = new Intent(getApplicationContext(), Help.class);
			startActivityForResult(myIntent, 0);
		}
		return true;
	}
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(getApplicationContext(), Derivative.class);
			startActivity(intent);
			finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}*/
}
