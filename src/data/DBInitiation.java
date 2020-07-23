package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVReader;


public class DBInitiation {
	
	//main function 一開始會呼叫
	public static void init() {
		Connection conn = Connect.getConn();
		
		createTable(conn);
		insertTravelData(conn);
		insertTravelScheduleData(conn);
		
		try {
			conn.close(); 
		} 
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	//初始化資料表
	private static void createTable(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS travel ("
                + "	travel_code integer PRIMARY KEY,"
                + "	travel_code_name text"
                + ");";
		String sql2 = "CREATE TABLE IF NOT EXISTS travel_schedule ("
                + "	id integer PRIMARY KEY,"
                + "	title text,"
                + "	travel_code integer,"
                + "	product_key text,"
                + "	price integer,"
                + "	start_date text,"
                + "	end_date text,"
                + "	lower_bound integer,"
                + "	upper_bound integer,"
                + "	remaining_amount integer"
                + ");";
		String sql3 = "CREATE TABLE IF NOT EXISTS customer_order ("
				+ " id integer PRIMARY KEY,"
                + "	product_key text,"
                + "	user_id text,"
                + "	travel_schedule_id integer,"
                + "	people_amount integer"
                + ");";
		
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.execute(sql2);
			stmt.execute(sql3);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try { 
				stmt.close(); 
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//讀取 travel_code.json 檔 寫入 travel table
	private static void insertTravelData(Connection conn) {
		String sql = "SELECT travel_code FROM travel";	
		Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
		
		try {
			stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (!rs.next()) {
				File file = new File("travel_code.json");
				FileInputStream fis = new FileInputStream(file);
				byte[] data = new byte[(int) file.length()];
				fis.read(data);
				fis.close();
				
				JSONArray jsonArray = new JSONArray(new String(data, "UTF-8"));
	
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject travelObj = (JSONObject) jsonArray.get(i);
					
					sql = "INSERT INTO travel(travel_code, travel_code_name) VALUES(?, ?)";
			        pstmt = conn.prepareStatement(sql);
			        pstmt.setInt(1, travelObj.getInt("travel_code"));
		            pstmt.setString(2, travelObj.getString("travel_code_name"));
		            pstmt.executeUpdate();
				}
            }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (stmt != null)
					stmt.close(); 
			} 
			catch (Exception e) { 
				e.printStackTrace(); 
			}
		}
	}
	
	//讀取 trip_data_all.csv 檔 寫入 travel_schedule table
	private static void insertTravelScheduleData(Connection conn) {
		String sql = "SELECT travel_code FROM travel_schedule";	
		Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;	
		
		try {
			stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (!rs.next()) {
            	CSVReader reader = new CSVReader(new FileReader("trip_data_all.csv")); 
            	String [] strs;
            	String [] last = null;
            	reader.readNext();
            	int i = 1;
            	
            	while ((strs = reader.readNext()) != null) {
            		if (strs.length == 8) {
	            		sql = "INSERT INTO travel_schedule"
								+ "(title, travel_code, product_key, price, start_date, end_date, lower_bound, upper_bound, remaining_amount)"
								+ " VALUES(?,?,?,?,?,?,?,?,?)";
						pstmt = conn.prepareStatement(sql);						
						pstmt.setString(1, strs[0]);
						pstmt.setInt(2, Integer.valueOf(strs[1]));
						pstmt.setString(3, strs[2]);
						pstmt.setInt(4, Integer.valueOf(strs[3]));
						pstmt.setString(5, strs[4]);
						pstmt.setString(6, strs[5]);
						pstmt.setInt(7, Integer.valueOf(strs[6]));
						pstmt.setInt(8, Integer.valueOf(strs[7]));
						pstmt.setInt(9, Integer.valueOf(strs[7]));
						pstmt.executeUpdate();
            		}
            		
            		last = strs;
            	}
            	
            	sql = "INSERT INTO travel_schedule"
						+ "(title, travel_code, product_key, price, start_date)"
						+ " VALUES(?, ?, ?, ?, ?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, last[0]);
				pstmt.setInt(2, Integer.valueOf(last[1]));
				pstmt.setString(3, last[2]);
				pstmt.setInt(4, Integer.valueOf(last[3]));
				pstmt.setString(5, last[4]);
				pstmt.executeUpdate();
            }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stmt != null)
					stmt.close(); 
				if (pstmt != null)
					pstmt.close(); 
				if (rs != null)
					rs.close(); 
			}
			catch (Exception e) {
				e.printStackTrace(); 
			}
		}
	}

}
