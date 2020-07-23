package panel;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import crud.Journey;
import crud.Order;
import main.Constants;

public class OrderPanel extends JPanel {
	private JPanel top;
	public JPanel btm;
	private JScrollPane bottom;
	
	public JPanel getBtm() {
		return btm;
	}

	public OrderPanel() {
		super();
	
		//顯示輸入框 查詢鈕的panel
		top = new JPanel();
		top.setLayout(new GridBagLayout());
		top.setBounds(0, 0, Constants.PANEL_TOP_AND_BOTTOM_WIDTH, Constants.PANEL_TOP_HEIGHT);
		
		//顯示訂單資訊的panel
		btm = new JPanel();
		btm.setLayout(new BoxLayout(btm, BoxLayout.Y_AXIS));
		//訂單可能有很多筆 用 ScrollPane
		bottom = new JScrollPane(btm);
		bottom.setBounds(0, Constants.PANEL_TOP_HEIGHT, Constants.PANEL_TOP_AND_BOTTOM_WIDTH, Constants.PANEL_BOTTOM_HEIGHT);
		
		//top 使用 GridBagLayout 需設定 GridBagConstraints
		GridBagConstraints[] gbcs = new GridBagConstraints[3];
	    for (int i = 0; i < gbcs.length; i++) {
	    	GridBagConstraints c = new GridBagConstraints();

	    	c.gridx = i;
	        c.gridy = 0;
	        
	        c.gridwidth = 1;
	        c.gridheight = 1;
	        c.fill = GridBagConstraints.NONE;
	       
	        gbcs[i] = c;
	    }	    
		
	  	JLabel label1 = new JLabel("身份證字號：");	    
	 	top.add(label1, gbcs[0]);
	  		
	  	JTextField userIdTextField = new JTextField(Constants.TEXTFIELD_COLUMN);
	  	top.add(userIdTextField, gbcs[1]);	  	

	  	JButton submitBtn = new JButton("查詢");
	    submitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btm.removeAll();

				HashMap<String, Order> hm = Order.read(userIdTextField.getText());

				if (hm.get("-1") != null) {
					JLabel jl = new JLabel(hm.get("-1").getErrMsg());
					btm.add(jl);
				}
				else {
					for (Map.Entry<String, Order> entry : hm.entrySet()) {
					    String productKey = entry.getKey();
					    Order o = entry.getValue();
						JLabel[] jls = new JLabel[4];
						jls[0] = new JLabel("行程：" + o.getTitle());
						jls[1] = new JLabel("入住人數：" + o.getPeopleAmount());
						jls[2] = new JLabel("出發日期：" + o.getStartDate());
						jls[3] = new JLabel("抵台日期：" + o.getEndDate());
						
						for (JLabel jl : jls)
							btm.add(jl);
						JButton[] btns = new JButton[2];
						JButton updateBtn = new JButton("修改訂單");
						JButton deleteBtn = new JButton("刪除訂單");
						updateBtn.addActionListener(actionListener);
						deleteBtn.addActionListener(actionListener);
						
						//在actionListener提出這些資訊
						updateBtn.putClientProperty("productKey", productKey);
						updateBtn.putClientProperty("function", "update");
						deleteBtn.putClientProperty("productKey", productKey);
						deleteBtn.putClientProperty("function", "delete");
						

						btm.add(updateBtn);
						btm.add(deleteBtn);
					}	
				}
				
				//查詢完將身分證字號輸入框文字清除 保護個資
				userIdTextField.setText("");
				bottom.revalidate();
				bottom.repaint();
			}				
	    });

	    top.add(submitBtn, gbcs[2]);
	    		
		this.add(top);
		this.add(bottom);

		this.setLayout(null);
	}

	private ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton) e.getSource();
			String productKey = (String) btn.getClientProperty("productKey");
			String function = (String) btn.getClientProperty("function");
			
			int peopleAmount = Order.getOMap().get(productKey).getPeopleAmount();
			String userId = Order.getOMap().get(productKey).getUserId();
			
			System.out.println(function + " " + productKey);
			
			//修改訂單鈕
			if (function.equals("update")) {
				JTextField people = new JTextField();
				Object[] message = {
				    "人數", people,
				};

				UIManager.put("OptionPane.okButtonText", "送出");
				UIManager.put("OptionPane.cancelButtonText", "取消");

				int option = JOptionPane.showConfirmDialog(null, message, "請輸入修改資訊", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
				    if (!people.getText().matches("^[1-9]{1}[0-9]*")) {
				    	System.out.println("修改無效人數");
				    	String warning = "無效人數";
				    	UIManager.put("OptionPane.okButtonText", "確定");
					    JOptionPane.showMessageDialog(null, warning, "輸入資訊有誤", JOptionPane.ERROR_MESSAGE);
				    }
				    //送資訊給修改訂單
				    else {
				    	Journey j2 = Journey.jMap.get(Order.getOMap().get(productKey).getTravelScheduleId());
				    	
				    	//剩下空位不足
				    	if (Integer.valueOf(people.getText()) > (peopleAmount + j2.getRemainingAmount())) {
				    		String msg = "您選擇 " + j2.getStartDate() + " 出發的 " + j2.getTitle() + " 行程\n訂位人數大於出團上限人數";
				    		
				    		UIManager.put("OptionPane.okButtonText", "確定");
					    	JOptionPane.showMessageDialog(null, msg, "", JOptionPane.INFORMATION_MESSAGE);
				    	}
				    	else {
				    		//peopleAmount -> 原本訂單的人數
				    		//people.getText -> 欲改成的人數
				    		int status = Order.update(productKey, userId, peopleAmount, Integer.valueOf(people.getText()));
					    	System.out.println(status);
				    		if (status == 1) {
								System.out.println(".修改訂單成功");
						    	JOptionPane.showMessageDialog(null, "修改訂單成功", null, JOptionPane.INFORMATION_MESSAGE);

						    	//找出修改訂單鈕在panel的什麼位置
						    	Component[] components = btn.getParent().getComponents();
						    	int position = 0;
						    	for (int i = 0; i < components.length; i++) {
						    		if (components[i].equals(btn)) {
						    			position = i / 6;
						    			break;
						    		}
						    	}
						    	JLabel jtf = (JLabel) components[position*6+1];
						    	jtf.setText("入住人數：" + Integer.valueOf(people.getText()));
							}
				    	}
				    }
				}
			}
			//刪除鈕
			else {
				UIManager.put("OptionPane.okButtonText", "確定");
				UIManager.put("OptionPane.cancelButtonText", "取消");
				int option = JOptionPane.showConfirmDialog(null, "確定要刪除此筆訂單？", null, JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					System.out.println(productKey + "\n人數:" + peopleAmount);
					
					int status = Order.delete(productKey, peopleAmount);
					
					if (status == 1) {
						System.out.println("刪除訂單成功");
				    	JOptionPane.showMessageDialog(null, "刪除訂單成功", null, JOptionPane.INFORMATION_MESSAGE);

				    	//找出刪除訂單鈕在panel的什麼位置
				    	Component[] components = btn.getParent().getComponents();
				    	int position = 0;
				    	for (int i = 0; i < components.length; i++) {
				    		if (components[i].equals(btn)) {
				    			position = i / 6;
				    			break;
				    		}
				    	}
				    	//找到位置後 將該筆訂單的資訊清除
				    	for (int i = 0; i < 6; i++) 
				    		btm.remove(position*6);

						bottom.revalidate();
						bottom.repaint();
					}
				}
			}
		}
	};

}
