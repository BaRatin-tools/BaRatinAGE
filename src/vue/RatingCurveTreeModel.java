package vue;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import Utils.Dico;

@SuppressWarnings("serial")
public class RatingCurveTreeModel extends DefaultTreeModel {

	private DefaultMutableTreeNode treeRootNode = new DefaultMutableTreeNode(Dico.getInstance(null).entry("RatingCurve"));

	public RatingCurveTreeModel() {
		 // An empty default tree node as root - do not use treeRootNode otherwise requires defining it as static
		super(new DefaultMutableTreeNode());       
		this.setRoot(treeRootNode);
	}

	public RatingCurveTreeModel(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		// TODO Auto-generated constructor stub
	}

	public void addNode(String name){
		this.treeRootNode.add(new DefaultMutableTreeNode(name));
		this.reload();
	}

	public void deleteNode(int indx){
		this.treeRootNode.remove(indx);
		this.reload();
	}

}
