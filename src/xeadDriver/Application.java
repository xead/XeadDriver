package xeadDriver;

/*
 * Copyright (c) 2015 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.UIManager;

public class Application {
	private JWindow splashScreen = new JWindow();
	private JLabel splashIcon = new JLabel();
	private JLabel splashText = new JLabel();
	private JProgressBar splashProgressBar = new JProgressBar();

	public Application(String[] args) {
		ImageIcon image = new ImageIcon(xeadDriver.Application.class.getResource("splash.png"));
		splashIcon.setIcon(image);
		splashIcon.setLayout(null);

		splashProgressBar.setBounds(0, 291, 500, 9);
		splashScreen.add(splashProgressBar);

		splashText.setFont(new java.awt.Font("Dialog", 0, 16));
		splashText.setOpaque(false);
		splashText.setBounds(280, 205, 220, 20);
		splashText.setText(XFUtility.RESOURCE.getString("SplashMessage0"));
		splashScreen.add(splashText);

		splashScreen.getContentPane().add(splashIcon);
		splashScreen.pack();
		splashScreen.setLocationRelativeTo(null);

		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				showSplash();
			}
		});
		new Session(args, this);
	}

	public static void main(String[] args) {
		try {
			UIManager.getInstalledLookAndFeels(); 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		new Application(args);
	}
	
	public void showSplash() {
		splashScreen.setVisible(true);
	}
	
	public void setTextOnSplash(String text) {
		if (splashText != null) {
			splashText.setText(text);
		}
	}
	
	public void setProgressMax(int value) {
		splashProgressBar.setMaximum(value);
	}
	
	public void setProgressValue(int value) {
		splashProgressBar.setValue(value);
	}
	
	public void repaintProgress() {
		splashProgressBar.paintImmediately(0,0,splashProgressBar.getWidth(),splashProgressBar.getHeight());
	}

	public void hideSplash() {
		if (splashScreen != null) {
			splashScreen.setVisible(false);
			splashScreen = null;
			splashText  = null;
			splashIcon  = null;
			splashProgressBar  = null;
		}
	}
}
