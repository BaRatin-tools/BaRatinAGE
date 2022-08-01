package vue;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.GridBag_Label;
import commons.GridBag_Layout;
import commons.GridBag_Separator;
import commons.GridBag_SplitPanel;

@SuppressWarnings("serial")
public class TreesPanel extends JPanel {
	
	private DataSetTree tree_cat;
	private RatingCurveTree tree_RC;
	private HydroTree tree_HYD;
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	
	public TreesPanel(){
		super();
		GridBag_Layout.SetGrid(this,new int[] {50,0,0}, new int[] {0}, new double[] {0.0,0.0,1.0},new double[] {1.0});
		
		// header
		new GridBag_Label(this,dico.entry("Xplorer"),config.getFontCatLbl(),Defaults.xplorerLblColor,SwingConstants.CENTER,0,0,1,1,true,false);
		new GridBag_Separator(this,0,1,1,true);
		
		// split panels
		GridBag_SplitPanel split1=new GridBag_SplitPanel(this,JSplitPane.VERTICAL_SPLIT,0.333,0,2,1,1);
		JSplitPane split2=new JSplitPane();
		split2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split2.setResizeWeight(0.5);
		split1.setRightComponent(split2);		
		
		// Catalogues panel, containing the Dataset tree (hydrau+gauging+error+limni)
		JScrollPane scroll_cat=new JScrollPane();
		split1.setLeftComponent(scroll_cat);
		tree_cat=new DataSetTree();
		CreateTreePanel(scroll_cat,tree_cat);

		// CT panel, containing the CT tree
		JScrollPane scroll_CT=new JScrollPane();
		split2.setLeftComponent(scroll_CT);
		tree_RC=new RatingCurveTree();
		CreateTreePanel(scroll_CT,tree_RC);

		// Hydro panel, containing the Hydro tree
		JScrollPane scroll_HYD=new JScrollPane();
		split2.setRightComponent(scroll_HYD);
		tree_HYD=new HydroTree();
		CreateTreePanel(scroll_HYD,tree_HYD);
	}
	
	private void CreateTreePanel(JScrollPane scroll,JTree tree){
		scroll.setVerticalScrollBarPolicy(Defaults.scrollV);
		scroll.setHorizontalScrollBarPolicy(Defaults.scrollH);
		JPanel pan=new JPanel();pan.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan,new int[] {0,0},new int[] {0,0},new double[] {0.0,1.0},new double[] {0.0,1.0});
		scroll.setViewportView(pan);
		tree.setFont(config.getFontTree());
		tree.setRowHeight(0);
		tree.setShowsRootHandles(true);
		GridBagConstraints gbc_tree = new GridBagConstraints();
		gbc_tree.anchor = GridBagConstraints.WEST;gbc_tree.fill = GridBagConstraints.VERTICAL;
		gbc_tree.gridx = 0;	gbc_tree.gridy = 0;
		pan.add(tree,gbc_tree);
	}

	public DataSetTree getTree_cat() {
		return tree_cat;
	}

	public void setTree_cat(DataSetTree tree_cat) {
		this.tree_cat = tree_cat;
	}

	public RatingCurveTree getTree_RC() {
		return tree_RC;
	}

	public void setTree_RC(RatingCurveTree tree_CT) {
		this.tree_RC = tree_CT;
	}

	public HydroTree getTree_HYD() {
		return tree_HYD;
	}

	public void setTree_HYD(HydroTree tree_HYD) {
		this.tree_HYD = tree_HYD;
	}
}
