package org.jason.lxcoff.lib.db;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;

public class DatabaseQuery {
	private ArrayList<String> arrayKeys = null;
	private ArrayList<String> arrayValues = null;
	private ArrayList<String> databaseKeys = null;
	private ArrayList<String> databaseKeyOptions = null;
	private DBAdapter database;

	public DatabaseQuery(Context context) {
		this.databaseKeys = new ArrayList();
		this.databaseKeyOptions = new ArrayList();
		this.databaseKeys.add("methodName");
		this.databaseKeyOptions.add("text not null");
		this.databaseKeys.add("execLocation");
		this.databaseKeyOptions.add("text not null");
		this.databaseKeys.add("networkType");
		this.databaseKeyOptions.add("text");
		this.databaseKeys.add("networkSubType");
		this.databaseKeyOptions.add("text");
		this.databaseKeys.add("execDuration");
		this.databaseKeyOptions.add("text");
		this.databaseKeys.add("energyConsumption");
		this.databaseKeyOptions.add("text");
		this.database = new DBAdapter(context, "logTable", this.databaseKeys, this.databaseKeyOptions);
		this.database.open();
		this.arrayKeys = new ArrayList();
		this.arrayValues = new ArrayList();
	}

	public void appendData(String key, String value) {
		this.arrayKeys.add(key);
		this.arrayValues.add(value);
	}

	public void addRow() {
		this.database.insertEntry(this.arrayKeys, this.arrayValues);
	}

	public void updateRow() {
		this.database.updateEntry(this.arrayKeys, this.arrayValues);
	}

	public ArrayList<String> getData(String[] keys, String selection, String[] selectionArgs, String groupBy, String having, String sortBy, String sortOption) {
		ArrayList<String> list = new ArrayList();
		Cursor results = this.database.getAllEntries(keys, selection, selectionArgs, groupBy, having, sortBy, sortOption);

		while(results.moveToNext()) {
			list.add(results.getString(results.getColumnIndex(sortBy)));
			list.add(results.getString(results.getColumnIndex("energyConsumption")));
		}

		results.close();
		return list;
	}

	public void destroy() throws Throwable {
		this.database.close();
	}
}
