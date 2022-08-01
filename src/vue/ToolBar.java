package vue;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import Utils.Defaults;
import Utils.Dico;
import commons.GridBag_Button;
import commons.GridBag_ToolBar;

@SuppressWarnings("serial")
public class ToolBar extends GridBag_ToolBar {
	
	private ArrayList<GridBag_Button> buttons;
	
	private static final String[] IDs={"Open","Save","New","AddConfig","AddGauging","AddLimni",
		"AddRC","AddHydro","Delete","Help"};
	private static final String[] tips={"Open","Save","New","AddConfig","AddGauging","AddLimni",
		"AddRC","AddHydro","DeleteItem","Help"};
	private static final String[] icons={Defaults.iconOpen,Defaults.iconSave,Defaults.iconNew,
		Defaults.iconHydraulic,Defaults.iconGauging,Defaults.iconLimni, Defaults.iconRC,
		Defaults.iconHydro,Defaults.iconDelete,Defaults.iconHelp};
	
	public ToolBar(Container container,ActionListener listener,Dico dico){
		super(container,listener,IDs,icons,0,0,1,1,dico.entry(tips));
		this.getButton().get(8).setVisible(false); // Do not show delete button - tricky to use due to multiple selections in tree panel 
	}

	public ArrayList<GridBag_Button> getButtons() {
		return buttons;
	}

	public void setButtons(ArrayList<GridBag_Button> buttons) {
		this.buttons = buttons;
	}
}
