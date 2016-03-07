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
import com.andymcsherry.library.actionbar.ActionBarListActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Derivative extends ActionBarListActivity {
	private static String[] listItems;
	static DisplayMetrics metrics;
	public ListView lv;
	public ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		listItems = new String[] { getString(R.string.calculator),
				getString(R.string.graph), getString(R.string.derivative),
				getString(R.string.integration), getString(R.string.arclength),
				getString(R.string.taylorseries),
				getString(R.string.tangentline),
				getString(R.string.formulatables) };

		adapter = new ArrayAdapter<String>(this, R.layout.mainlist, listItems);
		lv = getListView();
		setListAdapter(adapter);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent myIntent;
				if (position == 0) {
					myIntent = new Intent(getApplicationContext(),
							Calculator.class);
				} else if (position == 1) {
					myIntent = new Intent(getApplicationContext(), Graph.class);
				} else if (position == 2) {
					myIntent = new Intent(getApplicationContext(),
							DerivativeActivity.class);
				} else if (position == 3) {
					myIntent = new Intent(getApplicationContext(),
							DefIntegral.class);
				} else if (position == 4) {
					myIntent = new Intent(getApplicationContext(),
							ArcLength.class);
				} else if (position == 5) {
					myIntent = new Intent(getApplicationContext(),
							PowerSeries.class);
				} else if (position == 6) {
					myIntent = new Intent(getApplicationContext(),
							Tangent.class);
				} else {
					myIntent = new Intent(getApplicationContext(),
							IntegralTables.class);
				}
				startActivity(myIntent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.matrices));
		menu.add(Menu.NONE, 98, Menu.NONE, getString(R.string.options));
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			Intent intent = new Intent(getApplicationContext(), Matrices.class);
			startActivity(intent);
		} else if (item.getItemId() == 98) {
			startActivity(new Intent(getApplicationContext(), Options.class));
		} else if (item.getItemId() == 99) {
			Intent intent = new Intent(getApplicationContext(), Help.class);
			startActivity(intent);
		} else {
			Intent goToMarket = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=com.andymc.calculuspaid"));
			startActivity(goToMarket);
		}
		return true;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return mThumbIds.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GridViewItem item;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes

				item = new GridViewItem(mContext,
						(int) (metrics.widthPixels / 2), mThumbIds[position],
						listItems[position]);
			} else {
				item = (GridViewItem) convertView;
			}

			return item;
		}

		// references to our images
		private Integer[] mThumbIds = { R.drawable.tempicon,
				R.drawable.tempicon, R.drawable.tempicon, R.drawable.tempicon,
				R.drawable.tempicon, R.drawable.tempicon, R.drawable.tempicon,
				R.drawable.tempicon };
	}
}