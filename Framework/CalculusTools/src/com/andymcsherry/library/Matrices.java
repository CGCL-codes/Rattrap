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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Matrices extends Activity {
	
	public EditText[] Matrices;
	
	SharedPreferences sp;
	SharedPreferences.Editor edit;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matrices);
		
		sp = getSharedPreferences("matrices",0);
		edit = sp.edit();
		
		Matrices = new EditText[4];
		Matrices[0] = (EditText) findViewById(R.id.matrixA);
		Matrices[1] = (EditText) findViewById(R.id.matrixB);
		Matrices[2] = (EditText) findViewById(R.id.matrixC);
		Matrices[3] = (EditText) findViewById(R.id.matrixD);
		
		Matrices[0].setText(sp.getString("m1", ""));
		Matrices[1].setText(sp.getString("m2", ""));
		Matrices[2].setText(sp.getString("m3", ""));
		Matrices[3].setText(sp.getString("m4", ""));
		
		Button Update = (Button) findViewById(R.id.updateButton);
		Update.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				edit.putString("m1", Matrices[0].getText().toString());
				edit.putString("m2", Matrices[1].getText().toString());
				edit.putString("m3", Matrices[2].getText().toString());
				edit.putString("m4", Matrices[3].getText().toString());
				edit.commit();
				startActivity(new Intent(getApplicationContext(),Derivative.class));
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
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			startActivity(new Intent(getApplicationContext(),Derivative.class));
			return true;
		} else if (item.getItemId() == 99) {
			startActivity(new Intent(getApplicationContext(),Help.class));
			return true;
		}
		return false;
    }
}
