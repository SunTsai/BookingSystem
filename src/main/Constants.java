package main;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Constants {
	public static final Dimension DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static final int FRAME_WIDTH = DIMENSION.width * 3 / 4;
	public static final int FRAME_HEIGHT = DIMENSION.height * 3 / 4;
	public static final int PANEL_TOP_AND_BOTTOM_WIDTH = DIMENSION.width * 3 / 4;
	
	public static final int PANEL_TOP_HEIGHT = DIMENSION.height / 8;
	public static final int PANEL_BOTTOM_HEIGHT = FRAME_HEIGHT - PANEL_TOP_HEIGHT;
	
	public static final int TEXTFIELD_COLUMN = 10;
	
	public static final boolean test = false; 
	 
}
