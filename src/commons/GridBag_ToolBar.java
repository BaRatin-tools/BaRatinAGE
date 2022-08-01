package commons;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JToolBar;

/**
 * Customized tool bar, containing a list of buttons
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 *
 */
@SuppressWarnings("serial")
public class GridBag_ToolBar extends JToolBar{
	
	private ArrayList<GridBag_Button> button= new ArrayList<GridBag_Button>();

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param listener a listener
	 * @param ID list of ids for each button
	 * @param icon list of icons for each button
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param tip list of tip messages for each button
	 */
	public GridBag_ToolBar(Container container,
			ActionListener listener,
			String[] ID,
			String[] icon,
			int x, 
			int y, 
			int nx, 
			int ny,
			String[] tip){
		super();
		// create the toolbar
		this.Create(listener,ID,icon,tip);
		// Add the toolbar at the given position in container
		this.AddToContainer(container,x,y,nx,ny);
	}

	/**
	 * Create tool bar
	 * @param listener a listener
	 * @param ID list of ids for each button
	 * @param icon list of icons for each button
	 * @param tip list of tip messages for each button
	 */
	private void Create(ActionListener listener,String[] ID,String[] icon,String[] tip){
		this.setFloatable(false);
		int n=tip.length;
		// create gridbag
		int nrow=1;int ncol=n;
		int[] xx=new int[ncol];
		int[] yy=new int[nrow];
		Arrays.fill(xx,0);
		Arrays.fill(yy,0);
		double[] wx=new double[ncol];
		double[] wy=new double[nrow];
		Arrays.fill(wx,0);
		Arrays.fill(wy,0);
		GridBag_Layout.SetGrid(this,yy,xx,wy,wx);
		// add buttons
		for(int i=0;i<n;i++){
			GridBag_Button butt=new GridBag_Button(this,listener,ID[i],"",icon[i],i,0,1,1,false,false,tip[i]);
			this.button.add(butt);
		}
	}
	
	/**
	 * Add tool bar to container
	 * @param container The container where the component is added
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 */
	private void AddToContainer(Container container,int x,int y,int nx,int ny){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		container.add(this,gbc);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public ArrayList<GridBag_Button> getButton() {
		return button;
	}

	public GridBag_Button getButton(int i) {
		return button.get(i);
	}

	public void setButton(ArrayList<GridBag_Button> buttons) {
		this.button = buttons;
	}

}
