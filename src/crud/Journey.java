package crud;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import data.Connect;

public class Journey {
	private String title;
	private int price;
	private int lowerBound;
	private int upperBound;
	private String startDate;
	private String endDate;
	private String productKey;
	private int remainingAmount;
	private String errMsg;
	
	//key 為資料庫中travel_schedule的id
	//value 為 Journey object
	//在data.DataInitiation中被初始化
	public static LinkedHashMap<Integer, Journey> jMap;
	
	public Journey(String title, int price, int lowerBound, int upperBound, String startDate, String endDate, String productKey, int remainingAmount) {
		super();
		this.title = title;
		this.price = price;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.startDate = startDate;
		this.endDate = endDate;
		this.productKey = productKey;
		this.remainingAmount = remainingAmount;
	}
	
	public Journey(String errMsg) {
		super();
		this.errMsg = errMsg;
	}

	public String getTitle() {
		return title;
	}

	public int getPrice() {
		return price;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}
	
	public String getProductKey() {
		return productKey;
	}
	
	public int getRemainingAmount() {
		return remainingAmount;
	}

	public String getErrMsg() {
		return errMsg;
	}
	
	public void setRemainingAmount(int num) {
		this.remainingAmount += num;
	}
	
	//查詢可報名行程
	public static LinkedHashMap<Integer, Journey> read(String destination, String startDate) {        
		String sql = "SELECT ts.id, ts.title, ts.price, ts.lower_bound, ts.upper_bound, ts.start_date, ts.end_date, ts.product_key, ts.remaining_amount"
				+ " FROM travel_schedule AS ts"
				+ " INNER JOIN travel AS t"
				+ " ON t.travel_code_name=? AND ts.start_date>? AND t.travel_code=ts.travel_code"
				+ " ORDER BY ts.price, ts.start_date";
        
		Connection conn = Connect.getConn();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
      	
		LinkedHashMap<Integer, Journey> hm = new LinkedHashMap<Integer, Journey>();
                
        try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, destination);
			pstmt.setString(2, startDate);
			rs = pstmt.executeQuery();

			if (!rs.next()) {
				Journey j = new Journey("選擇的地點日期無開團資訊");
				hm.put(-1, j);
			}
			else {
				do {
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

					hm.put(id, j);
				}while (rs.next());
			}
		} 
        catch (SQLException e) {
			e.printStackTrace();
		}
        finally {
        	try {
        		if (rs != null)
        			rs.close();
        		if (pstmt != null)
					pstmt.close();
        		if (conn != null)
        			conn.close();
        	}
        	catch (SQLException e) {
				e.printStackTrace();
			} 
		}
		
		return hm;
	}
	
	//查詢旅遊地點 從資料庫讀資料 顯示在JourneyPanel
	public static ArrayList<String> readTravelCodeName() {
		String sql = "SELECT travel_code_name FROM travel";
		String key = "travel_code_name";
		
		return resultSetToArrayList(queryDB(sql), key); 
	}
	
	//查詢出發日期 從資料庫讀資料 顯示在JourneyPanel
	public static ArrayList<String>readStartDate() {
		String sql = "SELECT DISTINCT start_date FROM travel_schedule ORDER BY start_date";
		String key = "start_date";
		
		return resultSetToArrayList(queryDB(sql), key);
	}
	
	private static ResultSet queryDB(String sql) {
		Connection conn = Connect.getConn();
		
		Statement stmt = null;
        ResultSet rs = null;	
                
        try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} 
        catch (SQLException e) {
			e.printStackTrace();
        }        
        
        return rs;
	}
	
	//資料庫查詢完的結果 整理成 ArrayList 回傳
	private static ArrayList<String> resultSetToArrayList(ResultSet rs, String key) {
		ArrayList<String> al = new ArrayList<>();
        try {
			while (rs.next())
				al.add(rs.getString(key));
		} 
        catch (SQLException e) {
			e.printStackTrace();
		}
        finally {
        	try {
        		if (rs != null)
					rs.close();
			}
        	catch (SQLException e) {
        		e.printStackTrace();
			}
        }
        
        return al;
	}
}
