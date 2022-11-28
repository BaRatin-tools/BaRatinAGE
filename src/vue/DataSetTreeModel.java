package vue;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import Utils.Config;
import Utils.Dico;
import Utils.Dictionnary;

@SuppressWarnings("serial")
public class DataSetTreeModel extends DefaultTreeModel {

	private DefaultMutableTreeNode treeRootNode = new DefaultMutableTreeNode(
			Dico.getInstance(null).entry("Catalogues"));
	private DefaultMutableTreeNode configTreeNode = new DefaultMutableTreeNode(
			Dico.getInstance(null).entry("HydrauConf"));
	private DefaultMutableTreeNode gaugingTreeNode = new DefaultMutableTreeNode(
			Dico.getInstance(null).entry("Gaugings"));
	private DefaultMutableTreeNode errorTreeNode = new DefaultMutableTreeNode(
			Dico.getInstance(null).entry("RemnantError"));
	private DefaultMutableTreeNode limniTreeNode = new DefaultMutableTreeNode(
			Dico.getInstance(null).entry("Limnigraph"));

	public DataSetTreeModel() {
		// An empty default tree node as root - do not use treeRootNode otherwise
		// requires defining it as static
		super(new DefaultMutableTreeNode());
		// At this point "this" contains : (1) the five nodes above, completely
		// independent; (2) the default tree model just created, with a dummy root that
		// will never be used
		// Now add thematic nodes to the "custom" root
		this.treeRootNode.add(configTreeNode);
		this.treeRootNode.add(gaugingTreeNode);

		if (Config.getInstance().isExpertMode()) { // if the application is currently running in expert mode, this node
													// is required to display the errors
			this.treeRootNode.add(errorTreeNode);
		}

		this.treeRootNode.add(limniTreeNode);
		// And finally, set the root of the tree to our custom root, that now contains
		// all thematic nodes!
		this.setRoot(treeRootNode); // the setting of the real root of the tree after adding the first level of
									// children
	}

	public DataSetTreeModel(TreeNode root) {
		super(root);
	}

	public DataSetTreeModel(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}

	public void addConfigNode(String name) {
		this.configTreeNode.add(new DefaultMutableTreeNode(name));
		this.reload();
	}

	public void deleteConfigNode(int indx) {
		this.configTreeNode.remove(indx);
		this.reload();
	}

	public void addGaugingNode(String name) {
		this.gaugingTreeNode.add(new DefaultMutableTreeNode(name));
		this.reload();
	}

	public void deleteGaugingNode(int indx) {
		this.gaugingTreeNode.remove(indx);
		this.reload();
	}

	public void addLimniNode(String name) {
		this.limniTreeNode.add(new DefaultMutableTreeNode(name));
		this.reload();
	}

	public void deleteLimniNode(int indx) {
		this.limniTreeNode.remove(indx);
		this.reload();
	}

	public void addErrorNode(String name) {
		this.errorTreeNode.add(new DefaultMutableTreeNode(name));
		this.reload();
	}

	public void deleteErrorNode(int indx) {
		this.errorTreeNode.remove(indx);
		this.reload(); // force la r�actualisation, impliquant une mise � jour du rendu du JTree
	}

	public void updateToExpertMode() {
		try {
			this.treeRootNode.insert(errorTreeNode, 2);
			this.reload();
		} catch (ArrayIndexOutOfBoundsException e) {
			String error = Dictionnary.getInstance().getErrorOccurred() + ": " + e.getMessage();
			new ExceptionPanel(null, error);
		} catch (IllegalArgumentException e) {
			String error = Dictionnary.getInstance().getErrorOccurred() + ": " + e.getMessage();
			new ExceptionPanel(null, error);
		} catch (IllegalStateException e) {
			String error = Dictionnary.getInstance().getErrorOccurred() + ": " + e.getMessage();
			new ExceptionPanel(null, error);
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the treeRoot
	 */
	public DefaultMutableTreeNode getTreeRootNode() {
		return treeRootNode;
	}

	/**
	 * @param treeRoot the treeRoot to set
	 */
	public void setTreeRootNode(DefaultMutableTreeNode treeRoot) {
		this.treeRootNode = treeRoot;
	}

	/**
	 * @return the gaugingTreeNode
	 */
	public DefaultMutableTreeNode getGaugingTreeNode() {
		return gaugingTreeNode;
	}

	/**
	 * @param gaugingTreeNode the gaugingTreeNode to set
	 */
	public void setGaugingTreeNode(DefaultMutableTreeNode gaugingTreeNode) {
		this.gaugingTreeNode = gaugingTreeNode;
	}

	/**
	 * @return the configTreeNode
	 */
	public DefaultMutableTreeNode getConfigTreeNode() {
		return configTreeNode;
	}

	/**
	 * @param configTreeNode the configTreeNode to set
	 */
	public void setConfigTreeNode(DefaultMutableTreeNode configTreeNode) {
		this.configTreeNode = configTreeNode;
	}

	/**
	 * @return the limniTreeNode
	 */
	public DefaultMutableTreeNode getLimniTreeNode() {
		return limniTreeNode;
	}

	/**
	 * @param limniTreeNode the limniTreeNode to set
	 */
	public void setLimniTreeNode(DefaultMutableTreeNode limniTreeNode) {
		this.limniTreeNode = limniTreeNode;
	}

	/**
	 * @return the errorTreeNode
	 */
	public DefaultMutableTreeNode getErrorTreeNode() {
		return errorTreeNode;
	}

	/**
	 * @param errorTreeNode the errorTreeNode to set
	 */
	public void setErrorTreeNode(DefaultMutableTreeNode errorTreeNode) {
		this.errorTreeNode = errorTreeNode;
	}

}
