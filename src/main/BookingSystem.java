/*
 * 程式進入點
 * 先初始化資料庫
 * 再將常用到的旅遊行程匯到程式內
 * 最後呈現操作介面 
 */
package main;

import data.DBInitiation;
import data.DataInitiation;

public class BookingSystem {

	public static void main(String[] args) {
		DBInitiation.init();
		DataInitiation.initTravelScheduleData();
		new Frame();		
	}
}


