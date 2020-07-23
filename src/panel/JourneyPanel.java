package panel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

import crud.*;
import main.Constants;

public class JourneyPanel extends JPanel {
	private JPanel top;
	private JPanel btm;
	private JScrollPane bottom;
		
	public JourneyPanel() {
		super();
		
		//顯示旅遊地點 出發日期 查詢鈕的panel
		top = new JPanel();
		top.setLayout(new GridBagLayout());
		top.setBounds(0, 0, Constants.PANEL_TOP_AND_BOTTOM_WIDTH, Constants.PANEL_TOP_HEIGHT);

		//顯示可報名行程資訊的panel
		btm = new JPanel();
		btm.setLayout(new BoxLayout(btm, BoxLayout.Y_AXIS));
		//可報名行程可能有很多筆 用 ScrollPane
		bottom = new JScrollPane(btm);
		bottom.setBounds(0, Constants.PANEL_TOP_HEIGHT, Constants.PANEL_TOP_AND_BOTTOM_WIDTH, Constants.PANEL_BOTTOM_HEIGHT);
		
		//top 使用 GridBagLayout 需設定 GridBagConstraints
		GridBagConstraints[] gbcs = new GridBagConstraints[5];
	    for (int i = 0; i < gbcs.length; i++) {
	    	GridBagConstraints c = new GridBagConstraints();

	    	c.gridx = i;
	        c.gridy = 0;
	        
	        c.gridwidth = 1;
	        c.gridheight = 1;
	        c.fill = GridBagConstraints.NONE;
	       
	        gbcs[i] = c;
	    }	    
		
		JLabel label1 = new JLabel("目的地：");
		top.add(label1, gbcs[0]);
		
		ArrayList<String> destinations = Journey.readTravelCodeName();
	    JComboBox<String> destComboBox = new JComboBox<String>();
	    for (String dest : destinations) {
	    	destComboBox.addItem(dest);
	    }
	    top.add(destComboBox, gbcs[1]);
	    
	    JLabel label2 = new JLabel("出發日期：");
	    top.add(label2, gbcs[2]);
	    
	    ArrayList<String> dates = Journey.readStartDate();
	    JComboBox<String> datePicker = new JComboBox<String>();
	    for (String date : dates) {
	    	datePicker.addItem(date);
	    }
	    top.add(datePicker, gbcs[3]);
	    
	    JButton submitBtn = new JButton("查詢");
	    submitBtn.addActionListener(new ActionListener() {
	    	@Override
			public void actionPerformed(ActionEvent e) {
	    		//清空前一次的查詢資訊畫面
				btm.removeAll();
			
				LinkedHashMap<Integer, Journey> hm = Journey.read((String)destComboBox.getSelectedItem(), (String)datePicker.getSelectedItem());
				
				if (hm.get(-1) != null) {
					JLabel jl = new JLabel(hm.get(-1).getErrMsg());
					btm.add(jl);
				}
				else {
					int i = 1;
					for (Map.Entry<Integer, Journey> entry : hm.entrySet()) {
						//key 是旅遊行程在資料庫內的id
					    int id = entry.getKey();
					    //value 是 Journey object
					    Journey j = entry.getValue();
					    
					    JLabel[] jls = new JLabel[6];
						jls[0] = new JLabel("行程" + i + "：" + j.getTitle());
						jls[1] = new JLabel("價格：" + j.getPrice());
						jls[2] = new JLabel("最少出團人數：" + j.getLowerBound());	
						jls[3] = new JLabel("最多出團人數：" + j.getUpperBound());
						jls[4] = new JLabel("出發日期：" + j.getStartDate());
						jls[5] = new JLabel("抵台日期：" + j.getEndDate());
						
						for (JLabel jl : jls)
							btm.add(jl);
						
						JButton btn = new JButton("預訂");
						btn.putClientProperty("id", id);
						btn.addActionListener(bookActionListener);
														
						btm.add(btn);
						i++;
					}
				}
				
				bottom.revalidate();
				bottom.repaint();
			}
	    });
	    top.add(submitBtn, gbcs[4]);
		
		this.add(top);
		this.add(bottom);

		this.setLayout(null);
	}
	
	private ActionListener bookActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton) e.getSource();
			
			int id = (int) btn.getClientProperty("id");
			
			JTextField userId = new JTextField();
			JTextField phone = new JTextField();
			JTextField people = new JTextField();
			Object[] message = {
			    "身分證字號", userId,
			    "手機", phone, 
			    "訂位人數", people
			};

			UIManager.put("OptionPane.okButtonText", "送出");
			UIManager.put("OptionPane.cancelButtonText", "取消");
			int i = 0;
			int j = 0;
			int k = 0;
			
			int option = JOptionPane.showConfirmDialog(null, message, "請輸入個人資訊完成預訂", JOptionPane.OK_CANCEL_OPTION);
			
			if (option == JOptionPane.OK_OPTION) {
				//用 Regular expression檢查身份證 手機 報名人數
			    if (!userId.getText().matches("[a-zA-Z]{1}[1-2]{1}[0-9]{8}")) {
			        System.out.println("無效身分證字號");
			        i = 1;;
			    }
			    
			    if (!phone.getText().matches("[0]{1}[9]{1}[0-9]{8}")) {
		        	System.out.println("無效手機號碼");
		        	j = 1;
			    }

			    if (!people.getText().matches("^[1-9]{1}[0-9]*")) {
			    	System.out.println("無效人數");
		        	k = 1;
			    }
			    
			    String warning = "";
			    
			    if (i == 1)
			    	warning += "無效身分證字號\n";
				if (j == 1)
			    	warning += "無效手機號碼\n";
				if (k == 1)
			    	warning += "無效訂位人數\n";
				
				if (!warning.equals("")) {	    
				    warning += "請重新輸入\n";
				    
				    UIManager.put("OptionPane.okButtonText", "確定");
				    JOptionPane.showMessageDialog(null, warning, "輸入資訊有誤", JOptionPane.ERROR_MESSAGE);
			    }
			    //送資訊給新增訂單
			    else {
			    	Journey j2 = Journey.jMap.get(id);
			    	String msg = "您選擇 " + j2.getStartDate() + " 出發的 " + j2.getTitle();
			    	String title = "預訂失敗";


			    	if (j2.getRemainingAmount() == 0) {
			    		msg += "\n行程已售罄";
			    	}
			    	else if (Integer.valueOf(people.getText()) > j2.getRemainingAmount()){
			    		msg += " 行程\n訂位人數大於出團上限人數";
			    	}
			    	else {
			    		//訂單的 productKey 是以行程的title product_key end_date 組合而成
			    		String productKey = j2.getTitle() + j2.getProductKey() + j2.getEndDate();
			    		int status = Order.create(productKey, userId.getText(), id, Integer.valueOf(people.getText()), j2.getRemainingAmount());
			    		if (status == 999) {
			    			msg += " 行程\n此筆訂單已存在\n若需要修改人數或刪除訂單\n請至查詢訂單處操作";
			    		}
			    		else {
			    			msg = "預訂成功\n可至查詢訂單處查詢訂單";
			    			title = "預訂成功";
			    		}
			    	}
			    				    	
			    	UIManager.put("OptionPane.okButtonText", "確定");
			    	JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
			    }
			} 
			else {
			    System.out.println("取消預訂");
			    
			    //測試用的 正式使用時 要去改Constants.test值
			    if (Constants.test) {
				    Journey j2 = Journey.jMap.get(id);
				    String productKey = j2.getTitle() + j2.getProductKey() + j2.getEndDate();
				    int status = Order.create(productKey, "Q123456789", id, 1, j2.getRemainingAmount());
		    		if (status == 999) {
		    			System.out.println("已存在\n若需要修改人數或刪除訂單\n請至查詢訂單處操作");
		    			JOptionPane.showMessageDialog(null, "已存在\n若需要修改人數或刪除訂單\n請至查詢訂單處操作", "", JOptionPane.INFORMATION_MESSAGE);
		    		}
		    		else {
		    			UIManager.put("OptionPane.okButtonText", "確定");
				    	JOptionPane.showMessageDialog(null, "succes", "", JOptionPane.INFORMATION_MESSAGE);
		    		}
			    }
			}
		}
		
	};
}
