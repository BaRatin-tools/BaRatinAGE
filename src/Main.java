
import java.awt.EventQueue;
import javax.swing.UIManager;
// import javax.swing.plaf.metal.MetalLookAndFeel;

import ui.MainFrame;
// import utils.Performance;

public class Main {

        public static void main(String[] args) throws Exception {

                // System.setProperty("sun.java2d.uiScale", "1");

                /*
                 * *************************************************************
                 * 
                 * Test zone
                 * ---------
                 * 
                 ***************************************************************
                 **/

                // long startTime = System.currentTimeMillis();

                // Performance.printMemoryUsage();
                // Performance.printTimeElapsed(startTime);

                /**
                 * ************************************************************
                 */

                // Locale currentLocale;
                // ResourceBundle messages;

                // Lg.getInstance();

                try {
                        // UIManager.setLookAndFeel(new MetalLookAndFeel());
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                        e.printStackTrace();
                }
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                                try {
                                        new MainFrame();
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                });
        }
}
