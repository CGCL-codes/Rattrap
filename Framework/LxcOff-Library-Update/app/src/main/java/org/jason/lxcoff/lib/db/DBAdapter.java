package org.jason.lxcoff.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import java.util.ArrayList;

public class DBAdapter {
	private static final String DATABASE_NAME = "ThinkAir-Log.db";
	private String DATABASE_TABLE;
	private static final int DATABASE_VERSION = 1;
	public static final String KEY_ID = "_id";
	public ArrayList<String> TABLE_KEYS = new ArrayList();
	public ArrayList<String> TABLE_OPTIONS = new ArrayList();
	private String DATABASE_CREATE;
	private SQLiteDatabase db;
	private DBAdapter.myDBHelper dbHelper;

	public DBAdapter(Context context, String table, ArrayList<String> keys, ArrayList<String> options) {
		this.DATABASE_TABLE = table;
		this.TABLE_KEYS = (ArrayList)keys.clone();
		this.TABLE_OPTIONS = options;
		String keyString = "";

		for(int i = 0; this.TABLE_KEYS.size() > i; ++i) {
			if(i + 1 < this.TABLE_OPTIONS.size() && this.TABLE_OPTIONS.get(i) != null) {
				this.TABLE_OPTIONS.set(i, (String)this.TABLE_OPTIONS.get(i) + ",");
			} else if(i + 1 == this.TABLE_OPTIONS.size() && this.TABLE_OPTIONS.get(i) != null) {
				if(i + 1 < this.TABLE_KEYS.size()) {
					this.TABLE_OPTIONS.set(i, (String)this.TABLE_OPTIONS.get(i) + ",");
				} else {
					this.TABLE_KEYS.set(i, (String)this.TABLE_KEYS.get(i));
				}
			} else if(i + 1 != this.TABLE_KEYS.size()) {
				this.TABLE_KEYS.set(i, (String)this.TABLE_KEYS.get(i) + ",");
			} else {
				this.TABLE_KEYS.set(i, (String)this.TABLE_KEYS.get(i));
			}

			System.out.println(this.TABLE_OPTIONS.toString());
			System.out.println(this.TABLE_KEYS.toString());
			if(i + 1 <= this.TABLE_OPTIONS.size() && this.TABLE_OPTIONS.get(i) != null) {
				keyString = keyString + " " + (String)this.TABLE_KEYS.get(i) + " " + (String)this.TABLE_OPTIONS.get(i);
			} else if(i + 1 > this.TABLE_OPTIONS.size() || this.TABLE_OPTIONS.get(i) == null) {
				keyString = keyString + " " + (String)this.TABLE_KEYS.get(i);
			}
		}

		this.DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + this.DATABASE_TABLE + " (" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " + keyString + ");";
		Log.v("PowerDroid-Database", this.DATABASE_CREATE);
		this.dbHelper = new DBAdapter.myDBHelper(context, "ThinkAir-Log.db", (CursorFactory)null, 1, this.DATABASE_TABLE, this.DATABASE_CREATE);
	}

	public DBAdapter open() throws SQLException {
		this.db = this.dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		this.db.close();
	}

	public long insertEntry(ArrayList<String> key, ArrayList<String> value) {
		ContentValues contentValues = new ContentValues();

		for(int i = 0; key.size() > i; ++i) {
			contentValues.put((String)key.get(i), (String)value.get(i));
		}

		Log.v("PowerDroid-Database", "Database Add: " + contentValues.toString());
		return this.db.insert(this.DATABASE_TABLE, (String)null, contentValues);
	}

	public boolean removeEntry(long rowIndex) {
		return this.db.delete(this.DATABASE_TABLE, "_id=" + rowIndex, (String[])null) > 0;
	}

	public Cursor getAllEntries(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String sortBy, String sortOption) {
		return this.db.query(this.DATABASE_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy + " " + sortOption);
	}

	public Cursor getAllEntries(String[] columns, String selection, String[] selectionArgs) {
		return this.db.query(this.DATABASE_TABLE, columns, selection, selectionArgs, (String)null, (String)null, (String)null);
	}

	public void update(String sqlQuery) {
		this.db.rawQuery("UPDATE " + this.DATABASE_TABLE + sqlQuery, (String[])null);
	}

	public Cursor getAllEntries(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String sortBy, String sortOption, String limit) {
		return this.db.query(this.DATABASE_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy + " " + sortOption, limit);
	}

	public boolean clearTable() {
		return this.db.delete(this.DATABASE_TABLE, (String)null, (String[])null) > 0;
	}

	public int updateEntry(long rowIndex, ArrayList<String> key, ArrayList<String> value) {
		String where = "_id=" + rowIndex;
		ContentValues contentValues = new ContentValues();

		for(int i = 0; key.size() > i; ++i) {
			contentValues.put((String)key.get(i), (String)value.get(i));
		}

		return this.db.update(this.DATABASE_TABLE, contentValues, where, (String[])null);
	}

	public int updateEntry(ArrayList<String> key, ArrayList<String> value) {
		String where = "methodName = ? AND execLocation = ? AND networkType = ? AND networkSubType = ?";
		String[] whereArgs = new String[]{(String)value.get(0), (String)value.get(1), (String)value.get(2), (String)value.get(3)};
		ContentValues contentValues = new ContentValues();

		for(int i = 0; key.size() > i; ++i) {
			contentValues.put((String)key.get(i), (String)value.get(i));
		}

		return this.db.update(this.DATABASE_TABLE, contentValues, where, whereArgs);
	}

	private static class myDBHelper extends SQLiteOpenHelper {
		private String creationString;
		private String tableName;
		SQLiteDatabase db;

		public myDBHelper(Context context, String name, CursorFactory factory, int version, String tableName, String creationString) {
			super(context, name, factory, version);
			this.creationString = creationString;
			this.tableName = tableName;
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(this.creationString);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("PowerDroid-Database", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + this.tableName);
			this.onCreate(db);
		}

		public void onOpen(SQLiteDatabase db) {
			db.execSQL(this.creationString);
		}
	}
}
