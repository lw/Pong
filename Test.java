/*  
 *  Copyright (C) 2010  Luca Wehrstedt
 *
 *  This file is released under the GPLv2
 *  Read the file 'COPYING' for more information
 */

import javax.swing.JFrame;

public class Test {
	public static void main (String[] args) {
		PongWindow window = new PongWindow ();
		window.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		window.setVisible (true);
	}
}
