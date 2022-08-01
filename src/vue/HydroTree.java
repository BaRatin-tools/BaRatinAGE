package vue;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class HydroTree extends JTree implements MouseListener {

	private HydroTreeModel hydModel= new HydroTreeModel();

	public HydroTree() {
		super(new DefaultTreeModel(null));     //a DefaultTreeModel with a null root to match the call to super()
		this.setModel(hydModel);               //the setting of the real tree model
		this.addMouseListener(this);
	}

	public HydroTree(Object[] value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public HydroTree(Vector<?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public HydroTree(Hashtable<?, ?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public HydroTree(TreeNode root) {
		super(root);
		// TODO Auto-generated constructor stub
	}

	public HydroTree(TreeModel name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public HydroTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		TreePath selPath = this.getPathForLocation(me.getX(), me.getY());
		MainFrame frame=MainFrame.getInstance();
		if (selPath != null) {		      
			if (SwingUtilities.isRightMouseButton(me)) {
				this.setSelectionPath(selPath);   //to allow the selection in the tree by right-clicking on it
			}
			frame.getTabs().setSelectedIndex(MainFrame.HYDROGRAPH_INDX);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			if(node.getLevel()==0){   //click o the root
				if(SwingUtilities.isRightMouseButton(me)){
					DataSetPopUpMenu popup =new DataSetPopUpMenu(MainFrame.HYDROGRAPH_INDX);
					popup.show(this,me.getX(), me.getY());
				}
			}
			else if(node.isLeaf()){   //click on a leaf
				new HydrographPanel(node.toString(),true);
				if(SwingUtilities.isRightMouseButton(me)){
					DataSetPopUpMenu popup =new DataSetPopUpMenu(node,true);
					popup.show(this,me.getX(), me.getY());
				}
			}
		}				
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public HydroTreeModel getHydModel() {
		return hydModel;
	}

	public void setHydModel(HydroTreeModel hydModel) {
		this.hydModel = hydModel;
	}

}
