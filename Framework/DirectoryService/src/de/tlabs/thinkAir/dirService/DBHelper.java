/**
 * 
 */
package de.tlabs.thinkAir.dirService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Administrator
 *
 */

public class DBHelper {
	public static final String url = "jdbc:mysql://202.114.10.148:3306/androidlxc";
//	public static final String url = "jdbc:mysql://192.168.155.1:3306/androidlxc";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "KFniuchao88";
	

	public Connection conn = null;
	public Statement dbstate = null;

	public DBHelper() {
		try {
			Class.forName(name);//指定连接类型
			conn = DriverManager.getConnection(url, user, password);//获取连接
			System.out.println("Database connected");
			dbstate = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ResultSet dbSelect(String sql) 
	{
		try{ 
			ResultSet dbresult = dbstate.executeQuery(sql); 
			return dbresult; 
		}catch(Exception err){ 
			System.out.println("Exception: " + err.getMessage()); 
			return null;
		} 
	}//end String dbSelect(…) 
	 
	/** 
	 * 对数据库表中的记录进行删除操作 
	 * @param tableName 
	 * @param condition 
	 * @return bool值，表示删除成功或者失败。 
	 */ 
	public boolean dbDelete(String sql) 
	{//——–>>>删除操作 
		boolean delResult = false; 
		try{ 
			dbstate.executeUpdate(sql);	//return int // int delRe = ?? 
			delResult = true; 
		}catch(Exception e){ 
			System.out.println ("Exception: " + e.getMessage()); 
		} 
		if (delResult) 
			return true; 
		else 
			return false; 
	}//end dbDelete(…)  
	
	
	/** 
	 * 对数据库表中记录进行更新操作 
	 * @param tabName 
	 * @param reCount 
	 * @return bool值，成功返回true，失败返回false 
	 */ 
	public boolean dbUpdate(String sql) 
	{
		boolean updateResult = false;
		try 
		{ 
			dbstate.executeUpdate(sql); 
			updateResult = true; 
		}catch(Exception err){ 
			System.out.println("Exception: " + err.getMessage()); 
		} 
		return updateResult;
	}//end dbUpdate(…) 
	 
	
	/** 
	 * 对数据库表进行插入操作 
	 * @param tabName 
	 * @param hm 
	 * @return bool值，成功返回true，失败返回false 
	 */ 
	public boolean dbInsert(String sql) 
	{
		boolean insertResult = false;
		try 
		{ 
			dbstate.executeUpdate(sql); 
			insertResult = true; 
		}catch(Exception e){ 
			System.out.println("Exception: " + e.getMessage()); 
		} 
		return insertResult; 
	}//end dbInsert(…) 
	 
	
	/** 
	 * 断开数据库 
	 * @return bool值，成功返回true，失败返回false 
	 */ 
	public boolean dbClose() 
	{ 
		boolean closeResult = false; 
		try 
		{ 
			conn.close(); 
			closeResult = true; 
		}catch(Exception e){ 
			System.out.println("Exception: " + e.getMessage()); 
		} 
		return closeResult;
	}//end dbClose() 

}
