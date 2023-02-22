
import java.awt.EventQueue;
import javax.swing.UIManager;

import ui.MainFrame;
// import bam.utils.Performance;

public class Main {

        public static void main(String[] args) throws Exception {

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

                try {
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
