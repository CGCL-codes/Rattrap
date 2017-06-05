//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tlabs.thinkAir.dirService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBHelper {
	public static final String url = "jdbc:mysql://202.114.6.53:3306/androidlxc";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "lxc5967903";
	public Connection conn = null;
	public Statement dbstate = null;

	public DBHelper() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.conn = DriverManager.getConnection("jdbc:mysql://202.114.6.53:3306/androidlxc", "root", "lxc5967903");
			System.out.println("Database connected");
			this.dbstate = this.conn.createStatement();
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}

	public ResultSet dbSelect(String sql) {
		try {
			ResultSet dbresult = this.dbstate.executeQuery(sql);
			return dbresult;
		} catch (Exception var3) {
			System.out.println("Exception: " + var3.getMessage());
			return null;
		}
	}

	public boolean dbDelete(String sql) {
		boolean delResult = false;

		try {
			this.dbstate.executeUpdate(sql);
			delResult = true;
		} catch (Exception var4) {
			System.out.println("Exception: " + var4.getMessage());
		}

		return delResult;
	}

	public boolean dbUpdate(String sql) {
		boolean updateResult = false;

		try {
			this.dbstate.executeUpdate(sql);
			updateResult = true;
		} catch (Exception var4) {
			System.out.println("Exception: " + var4.getMessage());
		}

		return updateResult;
	}

	public boolean dbInsert(String sql) {
		boolean insertResult = false;

		try {
			this.dbstate.executeUpdate(sql);
			insertResult = true;
		} catch (Exception var4) {
			System.out.println("Exception: " + var4.getMessage());
		}

		return insertResult;
	}

	public boolean dbClose() {
		boolean closeResult = false;

		try {
			this.conn.close();
			closeResult = true;
		} catch (Exception var3) {
			System.out.println("Exception: " + var3.getMessage());
		}

		return closeResult;
	}
}
