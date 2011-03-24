package xeadDriver;

import javax.swing.UIManager;


public class Application {

	public Application(String[] args) {
		new Session(args);
	}

	public static void main(String[] args) {
		//
		try {
			UIManager.getInstalledLookAndFeels(); 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		//
		new Application(args);
	}
}
