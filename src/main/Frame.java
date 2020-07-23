package main;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import panel.*;

public class Frame {
	
	private JButton[] btns;
	private JPanel[] panels;
	
	public Frame() {
		JFrame f = new JFrame("旅遊訂票系統");

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		
		btns = new JButton[2];
		btns[0] = new JButton("查詢可報名行程");
		btns[1] = new JButton("查詢訂單");
		
		panels = new JPanel[2];
		panels[0] = new JourneyPanel();
		panels[1] = new OrderPanel();
		
		for (int i = 0; i < panels.length; i++) 
			panels[i].setVisible(false);
		
	    for (int i = 0; i < btns.length; i++)
	    	btns[i].addActionListener(new btnListener(i));
	    		
		for (JButton btn: btns)
			btnPanel.add(btn);
		
	    Container container = f.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	    container.add(btnPanel);
	    for (JPanel panel: panels)
	    	container.add(panel);
	    
        f.setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
	    f.setVisible(true);   
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private class btnListener implements ActionListener {
		private int i;
		
		public btnListener(int i) {
			this.i = i;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < panels.length; i++) {
				if (i == this.i)
					panels[i].setVisible(true);
				else 
					panels[i].setVisible(false);
			}
			
			//如果按了旅遊查詢鈕  將訂單panel清空
			if (this.i == 0) {
				JPanel btm = ((OrderPanel)panels[1]).getBtm();
				btm.removeAll();
			}
		}
	}
}
