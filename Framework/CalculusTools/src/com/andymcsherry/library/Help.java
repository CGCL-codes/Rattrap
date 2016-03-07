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
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class Help extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
	}

	@Override
	// Creates menu
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.home)); 
		return true;
	}

	@Override
	// Opens new activity based on user selection
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		if(item.getItemId()==0){
			intent = new Intent(getApplicationContext(), Derivative.class);
			intent.putExtra("function", getIntent().getStringExtra("function")); //$NON-NLS-1$ //$NON-NLS-2$
		}else{
			intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid")); //$NON-NLS-1$
		}
		startActivity(intent);
		return true;
	}
}
