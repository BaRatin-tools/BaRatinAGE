package vue;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import controleur.Control;
import Utils.Config;
import Utils.Dico;

@SuppressWarnings("serial")
public class DataSetPopUpMenu extends JPopupMenu implements ActionListener{

	private JMenuItem add = new JMenuItem();
	private JMenuItem deleteAll = new JMenuItem();
	private JMenuItem delete = new JMenuItem();
	private JMenuItem rename = new JMenuItem();
	private JMenuItem update = new JMenuItem();
	private JMenuItem export = new JMenuItem();
	private JMenuItem exportEq = new JMenuItem();
	private JMenuItem exportMCMC = new JMenuItem();
	private JMenuItem duplicate = new JMenuItem();
	/*
	private String file;
	private String name;
	 */
	private Object obj;
	//locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public DataSetPopUpMenu() throws HeadlessException {
	}

	public DataSetPopUpMenu(String arg0) throws HeadlessException {
		super(arg0);
	}

	public DataSetPopUpMenu(Integer code) throws HeadlessException {
		super();
		add.setText(dico.entry("Add"));
		this.add(add);
		add.addActionListener(this);
		/*
		deleteAll.setText(dico.entry("DeleteAll"));
		this.add(deleteAll);
		deleteAll.addActionListener(this);
		 */
		obj = code;
	}

	public DataSetPopUpMenu(DefaultMutableTreeNode node) throws HeadlessException {
		super();
		delete.setText(dico.entry("Delete"));
		this.add(delete);
		delete.addActionListener(this);
		rename.setText(dico.entry("Rename"));
		this.add(rename);
		rename.addActionListener(this);
		duplicate.setText(dico.entry("Duplicate"));
		this.add(duplicate);
		duplicate.addActionListener(this);
		obj = node;
	}

	public DataSetPopUpMenu(DefaultMutableTreeNode node, int Xtend) throws HeadlessException {
		this(node);
		if(Xtend>0){ // Export values for RC and Q(t)
			export.setText(dico.entry("ExportValues"));
			this.add(export);
			export.addActionListener(this);
		}
		if(Xtend>1){ // Export RC equation and MCMC simulations
			exportEq.setText(dico.entry("ExportEquation"));
			this.add(exportEq);
			exportEq.addActionListener(this);
			exportMCMC.setText(dico.entry("ExportMCMC"));
			this.add(exportMCMC);
			exportMCMC.addActionListener(this);
		}
		obj = node;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(add)){
			Integer tmp = (Integer)obj;
			if (tmp.equals(MainFrame.HYDRAULIC_INDX)){
				MainFrame.getInstance().addConfig(true,null,null);
			}
			else if (tmp.equals(MainFrame.GAUGING_INDX)){
				MainFrame.getInstance().addGaugingSet(true,null,null);
			}
			else if (tmp.equals(MainFrame.REMNANT_INDX)){

			}
			else if (tmp.equals(MainFrame.LIMNI_INDX)){
				MainFrame.getInstance().addLimni(true,null,null);
			}
			else if (tmp.equals(MainFrame.RC_INDX)){
				MainFrame.getInstance().addRatingCurve(true,null,null);
			}
			else if (tmp.equals(MainFrame.HYDROGRAPH_INDX)){
				MainFrame.getInstance().addHydrograph(true,null,null);
			}
		}
		else if (ae.getSource().equals(duplicate)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Integer tmp=node.getRoot().getIndex(node.getParent());
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			Object HYDroot = MainFrame.getInstance().getTrees().getTree_HYD().getModel().getRoot();		
			if(tmp>=0){ // source is a basic object
				if (tmp.equals(MainFrame.HYDRAULIC_INDX)){
					controller.duplicateHydrauConfig(node.toString());
				}
				else if (tmp.equals(MainFrame.GAUGING_INDX)){
					controller.duplicateGauging(node.toString());
				}
				else if (tmp.equals(MainFrame.REMNANT_INDX)){

				}
				else if (tmp.equals(MainFrame.LIMNI_INDX)){
					controller.duplicateLimni(node.toString());
				}
			} else { // source is a derived object (RC, HYD)
				if(RCroot!=null){
					if (node.getParent().equals(RCroot)){controller.duplicateRatingCurve(node.toString());}
				}	
				if(HYDroot!=null){
					if (node.getParent().equals(HYDroot)){controller.duplicateHydrograph(node.toString());}
				} 
			}
		}
		else if (ae.getSource().equals(delete)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Integer tmp=node.getRoot().getIndex(node.getParent());
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			Object HYDroot = MainFrame.getInstance().getTrees().getTree_HYD().getModel().getRoot();		
			if(tmp>=0){ // source is a basic object
				if (tmp.equals(MainFrame.HYDRAULIC_INDX)){controller.deleteHydrauConfig(node.toString());}
				else if (tmp.equals(MainFrame.GAUGING_INDX)){controller.deleteGaugingSet(node.toString());}
				else if (tmp.equals(MainFrame.LIMNI_INDX)){controller.deleteLimni(node.toString());}
			}
			else
			{ // source is a derived object (RC, HYD)
				if(RCroot!=null){
					if (node.getParent().equals(RCroot)){controller.deleteRatingCurve(node.toString());}
				}
				if(HYDroot!=null){
					if (node.getParent().equals(HYDroot)){controller.deleteHydrograph(node.toString());}
				}
			}
		}
		else if (ae.getSource().equals(rename)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Integer tmp=node.getRoot().getIndex(node.getParent());
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			Object HYDroot = MainFrame.getInstance().getTrees().getTree_HYD().getModel().getRoot();		
			if(tmp>=0){ // source is a basic object
				if (tmp.equals(MainFrame.HYDRAULIC_INDX)){controller.renameHydrauConfig(node.toString());}
				else if (tmp.equals(MainFrame.GAUGING_INDX)){controller.renameGaugingSet(node.toString());}
				else if (tmp.equals(MainFrame.LIMNI_INDX)){controller.renameLimni(node.toString());}
			}
			else
			{ // source is a derived object (RC, HYD)
				if(RCroot!=null){
					if (node.getParent().equals(RCroot)){controller.renameRatingCurve(node.toString());}
				}
				if(HYDroot!=null){
					if (node.getParent().equals(HYDroot)){controller.renameHydrograph(node.toString());}
				}
			}
		}
		else if (ae.getSource().equals(export)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			Object HYDroot = MainFrame.getInstance().getTrees().getTree_HYD().getModel().getRoot();	
			if(RCroot!=null){
				if (node.getParent().equals(RCroot)){controller.exportRatingCurve(node.toString());}
			}
			if(HYDroot!=null){
				if (node.getParent().equals(HYDroot)){controller.exportHydrograph(node.toString());}
			}
		}
		else if (ae.getSource().equals(exportEq)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			if(RCroot!=null){
				if (node.getParent().equals(RCroot)){controller.exportRatingCurveEquation(node.toString());}
			}
		}
		else if (ae.getSource().equals(exportMCMC)){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Object RCroot = MainFrame.getInstance().getTrees().getTree_RC().getModel().getRoot();
			if(RCroot!=null){
				if (node.getParent().equals(RCroot)){controller.exportMCMC(node.toString());}
			}
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public JMenuItem getAdd() {
		return add;
	}

	public void setAdd(JMenuItem add) {
		this.add = add;
	}

	public JMenuItem getDelete() {
		return delete;
	}

	public void setDelete(JMenuItem delete) {
		this.delete = delete;
	}

	public JMenuItem getUpdate() {
		return update;
	}

	public void setUpdate(JMenuItem update) {
		this.update = update;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public JMenuItem getDeleteAll() {
		return deleteAll;
	}

	public void setDeleteAll(JMenuItem deleteAll) {
		this.deleteAll = deleteAll;
	}

	public JMenuItem getRename() {
		return rename;
	}

	public void setRename(JMenuItem rename) {
		this.rename = rename;
	}

	public JMenuItem getExport() {
		return export;
	}

	public void setExport(JMenuItem export) {
		this.export = export;
	}

	public JMenuItem getDuplicate() {
		return duplicate;
	}

	public void setDuplicate(JMenuItem duplicate) {
		this.duplicate = duplicate;
	}

}
