package vue;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import commons.GridBag_Layout;
import Utils.Defaults;

@SuppressWarnings("serial")
public abstract class ItemPanel extends JSplitPane{	
	//classe abstraite, mère de tous les panneaux thématiques, gaugingPane, TimeSeriesPane...

	private JPanel InfoPanel;
	private JPanel GraphPanel;
	
	public ItemPanel(
			int[] sizeRow, 
			int[] sizeCol, 
			double[] weightRow, 
			double[] weightCol,
			int[] sizeRow_graph, 
			int[] sizeCol_graph, 
			double[] weightRow_graph, 
			double[] weightCol_graph) {
		super();
		this.setResizeWeight(0.5);
		this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		// top info panel
		InfoPanel=new JPanel();
		GridBag_Layout.SetGrid(InfoPanel,sizeRow,sizeCol,weightRow,weightCol);
		InfoPanel.setBackground(Defaults.bkgColor);
		this.setLeftComponent(InfoPanel);
		// bottom graph panel
		GraphPanel=new JPanel();
		GridBag_Layout.SetGrid(GraphPanel,sizeRow_graph,sizeCol_graph,weightRow_graph,weightCol_graph);
		GraphPanel.setBackground(Defaults.bkgColor);
		this.setRightComponent(GraphPanel);
	}

	public JPanel getInfoPanel() {
		return InfoPanel;
	}

	public void setInfoPanel(JPanel infoPanel) {
		InfoPanel = infoPanel;
	}

	public JPanel getGraphPanel() {
		return GraphPanel;
	}

	public void setGraphPanel(JPanel graphPanel) {
		GraphPanel = graphPanel;
	}

/*	public ItemPanel(LayoutManager arg0) {
		super(arg0);
	}

	public ItemPanel(boolean arg0) {
		super(arg0);
	}

	public ItemPanel(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
	}
*/	

}
