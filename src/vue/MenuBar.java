package vue;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.Custom_Menu;
import commons.Custom_MenuBar;

@SuppressWarnings("serial")
public class MenuBar extends Custom_MenuBar{
	
	private ArrayList<Custom_Menu> menus;
	
	private static final String[] Main_Menus={"File","HydrauConf","Gaugings","Limnigraph","RatingCurve","Hydrograph","Options","Help"};
	private static final String[] File_IDs={"Open","Save","SaveAs","New","Quit"};
	private static final String[] File_names={"Open","Save","SaveAs","New","Quit"};
	private static final String[] HydrauConf_IDs={"AddConfig","DeleteConfig","RenameConfig","DuplicateConfig"};
	private static final String[] HydrauConf_names={"Add","Delete","Rename","Duplicate"};
	private static final String[] Gaugings_IDs={"AddGauging","DeleteGauging","RenameGauging","DuplicateGauging"};
	private static final String[] Gaugings_names={"Add","Delete","Rename","Duplicate"};
	private static final String[] Limnigraph_IDs={"AddLimni","DeleteLimni","RenameLimni","DuplicateLimni"};
	private static final String[] Limnigraph_names={"Add","Delete","Rename","Duplicate"};
	private static final String[] RatingCurve_IDs={"AddRC","DeleteRC","RenameRC","DuplicateRC","ExportRC","ExportRCeq","ExportMCMC"};
	private static final String[] RatingCurve_names={"Add","Delete","Rename","Duplicate","ExportValues","ExportEquation","ExportMCMC"};
	private static final String[] Hydrograph_IDs={"AddHydro","DeleteHydro","RenameHydro","DuplicateHydro","ExportHydro"};
	private static final String[] Hydrograph_names={"Add","Delete","Rename","Duplicate","ExportValues"};
	private static final String[] Options_IDs={"Language","Preferences","DefaultDirectory","MCMCoptions","SaveOptions"};
	private static final String[] Options_names={"Language","Preferences","DefaultDirectory","MCMCoptions","SaveOptions"};
	private static final String[] Help_IDs={"Help","About"};
	private static final String[] Help_names={"Help","About"};

	public MenuBar(JFrame container,ActionListener listener,Dico dico){
		super(container,dico.entry(Main_Menus),Config.getInstance().getFontMenu(),Defaults.menuSep);
		Font font=Config.getInstance().getFontMenuItem();
		int k;
		// Add items to FILE menus
		k=0;
		this.getMenu(k).AddItem(File_IDs,dico.entry(File_names),font,listener);
		// Add items to Hydrau menus
		k=k+1;
		this.getMenu(k).AddItem(HydrauConf_IDs,dico.entry(HydrauConf_names),font,listener);
		// Add items to Gaugings menus
		k=k+1;
		this.getMenu(k).AddItem(Gaugings_IDs,dico.entry(Gaugings_names),font,listener);
		// Add items to Limni menus
		k=k+1;
		this.getMenu(k).AddItem(Limnigraph_IDs,dico.entry(Limnigraph_names),font,listener);
		// Add items to RC menus
		k=k+1;
		this.getMenu(k).AddItem(RatingCurve_IDs,dico.entry(RatingCurve_names),font,listener);
		// Add items to Hydrograph menus
		k=k+1;
		this.getMenu(k).AddItem(Hydrograph_IDs,dico.entry(Hydrograph_names),font,listener);
		// Add items to OPTIONS menus
		k=k+1;
		this.getMenu(k).AddItem(Options_IDs,dico.entry(Options_names),font,listener);
		// Add items to HELP menus
		k=k+1;
		this.getMenu(k).AddItem(Help_IDs,dico.entry(Help_names),font,listener);
	}

	public ArrayList<Custom_Menu> getMenus() {
		return menus;
	}

	public Custom_Menu getMenus(int i) {
		return menus.get(i);
	}

	public void setMenus(ArrayList<Custom_Menu> menus) {
		this.menus = menus;
	}

}
