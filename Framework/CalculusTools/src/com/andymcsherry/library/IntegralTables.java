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
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class IntegralTables extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceStance) {
		super.onCreate(savedInstanceStance);

		String[] listItems = new String[] { getString(R.string.trigIdentity),
				getString(R.string.limitForm),
				getString(R.string.derivForm),
				getString(R.string.basicInteg),
				getString(R.string.trigInteg),
				getString(R.string.invTrigInteg),
				getString(R.string.logInteg), 
				getString(R.string.expInteg),
				getString(R.string.hypInteg) };
		ListView lv = getListView();
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main, listItems));

		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent myIntent = new Intent(getApplicationContext(),
						IntegralDisplayer.class);
				if (position == 0){
					myIntent.putExtra("intChoice", "trigi");
				}else if (position == 1) {
					myIntent.putExtra("intChoice", "lim");
				}else if(position ==2){
					myIntent.putExtra("intChoice", "dif");
				}else if(position == 3){
					myIntent.putExtra("intChoice", "basic");
				} else if (position == 4) {
					myIntent.putExtra("intChoice", "trig");
				} else if (position == 5) {
					myIntent.putExtra("intChoice", "itrig");
				} else if (position == 6) {
					myIntent.putExtra("intChoice", "log");
				} else if (position == 7) {
					myIntent.putExtra("intChoice", "exp");
				} else {
					myIntent.putExtra("intChoice", "hyp");
				}
				startActivity(myIntent);

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
		intent.putExtra("function", getIntent().getStringExtra("function"));
		startActivity(intent);
		return true;
	}
}