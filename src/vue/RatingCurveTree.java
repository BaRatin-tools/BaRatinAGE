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
public class RatingCurveTree extends JTree implements MouseListener{

	private RatingCurveTreeModel rctModel= new RatingCurveTreeModel();

	public RatingCurveTree() {
		super(new DefaultTreeModel(null));     //a DefaultTreeModel with a null root to match the call to super()
		this.setModel(rctModel);               //the setting of the real tree model
		this.addMouseListener(this);
	}

	public RatingCurveTree(Object[] value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public RatingCurveTree(Vector<?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public RatingCurveTree(Hashtable<?, ?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public RatingCurveTree(TreeNode root) {
		super(root);
		// TODO Auto-generated constructor stub
	}

	public RatingCurveTree(TreeModel name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RatingCurveTree(TreeNode root, boolean asksAllowsChildren) {
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
			frame.getTabs().setSelectedIndex(MainFrame.RC_INDX);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			if(node.getLevel()==0){   //click o the root
				if(SwingUtilities.isRightMouseButton(me)){
					DataSetPopUpMenu popup =new DataSetPopUpMenu(MainFrame.RC_INDX);
					popup.show(this,me.getX(), me.getY());
				}
			}
			else if(node.isLeaf()){   //click on a leaf
				new RatingCurvePanel(node.toString(),true);
				if(SwingUtilities.isRightMouseButton(me)){
					DataSetPopUpMenu popup =new DataSetPopUpMenu(node,2);
					popup.show(this,me.getX(), me.getY());
				}
			}
		}		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public RatingCurveTreeModel getRctModel() {
		return rctModel;
	}

	public void setRctModel(RatingCurveTreeModel rctModel) {
		this.rctModel = rctModel;
	}

}
