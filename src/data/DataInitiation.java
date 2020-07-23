/*
 * 查詢旅遊行程常會用到資料庫
 * 避免多次查詢
 * 一開始就先將資料庫內的旅遊行程全部存在 Journey.jMap
 * key 為在資料庫內的 id
 * value 為 Journey object
 */

package data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;

import crud.*;

public class DataInitiation {
	
	public static void initTravelScheduleData() {
		Connection conn = Connect.getConn();
		String sql = "SELECT id, title, price, lower_bound, upper_bound, start_date, end_date, product_key, remaining_amount FROM travel_schedule";	
		Statement stmt = null;
        ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            Journey.jMap = new LinkedHashMap<Integer, Journey>();
                
            while (rs.next()) {
            	int id = rs.getInt("id");
				String t = rs.getString("title");
				int p = rs.getInt("price");
				int lB = rs.getInt("lower_bound");
				int uB = rs.getInt("upper_bound");
				String sD = rs.getString("start_date");
				String eD = rs.getString("end_date");
				String pK = rs.getString("product_key");
				int rA = rs.getInt("remaining_amount");
				
				Journey j = new Journey(t, p, lB, uB, sD, eD, pK, rA);
				Journey.jMap.put(id, j);
            }
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
        finally {
        	try {
        		if (stmt != null)
					stmt.close();
        		if (conn != null)
        			conn.close();
        	}
        	catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}
}
