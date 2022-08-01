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

import Utils.Config;
import Utils.Dico;

@SuppressWarnings("serial")
public class DataSetTree extends JTree implements MouseListener {

	private DataSetTreeModel dstModel= new DataSetTreeModel();
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	public DataSetTree() {
		super(new DefaultTreeModel(null));     //a DefaultTreeModel with a null root to match the call to super()
		this.setModel(dstModel);               //the setting of the real tree model
		this.addMouseListener(this);
		this.getDstModel().addErrorNode(dico.entry("Remnant_Linear"));
		this.getDstModel().addErrorNode(dico.entry("Remnant_Constant"));
	}

	public DataSetTree(Object[] arg0) {
		super(arg0);
	}

	public DataSetTree(Vector<?> arg0) {
		super(arg0);
	}

	public DataSetTree(Hashtable<?, ?> arg0) {
		super(arg0);
	}

	public DataSetTree(TreeNode arg0) {
		super(arg0);
	}

	public DataSetTree(TreeModel arg0) {
		super(arg0);
	}

	public DataSetTree(TreeNode arg0, boolean arg1) {
		super(arg0, arg1);
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		TreePath selPath = this.getPathForLocation(me.getX(), me.getY());
		MainFrame frame=MainFrame.getInstance();
		if (selPath != null) {		      
			if (SwingUtilities.isRightMouseButton(me)) {
				this.setSelectionPath(selPath);   //to allow the selection in the tree by right-clicking on it
			}
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			DefaultMutableTreeNode root =(DefaultMutableTreeNode) selPath.getPathComponent(0);
			int indx=-1;
			if(node.getLevel()==1){   //if the use click on second lvl of nodes (root is the first, so is 0) which represent the differents types of datasets
				indx=root.getIndex(node);
				frame.getTabs().setSelectedIndex(indx);
				if(SwingUtilities.isRightMouseButton(me)){
					if(indx!=MainFrame.REMNANT_INDX){		
						DataSetPopUpMenu popup =new DataSetPopUpMenu(indx);
						popup.show(this,me.getX(), me.getY());
					}
				}
			}
			else if(node.isLeaf()){   //if the user click on a leaf, the third lvl of nodes which represent a specific dataset
				indx=root.getIndex(node.getParent());
				frame.getTabs().setSelectedIndex(indx);
				if(indx==MainFrame.HYDRAULIC_INDX){
					new ConfigHydrauPanel(node.toString(),true);
				}
				if(indx==MainFrame.GAUGING_INDX){
					new GaugingPanel(node.toString(),true);
				}
				if(indx==MainFrame.LIMNI_INDX){
					new LimniPanel(node.toString(),true);
				}
				if(SwingUtilities.isRightMouseButton(me)){
					if(indx!=MainFrame.REMNANT_INDX){
						DataSetPopUpMenu popup =new DataSetPopUpMenu(node);
						popup.show(this,me.getX(), me.getY());
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent me) {

	}

	@Override
	public void mouseReleased(MouseEvent me) {
		/*
		TreePath selPath = this.getPathForLocation(me.getX(), me.getY());
		if (selPath != null) {
			if (SwingUtilities.isRightMouseButton(me)) {
				this.setSelectionPath(selPath);   //to allow the selection in the tree by right-clicking on it
			}
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			MainFrame.getInstance().getTabs().setSelectedIndex(node.getParent().getIndex(node));
		}
		 */
		// TODO à compléter
	}

	public DataSetTreeModel getDstModel() {
		return dstModel;
	}

	public void setDstModel(DataSetTreeModel dstModel) {
		this.dstModel = dstModel;
	}

}
