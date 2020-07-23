package crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import data.Connect;

public class Order {
	private String userId;
	private String title;
	private int peopleAmount;
	private String startDate;
	private String endDate;
	private int totalPrice;
	private int travelScheduleId;
	private String errMsg;
	
	//key 為資料庫中訂單的product_key
	//value 為 Order object
	private static LinkedHashMap<String, Order> oMap;
	
	public Order(String userId, String title, int peopleAmount, String startDate, String endDate, int totalPrice, int travelScheduleId) {
		super();
		this.userId = userId;
		this.title = title;
		this.peopleAmount = peopleAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalPrice = totalPrice;
		this.travelScheduleId = travelScheduleId;
	}
	
	public Order(String errMsg) {
		this.errMsg = errMsg;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getTitle() {
		return title;
	}

	public int getPeopleAmount() {
		return peopleAmount;
	}
	public void setPeopleAmount(int num) {
		this.peopleAmount = num;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getTotalPrice() {
		return totalPrice;
	}
	
	public int getTravelScheduleId() {
		return travelScheduleId;
	}
	
	public String getErrMsg() {
		return errMsg;
	}
	
	public static LinkedHashMap<String, Order> getOMap() {
		return oMap;
	}
	
	public static int create(String productKey, String userId, int tSId, int peopleAmount, int remainingAmount) {
		String sql = "SELECT id FROM customer_order WHERE product_key=? AND user_id=?";
		
		Connection conn = Connect.getConn();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
        try {
        	pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, productKey);
			pstmt.setString(2, userId);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return 999;
			}
			
			sql = "INSERT INTO customer_order(product_key, user_id, travel_schedule_id, people_amount) VALUES(?,?,?,?)";
        	
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, productKey);
			pstmt.setString(2, userId);
			pstmt.setInt(3, tSId);
			pstmt.setInt(4, peopleAmount);
			pstmt.executeUpdate();
			
			sql = "UPDATE travel_schedule SET remaining_amount=? WHERE id=?";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, remainingAmount - peopleAmount);
			pstmt.setInt(2, tSId);
			pstmt.executeUpdate();
			
			Journey.jMap.get(tSId).setRemainingAmount(-peopleAmount);
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
        
        return 1;
	}
	
	public static LinkedHashMap<String, Order> read(String userId) {
		String sql = "SELECT ts.title, co.people_amount, ts.start_date, ts.end_date, ts.price, co.product_key, co.user_id, co.travel_schedule_id"
				+ " FROM travel_schedule AS ts"
				+ " INNER JOIN customer_order AS co"
				+ " ON ts.id=co.travel_schedule_id AND co.user_id=?";
        
		Connection conn = Connect.getConn();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
      	
		oMap = new LinkedHashMap<String, Order>();
                
        try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			if (!rs.next()) {
				Order o = new Order("查無訂單");
				oMap.put("-1", o);
			}
			else {
				do {
					String uId = rs.getString("user_id");
					String pK = rs.getString("product_key");
					String t = rs.getString("title");
					int pA = rs.getInt("people_amount");
					String sD = rs.getString("start_date");
					String eD = rs.getString("end_date");
					int p = rs.getInt("price");
					int tSId = rs.getInt("travel_schedule_id");
					
					Order o = new Order(uId, t, pA, sD, eD, p*pA, tSId);
					oMap.put(pK, o);
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
		
        return oMap;
	}
	
	public static int update(String productKey, String userId, int originalPeopleAmount, int updatePeopleAmount) {
		//更改訂單的參加人數
		String sql = "UPDATE customer_order"
				+ " SET people_amount=?"
				+ " WHERE product_key=? AND user_id=?";
		
		Connection conn = Connect.getConn();
		PreparedStatement pstmt = null;
		
        try {
        	pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, updatePeopleAmount);
			pstmt.setString(2, productKey);
			pstmt.setString(3, userId);
			pstmt.executeUpdate();
			
			//再更改原始資料的剩餘人數
			sql = "UPDATE travel_schedule AS ts"	
					+ " SET remaining_amount=remaining_amount+?"
					+ " WHERE id=(SELECT travel_schedule_id FROM customer_order WHERE product_key=?)";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, originalPeopleAmount - updatePeopleAmount);
			pstmt.setString(2, productKey);
			pstmt.executeUpdate();
			
			Journey.jMap.get(oMap.get(productKey).getTravelScheduleId()).setRemainingAmount(originalPeopleAmount - updatePeopleAmount);
			oMap.get(productKey).setPeopleAmount(updatePeopleAmount);
		} 
        catch (SQLException e) {
			e.printStackTrace();
		}
        finally {
        	try {
        		if (pstmt != null)
					pstmt.close();
        		if (conn != null)
        			conn.close();
        	}
        	catch (SQLException e) {
				e.printStackTrace();
			} 
		}
		
		return 1;
	}
	
	public static int delete(String productKey, int peopleAmount) {
		//update remaining_amount 
		String sql = "UPDATE travel_schedule AS ts"
				+ " SET remaining_amount=remaining_amount+?"
				+ " WHERE id=(SELECT travel_schedule_id FROM customer_order WHERE product_key=?)";

		Connection conn = Connect.getConn();
		PreparedStatement pstmt = null;
		
        try {
        	pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, peopleAmount);
			pstmt.setString(2, productKey);
			pstmt.executeUpdate();
			
			sql = "DELETE FROM customer_order WHERE product_key=?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, productKey);
			pstmt.executeUpdate();
			
			Journey.jMap.get(oMap.get(productKey).getTravelScheduleId()).setRemainingAmount(peopleAmount);
			oMap.remove(productKey);
		} 
        catch (SQLException e) {
			e.printStackTrace();
		}
        finally {
        	try {
        		if (pstmt != null)
					pstmt.close();
        		if (conn != null)
        			conn.close();
        	}
        	catch (SQLException e) {
				e.printStackTrace();
			} 
		}
		
		return 1;
	}
}
