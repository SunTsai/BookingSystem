package data;

import java.sql.*;


public class Connect {
	//連接資料庫 回傳Connection
	public static Connection getConn() {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:booking.db");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
			
		System.out.println("Opened database successfully");
			
		return conn;
	}
}
