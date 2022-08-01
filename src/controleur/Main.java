package controleur;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import moteur.Station;
import vue.MainFrame;
import Utils.Config;
/**
 * Main
 * @author Ben Renard, Irstea Lyon
 */
public class Main{
	public static void main(String[] args) {
		// try setting the look-and-feel
		try {
			Config conf= Config.getInstance();
			if(conf.getLookAndFeel().equals("OS")){UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
			else{for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (conf.getLookAndFeel().equals(info.getName())) {UIManager.setLookAndFeel(info.getClassName());break;}
				}
			}
		}
	    catch(Exception e){};// leave to default look-and-feel
	    // Run the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Station.getInstance();
					MainFrame.getInstance();
					//UIManager.put("OptionPane.messageFont",GUI.getOptions().getFontLabel());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
