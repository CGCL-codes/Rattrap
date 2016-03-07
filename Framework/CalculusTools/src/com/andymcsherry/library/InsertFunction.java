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

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class InsertFunction extends ListActivity {

	String f = "";
	String fOrig;
	
	public static String[] list = new String[24];
	
	SharedPreferences sp;
	SharedPreferences.Editor edit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent.getStringExtra("motive").equals("matrix")){
			sp = getSharedPreferences("matrices", 0);
			edit = sp.edit();
			list = new String[] {"A\n" + sp.getString("m1",""), 
					"B\n" + sp.getString("m2",""), 
					"C\n" + sp.getString("m3",""),
					"D\n" + sp.getString("m4","")};
		}else{
			sp = getSharedPreferences("functions", 0);
		edit = sp.edit();
		
		for(int i = 0; i < 6; i ++){
			list[i] = "f" + (i+1) + "=" + sp.getString("f" + (i+1), "");
			list[i+6] = "r" + (i+1) + "=" + sp.getString("r" + (i+1), "");
			list[i+12] = "x" + (i+1) + "=" + sp.getString("x" + (i+1), "");
			list[i+18] = "y" + (i+1) + "=" + sp.getString("y" + (i+1), "");
		}
		}
		setListAdapter(new ArrayAdapter<String>(this, R.layout.insertfunction,
				list));
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(getIntent().getStringExtra("motive").equals("add")){
					String f = getIntent().getStringExtra("function");
					if(position < 6){
						edit.putString("f" + (position+1), f);
					}else if (position < 12){
						edit.putString("r" + (position-5), f);
					}else if (position < 18){
						edit.putString("x" + (position-11), f);
					}else {
						edit.putString("f" + (position-17), f);
					}
					edit.commit();
					startActivity(new Intent(getApplicationContext(), Graph.class));
					finish();
				} else if(getIntent().getStringExtra("motive").equals("matrix")){
					edit.putString("m" + (position+1), getIntent().getStringExtra("matrix"));
					edit.commit();
					startActivity(new Intent(getApplicationContext(), Calculator.class));
					finish();
				}else{
					String f;
					if(position < 6){
						f = sp.getString("f" + (position+1), "");
					}else if (position < 12){
						f = sp.getString("r" + (position-5), "");
					}else if (position < 18){
						f = sp.getString("x" + (position-11), "");
					}else {
						f = sp.getString("y" + (position-17), "");
					}
					String prev = getIntent().getStringExtra("prev");
					Intent intent;
					if (prev.equals("DerivativeActivity")) {
						intent = new Intent(getApplicationContext(),
								DerivativeActivity.class);
					} else if (prev.equals("DefIntegral")) {
						intent = new Intent(getApplicationContext(),
								DefIntegral.class);
					} else if (prev.equals("ArcLength")) {
						intent = new Intent(getApplicationContext(),
								ArcLength.class);
					} else if (prev.equals("PowerSeries")) {
						intent = new Intent(getApplicationContext(),
								PowerSeries.class);
					} else if (prev.equals("Tangent")) {
						intent = new Intent(getApplicationContext(),
								Tangent.class);
					} else if (prev.equals("slopeadd")) {
						intent = new Intent(getApplicationContext(),
								SlopeFieldAdd.class);
					} else if (prev.equals("addrk")) {
						intent = new Intent(getApplicationContext(), AddRK.class);
					} else {
						intent = new Intent(getApplicationContext(), GraphAddFunction.class);
					}
					intent.putExtra("add","true");
					intent.putExtra("iFunction", f);
					intent.putExtra("function",getIntent().getStringExtra("function"));
					startActivityForResult(intent, 0);
					finish();
				}
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent;
			String prev = getIntent().getStringExtra("prev");
			String motive = getIntent().getStringExtra("motive");
			if(motive.equals("add")){
				intent = new Intent(getApplicationContext(), Graph.class);
			}else if(motive.equals("matrix")){
				intent = new Intent(getApplicationContext(), Calculator.class);
			}else if (prev.equals("DerivativeActivity")) {
				intent = new Intent(getApplicationContext(),
						DerivativeActivity.class);
			} else if (prev.equals("DefIntegral")) {
				intent = new Intent(getApplicationContext(),
						DefIntegral.class);
			} else if (prev.equals("ArcLength")) {
				intent = new Intent(getApplicationContext(),
						ArcLength.class);
			} else if (prev.equals("PowerSeries")) {
				intent = new Intent(getApplicationContext(),
						PowerSeries.class);
			} else if (prev.equals("Tangent")) {
				intent = new Intent(getApplicationContext(),
						Tangent.class);
			} else if (prev.equals("slopeadd")) {
				intent = new Intent(getApplicationContext(),
						SlopeFieldAdd.class);
			} else if (prev.equals("addrk")) {
				intent = new Intent(getApplicationContext(), AddRK.class);
			} else {
				intent = new Intent(getApplicationContext(), GraphAddFunction.class);
			}
			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
