package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import commons.Constants;
import commons.GridBag_Button;
import commons.GridBag_Label;
import commons.GridBag_Layout;
import commons.GridBag_SplitPanel;
import commons.GridBag_TextField_Titled;
import commons.GridBag_Text_Titled;
import controleur.Control;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;

@SuppressWarnings("serial")
public class Frame_PriorAssistant extends JDialog implements ActionListener {

	private int type;
	private ControlPanel control;
	private ConfigHydrauPanel hydrau;
	private GridBag_Text_Titled k;
	private GridBag_Text_Titled kpom;
	private GridBag_Text_Titled a;
	private GridBag_Text_Titled apom;
	private GridBag_Text_Titled c;
	private GridBag_Text_Titled cpom;
	private GridBag_Button butt_propa;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_cancel;
	private GridBag_Button butt_J;
	private GridBag_Button butt_M;// For Manning instead of Strickler
	private GridBag_TextField_Titled[][] uncertainties;
	private JDialog dialog_J;
	private JDialog dialog_M;
	private GridBag_Button butt_apply_J;
	private GridBag_Button butt_cancel_J;
	private GridBag_Button butt_apply_M;
	private GridBag_Button butt_cancel_M;
	private GridBag_TextField_Titled[][] u_J;
	private GridBag_TextField_Titled[][] u_M;


	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public Frame_PriorAssistant(JFrame parent,int type,ControlPanel control,ConfigHydrauPanel hydrau){
		super(parent,true);
		this.type=type;
		this.control=control;
		this.hydrau=hydrau;
		// decoration
		this.setTitle(dico.entry("PriorAssistant"));
		this.setSize(Defaults.priorAssistantSize);
		this.setLocationRelativeTo(null);
		this.getContentPane().setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(this.getContentPane(),new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		ImageIcon icon=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconPriorAssistant));
		this.setIconImage(icon.getImage());
		// basic containers
		GridBag_SplitPanel split_1=new GridBag_SplitPanel(this.getContentPane(),JSplitPane.VERTICAL_SPLIT,0.2,0,0,1,1);
		JPanel pan_equation=new JPanel();
		pan_equation.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_equation,new int[] {0,0}, new int[] {0,0,0,0,0}, new double[] {1.0,1.0},new double[] {0.0,1.0,1.0,1.0,0.0});
		JPanel pan_low=new JPanel();
		pan_low.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_low,new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		GridBag_SplitPanel split_2=new GridBag_SplitPanel(pan_low,JSplitPane.VERTICAL_SPLIT,0.8,0,0,1,1);
		split_1.setLeftComponent(pan_equation);
		split_1.setRightComponent(split_2);
		JPanel pan_butt=new JPanel();
		pan_butt.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_butt,new int[] {0,0}, new int[] {0}, new double[] {0.0,0.0},new double[] {0.0});
		GridBag_SplitPanel split_3=new GridBag_SplitPanel(pan_low,JSplitPane.HORIZONTAL_SPLIT,0.5,0,0,1,1);
		split_2.setLeftComponent(split_3);
		split_2.setRightComponent(pan_butt);
		JPanel pan_left=new JPanel();
		pan_left.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_left,new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		JPanel pan_right=new JPanel();
		pan_right.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_right,new int[] {0,0,0}, new int[] {0,0,0}, new double[] {0.0,0.0,0.0},new double[] {0.0,1.0,1.0});
		split_3.setLeftComponent(pan_left);
		split_3.setRightComponent(pan_right);
		// Equation
		GridBag_Label lab1=new GridBag_Label(pan_equation,"<html>Q(h)=a(h-b)<sup>c</sup> (h>k)</html>",config.getFontBigLbl(),Defaults.txtColor,
				SwingConstants.CENTER,2,0,1,1,true,true);
		lab1.setOpaque(true);
		lab1.setBackground(Defaults.bkgColor);		
		GridBag_Label lab2=new GridBag_Label(pan_equation,"",config.getFontBigTxt(),Defaults.txtColor,
				SwingConstants.CENTER,2,1,1,1,true,true);
		lab2.setOpaque(true);
		lab2.setBackground(Defaults.bkgColor);
		GridBag_Label leftarrow=new GridBag_Label(pan_equation,"",config.getFontBigTxt(),Defaults.txtColor,
				SwingConstants.CENTER,0,1,1,1,true,true);
		leftarrow.setIcon(new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconSW)));
		GridBag_Label lab_left=new GridBag_Label(pan_equation,dico.entry("PhysicalParameters"),config.getFontBigLbl(),Defaults.txtColor,
				SwingConstants.LEFT,1,1,1,1,true,false);
		lab_left.setOpaque(true);
		lab_left.setBackground(Defaults.bkgColor);	
		GridBag_Label lab_right=new GridBag_Label(pan_equation,dico.entry("RatingCurveParameters"),config.getFontBigLbl(),Defaults.txtColor,
				SwingConstants.RIGHT,3,1,1,1,true,true);
		lab_right.setOpaque(true);
		lab_right.setBackground(Defaults.bkgColor);		
		GridBag_Label rightarrow=new GridBag_Label(pan_equation,"",config.getFontBigTxt(),Defaults.txtColor,
				SwingConstants.CENTER,4,1,1,1,true,true);
		rightarrow.setIcon(new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconSE)));
		// Propagation button
		butt_propa= new GridBag_Button(pan_right,this,"butt_propa",
				dico.entry("Compute"),
				Defaults.iconPropagate,
				0,2,1,1,false,true,dico.entry("PropagateUncertainty"));
		// Output right panel
		k=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("kpar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,1,1,1);
		kpom=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,2,1,1,1);
		a=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("apar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,2,1,1);
		apom=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,2,2,1,1);
		c=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("cpar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,3,1,1);
		cpom=new GridBag_Text_Titled(pan_right,Constants.S_BLANK,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,2,3,1,1);
		// Apply/Cancel buttons
		butt_apply=new GridBag_Button(pan_butt,this,"butt_apply",
				dico.entry("Apply"),Defaults.iconApply,
				0,0,1,1,false,false,dico.entry("Apply"));
		butt_cancel=new GridBag_Button(pan_butt,this,"butt_cancel",
				dico.entry("Cancel"),Defaults.iconCancel,
				1,0,1,1,false,false,dico.entry("Cancel"));
		// type-specific stuff
		Combo_ControlType[] list=Combo_ControlType.getList();
		if(list[type].getTxt().equals("RectangularChannel")){uncertainties=fill_Channel(lab2,pan_left,"Rectangular");}
		if(list[type].getTxt().equals("TriangularChannel")){uncertainties=fill_Channel(lab2,pan_left,"Triangular");}
		if(list[type].getTxt().equals("ParabolicChannel")){uncertainties=fill_Channel(lab2,pan_left,"Parabolic");}
		if(list[type].getTxt().equals("RectangularSill")){uncertainties=fill_RectangularSill(lab2,pan_left);}
		if(list[type].getTxt().equals("TriangularSill")){uncertainties=fill_TriangularSill(lab2,pan_left);}
		if(list[type].getTxt().equals("ParabolicSill")){uncertainties=fill_ParabolicSill(lab2,pan_left);}
		if(list[type].getTxt().equals("Orifice")){uncertainties=fill_Orifice(lab2,pan_left);}
		controller.fillPriorAssistant(this);
		// Show off!
		split_3.setResizeWeight(0.5);
		this.setVisible(true);
	}

	private GridBag_TextField_Titled[][]  fill_Channel(GridBag_Label lab,JPanel pan,String type){

		int dim;
		if(type.equals("Parabolic")){dim=6;} else {dim=5;}
		GridBag_TextField_Titled[][] u= new GridBag_TextField_Titled[dim][2];

		String foo1 = null,foo2,txt;
		if(config.isUseManning()){
			if(type.equals("Rectangular")){foo1="a=n<sup>-1</sup>.B<sub>w</sub>.\u221AS";}
			if(type.equals("Triangular")){foo1="a=n<sup>-1</sup>.tan(v/2).(0.5sin(v/2))<sup>2/3</sup>.\u221AS";}
			if(type.equals("Parabolic")){foo1="a=n<sup>-1</sup>.B<sub>p</sub>.H<sub>p</sub><sup>-1/2</sup>.(2/3)<sup>5/3</sup>.\u221AS";}
			foo2="n: "+dico.entry("Explanation_Manning");
			}
		else {
			if(type.equals("Rectangular")){foo1="a=K<sub>S</sub>.B<sub>w</sub>.\u221AS";}
			if(type.equals("Triangular")){foo1="a=K<sub>S</sub>.tan(v/2).(0.5sin(v/2))<sup>2/3</sup>.\u221AS";}
			if(type.equals("Parabolic")){foo1="a= K<sub>S</sub>.B<sub>p</sub>.H<sub>p</sub><sup>-1/2</sup>.(2/3)<sup>5/3</sup>.\u221AS";}
			foo2= "K<sub>S</sub>: " + dico.entry("Explanation_K");
			}
		txt="<html>"
				+ "<hr>"
				+ foo1
				+ "<br>"
				+ foo2
				+ "<br>";
		
		Double expo=1.67;
		String explanation_c="";
		if(type.equals("Rectangular")){expo=1.67;explanation_c="Explanation_cchannel_rectangle";}
		if(type.equals("Triangular")){expo=2.67;explanation_c="Explanation_cchannel_triangle";}
		if(type.equals("Parabolic")){expo=2.17;explanation_c="Explanation_cchannel_parabola";}

		if(type.equals("Rectangular")){txt=txt+"B<sub>w</sub>: " + dico.entry("Explanation_Lchannel");}
		if(type.equals("Triangular")){txt=txt+"v: " + dico.entry("Explanation_Vchannel");}
		if(type.equals("Parabolic")){txt=txt+"B<sub>p</sub>: " + dico.entry("Explanation_Bpchannel")
				+ "<br>" + "H<sub>p</sub>: " + dico.entry("Explanation_Hpchannel");}
		txt=txt	+ "<br>"
				+ "S=|z<sub>X</sub>-z<sub>Y</sub>|/dist(X,Y): " + dico.entry("Explanation_J")
				+ "<hr>"
				+ "c: " + dico.entry(explanation_c)
				+ "<hr>"
				+ "k: " + dico.entry("Explanation_k")
				+ "<hr>"
				+ "b: " + dico.entry("Explanation_b")
				+ "<hr>"
				+ "</html>";
		lab.setText(txt);

		int[] ny;
		double[]wy;
		if(type.equals("Parabolic")){ny=new int[]{0,0,0,0,0,0};wy=new double[]{0.0,0.0,0.0,0.0,0.0,0.0};} 
		else {ny=new int[]{0,0,0,0,0};wy=new double[]{0.0,0.0,0.0,0.0,0.0};}
		GridBag_Layout.SetGrid(pan,ny,new int[]{0,0,0},wy,new double[]{1.0,1.0,0.0});

		// textboxes
		int k=0;
		if(config.isUseManning()){
			doTextboxPair(k,pan,u,"","<html>n [s.m<sup>-1/3</sup>]</html>",dico.entry("Explanation_Manning"),"");
			butt_M=new GridBag_Button(pan,this,"butt_M",
					dico.entry("Strickler"),null,
					2,k,1,1,true,true,dico.entry("butt_M_info_K"));
		}
		else{
			doTextboxPair(k,pan,u,"","<html>K<sub>S</sub> [m<sup>1/3</sup>.s<sup>-1</sup>]</html>",dico.entry("Explanation_K"),"");
			butt_M=new GridBag_Button(pan,this,"butt_M",
					dico.entry("Manning"),null,
					2,k,1,1,true,true,dico.entry("butt_M_info_n"));
		}
		k=k+1;
		if(type.equals("Rectangular")){
			doTextboxPair(k,pan,u,"","<html>B<sub>w</sub> [m]</html>",dico.entry("Explanation_Lchannel"),"");}
		if(type.equals("Triangular")){
			doTextboxPair(k,pan,u,"","<html>v [<sup>o</sup>]</html>",dico.entry("Explanation_Vchannel"),"");}
		if(type.equals("Parabolic")){
			doTextboxPair(k,pan,u,"","<html>B<sub>p</sub> [m]</html>",dico.entry("Explanation_Bpchannel"),"");
			k=k+1;
			doTextboxPair(k,pan,u,"","<html>H<sub>p</sub> [m]</html>",dico.entry("Explanation_Hpchannel"),"");
			}		
		k=k+1;	
		doTextboxPair(k,pan,u,"","S [-]",dico.entry("Explanation_J"),"");
		butt_J=new GridBag_Button(pan,this,"butt_J",
				dico.entry("butt_J_text"),null,
				2,k,1,1,true,true,dico.entry("butt_J_info"));
		k=k+1;
		doTextboxPair(k,pan,u,Double.toString(expo),"c [-]",dico.entry(explanation_c),"0.05");
		k=k+1;		
		doTextboxPair(k,pan,u,"","k [m]",dico.entry("Explanation_k"),"");
		return u;
	}

	private GridBag_TextField_Titled[][] fill_RectangularSill(GridBag_Label lab,JPanel pan){
		GridBag_TextField_Titled[][] u= new GridBag_TextField_Titled[5][2];

		lab.setText("<html>"
				+ "<hr>"
				+ "a=C<sub>r</sub>.B<sub>w</sub>.\u221A(2g)"
				+ "<br>"
				+ "C<sub>r</sub>: " + dico.entry("Explanation_frectangle")
				+ "<br>"
				+ "B<sub>w</sub>: " + dico.entry("Explanation_Lrectangle")
				+ "<br>"
				+ "g: " + dico.entry("Explanation_g")
				+ "<hr>"
				+ "c: " + dico.entry("Explanation_crectangle")
				+ "<hr>"
				+ "k: " + dico.entry("Explanation_k")
				+ "<hr>"
				+ "b: " + dico.entry("Explanation_b")
				+ "<hr>"
				+ "</html>");

		GridBag_Layout.SetGrid(pan,new int[]{0,0,0,0,0},new int[]{0,0},
				new double[]{0.0,0.0,0.0,0.0,0.0},new double[]{1.0,1.0});
		// textboxes
		int k=0;		
		doTextboxPair(k,pan,u,"0.4","<html>C<sub>r</sub> [-]</html>",dico.entry("Explanation_frectangle"),"0.1");
		k=k+1;
		doTextboxPair(k,pan,u,"","<html>B<sub>w</sub> [m]</html>",dico.entry("Explanation_Lrectangle"),"");
		k=k+1;
		doTextboxPair(k,pan,u,"9.81","<html>g [m.s<sup>-2</sup>]</html>",dico.entry("Explanation_g"),"0.01");
		k=k+1;
		doTextboxPair(k,pan,u,"1.5","c [-]",dico.entry("Explanation_crectangle"),"0.05");
		k=k+1;
		doTextboxPair(k,pan,u,"","k [m]",dico.entry("Explanation_k"),"");

		return u;
	}

	private GridBag_TextField_Titled[][]  fill_TriangularSill(GridBag_Label lab,JPanel pan){

		GridBag_TextField_Titled[][] u= new GridBag_TextField_Titled[5][2];

		lab.setText("<html>"
				+ "<hr>"
				+ "a=C<sub>t</sub> .tan(v/2).\u221A(2g)"
				+ "<br>"
				+ "C<sub>t</sub> : " + dico.entry("Explanation_ftriangle")
				+ "<br>"
				+ "v: " + dico.entry("Explanation_u")
				+ "<br>"
				+ "g: " + dico.entry("Explanation_g")
				+ "<hr>"
				+ "c: " + dico.entry("Explanation_ctriangle")
				+ "<hr>"
				+ "k: " + dico.entry("Explanation_k")
				+ "<hr>"
				+ "b: " + dico.entry("Explanation_b")
				+ "<hr>"
				+ "</html>");

		GridBag_Layout.SetGrid(pan,new int[]{0,0,0,0,0},new int[]{0,0},
				new double[]{0.0,0.0,0.0,0.0,0.0},new double[]{1.0,1.0});

		// textboxes
		int k=0;		
		doTextboxPair(k,pan,u,"0.31","<html>C<sub>t</sub>  [-]</html>",dico.entry("Explanation_ftriangle"),"0.05");
		k=k+1;
		doTextboxPair(k,pan,u,"","<html>v [<sup>o</sup>]</html>",dico.entry("Explanation_u"),"");
		k=k+1;
		doTextboxPair(k,pan,u,"9.81","<html>g [m.s<sup>-2</sup>]</html>",dico.entry("Explanation_g"),"0.01");
		k=k+1;
		doTextboxPair(k,pan,u,"2.5","c [-]",dico.entry("Explanation_ctriangle"),"0.05");
		k=k+1;
		doTextboxPair(k,pan,u,"","k [m]",dico.entry("Explanation_k"),"");

		return u;
	}

	private GridBag_TextField_Titled[][]  fill_Orifice(GridBag_Label lab,JPanel pan){

		GridBag_TextField_Titled[][] u= new GridBag_TextField_Titled[5][2];

		lab.setText("<html>"
				+ "<hr>"
				+ "a=C<sub>o</sub>.A<sub>w</sub>.\u221A(2g)"
				+ "<br>"
				+ "C<sub>o</sub>: " + dico.entry("Explanation_forifice")
				+ "<br>"
				+ "A<sub>w</sub>: " + dico.entry("Explanation_S")
				+ "<br>"
				+ "g: " + dico.entry("Explanation_g")
				+ "<hr>"
				+ "c: " + dico.entry("Explanation_corifice")
				+ "<hr>"
				+ "k: " + dico.entry("Explanation_k")
				+ "<hr>"
				+ "b: " + dico.entry("Explanation_b")
				+ "<hr>"
				+ "</html>");

		GridBag_Layout.SetGrid(pan,new int[]{0,0,0,0,0},new int[]{0,0},
				new double[]{0.0,0.0,0.0,0.0,0.0},new double[]{1.0,1.0});

		// textboxes
		int k=0;		
		doTextboxPair(k,pan,u,"0.6","<html>C<sub>o</sub> [-]</html>",dico.entry("Explanation_forifice"),"0.1");
		k=k+1;
		doTextboxPair(k,pan,u,"","<html>A<sub>w</sub> [m<sup>2</sup>]</html>",dico.entry("Explanation_S"),"");
		k=k+1;
		doTextboxPair(k,pan,u,"9.81","<html>g [m.s<sup>-2</sup>]</html>",dico.entry("Explanation_g"),"0.01");
		k=k+1;
		doTextboxPair(k,pan,u,"0.5","c [-]",dico.entry("Explanation_corifice"),"0.05");
		k=k+1;
		doTextboxPair(k,pan,u,"","k [m]",dico.entry("Explanation_k"),"");

		return u;

	}

	private GridBag_TextField_Titled[][] fill_ParabolicSill(GridBag_Label lab,JPanel pan){
		GridBag_TextField_Titled[][] u= new GridBag_TextField_Titled[6][2];

		lab.setText("<html>"
				+ "<hr>"
				+ "a=C<sub>p</sub>.B<sub>p</sub>.H<sub>p</sub><sup>-1/2</sup>.\u221A(2g)"
				+ "<br>"
				+ "C<sub>p</sub>: " + dico.entry("Explanation_fparabola")
				+ "<br>"
				+ "B<sub>p</sub>: " + dico.entry("Explanation_Bparabola")
				+ "<br>"
				+ "H<sub>p</sub>: " + dico.entry("Explanation_Hparabola")
				+ "<br>"
				+ "g: " + dico.entry("Explanation_g")
				+ "<hr>"
				+ "c: " + dico.entry("Explanation_cparabola")
				+ "<hr>"
				+ "k: " + dico.entry("Explanation_k")
				+ "<hr>"
				+ "b: " + dico.entry("Explanation_b")
				+ "<hr>"
				+ "</html>");

		GridBag_Layout.SetGrid(pan,new int[]{0,0,0,0,0,0},new int[]{0,0},
				new double[]{0.0,0.0,0.0,0.0,0.0,0.0},new double[]{1.0,1.0});
		// textboxes
		int k=0;		
		doTextboxPair(k,pan,u,"0.22","<html>C<sub>p</sub> [-]</html>",dico.entry("Explanation_fparabola"),"0.04");
		k=k+1;
		doTextboxPair(k,pan,u,"","<html>B<sub>p</sub> [m]</html>",dico.entry("Explanation_Bparabola"),"");
		k=k+1;
		doTextboxPair(k,pan,u,"","<html>H<sub>p</sub> [m]</html>",dico.entry("Explanation_Hparabola"),"");
		k=k+1;
		doTextboxPair(k,pan,u,"9.81","<html>g [m.s<sup>-2</sup>]</html>",dico.entry("Explanation_g"),"0.01");
		k=k+1;
		doTextboxPair(k,pan,u,"2.0","c [-]",dico.entry("Explanation_cparabola"),"0.05");
		k=k+1;
		doTextboxPair(k,pan,u,"","k [m]",dico.entry("Explanation_k"),"");
		return u;
	}


	private void doTextboxPair(int k,JPanel pan,GridBag_TextField_Titled[][] u,
			String val,String lbl,String tip,String pomval){
		u[k][0]=new GridBag_TextField_Titled(pan,val,lbl,
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,tip);
		u[k][1]=new GridBag_TextField_Titled(pan,pomval,dico.entry("+/-"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("+/-_long"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_cancel)) {this.dispose();}
		else if(e.getSource().equals(butt_apply)) {applyAction();}
		else if(e.getSource().equals(butt_propa)) {propaAction();}		
		else if(e.getSource().equals(butt_J)) {buttJAction();}		
		else if(e.getSource().equals(butt_M)) {buttMAction();}		
		else if(e.getSource().equals(butt_cancel_J)) {dialog_J.dispose();}
		else if(e.getSource().equals(butt_apply_J)) {propaAction_J();}
		else if(e.getSource().equals(butt_cancel_M)) {dialog_M.dispose();}
		else if(e.getSource().equals(butt_apply_M)) {propaAction_M();}
	}

	private void applyAction(){
		this.control.getK().setText(this.k.getText());
		this.control.getKpom().setText(this.kpom.getText());
		this.control.getA().setText(this.a.getText());
		this.control.getApom().setText(this.apom.getText());
		this.control.getC().setText(this.c.getText());
		this.control.getCpom().setText(this.cpom.getText());
		controller.updateHydrauConfig_specifix(this);		
		this.dispose();
	}

	private void propaAction(){
		// type-specific: a
		Combo_ControlType[] list=Combo_ControlType.getList();
		boolean ok=true;
		if(list[type].getTxt().equals("RectangularSill")){ok=propaAction_Rectangle();}
		if(list[type].getTxt().equals("TriangularSill")){ok=propaAction_Triangle();}
		if(list[type].getTxt().equals("Orifice")){ok=propaAction_Orifice();}
		if(list[type].getTxt().equals("ParabolicSill")){ok=propaAction_Parabola();}
		if(list[type].getTxt().equals("RectangularChannel")){ok=propaAction_Channel_Rectangle();}
		if(list[type].getTxt().equals("TriangularChannel")){ok=propaAction_Channel_Triangle();}
		if(list[type].getTxt().equals("ParabolicChannel")){ok=propaAction_Channel_Parabola();}
		if(!ok) {return;}
		// common to all types: k and c
		int n=this.uncertainties.length;
		this.k.setText(uncertainties[n-1][0].getText());
		this.kpom.setText(uncertainties[n-1][1].getText());
		this.c.setText(uncertainties[n-2][0].getText());
		this.cpom.setText(uncertainties[n-2][1].getText());
	}

	private double[][] propaStringToDouble(){
		int n=uncertainties.length;
		double[][] z= propaStringToDouble(n);
		return z;		
	}

	private double[][] propaStringToDouble(int n){
		double[][] z= new double[n][2];
		for(int i=0;i<n;i++){
			try{z[i][0]=Double.parseDouble(uncertainties[i][0].getText());}
			catch(Exception e){new ExceptionPanel(this,dico.entry("FormatErrorMessage"));return null;}
			try{z[i][1]=Double.parseDouble(uncertainties[i][1].getText());}
			catch(Exception e){new ExceptionPanel(this,dico.entry("FormatErrorMessage"));return null;}
		}
		return z;		
	}

	private double[][] propaStringToDouble(GridBag_TextField_Titled[][] pairs){
		int n=pairs.length;
		double[][] z= new double[n][2];
		for(int i=0;i<n;i++){
			try{z[i][0]=Double.parseDouble(pairs[i][0].getText());}
			catch(Exception e){new ExceptionPanel(this,dico.entry("FormatErrorMessage"));return null;}
			try{z[i][1]=Double.parseDouble(pairs[i][1].getText());}
			catch(Exception e){new ExceptionPanel(this,dico.entry("FormatErrorMessage"));return null;}
		}
		return z;		
	}

	private boolean propaAction_Rectangle(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double L=z[1][0],Lpom=z[1][1];
		double g=z[2][0],gpom=z[2][1];
		double val,var;
		val=f*L*Math.sqrt(2*g);
		var=Math.pow(0.5*fpom,2)*Math.pow(L*Math.sqrt(2*g),2)+
				Math.pow(0.5*Lpom,2)*Math.pow(f*Math.sqrt(2*g),2)+
				Math.pow(0.5*gpom,2)*Math.pow(f*L*Math.pow(2*g,-0.5),2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_J(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble(u_J);
		if(z==null){return false;}
		// start computation
		double A=z[0][0],Apom=z[0][1];
		double B=z[1][0],Bpom=z[1][1];
		double D=z[2][0],Dpom=z[2][1];
		double val,var;
		val=Math.abs(A-B)/D;
		var=Math.pow(0.5*Apom,2)*Math.pow(1/D,2)+
				Math.pow(0.5*Bpom,2)*Math.pow(1/D,2)+
				Math.pow(0.5*Dpom,2)*Math.pow(Math.abs(A-B)*(1/Math.pow(D,2)),2);
		// fill textboxes
		this.uncertainties[2][0].setText(Double.toString(val));
		this.uncertainties[2][1].setText(Double.toString(2*Math.sqrt(var)));
		dialog_J.dispose();
		return true;
	}

	private boolean propaAction_M(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble(u_M);
		if(z==null){return false;}
		// start computation
		double n=z[0][0],npom=z[0][1];
		double val,var;
		val=1.0/n;
		var=Math.pow(0.5*npom,2)*Math.pow(1.0/n,4);
		// fill textboxes
		this.uncertainties[0][0].setText(Double.toString(val));
		this.uncertainties[0][1].setText(Double.toString(2*Math.sqrt(var)));
		dialog_M.dispose();
		return true;
	}

	private boolean propaAction_Triangle(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double u=z[1][0],upom=z[1][1];
		double g=z[2][0],gpom=z[2][1];
		double val,var;
		double k=(0.5*Math.PI)/180.0;
		val=f*Math.tan(u*k)*Math.sqrt(2.0*g);
		var=Math.pow(0.5*fpom,2)*Math.pow(Math.tan(u*k)*Math.sqrt(2.0*g),2)+
				Math.pow(0.5*upom,2)*Math.pow((f*Math.sqrt(2.0*g)*k)/Math.pow(Math.cos(u*k),2),2)+
				Math.pow(0.5*gpom,2)*Math.pow(f*Math.tan(u*k)*Math.pow(2.0*g,-0.5),2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_Orifice(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double S=z[1][0],Spom=z[1][1];
		double g=z[2][0],gpom=z[2][1];
		double val,var;
		val=f*S*Math.sqrt(2.0*g);
		var=Math.pow(0.5*fpom,2)*Math.pow(S*Math.sqrt(2.0*g),2)+
				Math.pow(0.5*Spom,2)*Math.pow(f*Math.sqrt(2.0*g),2)+
				Math.pow(0.5*gpom,2)*Math.pow(f*S*Math.pow(2.0*g,-0.5),2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_Parabola(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double B=z[1][0],Bpom=z[1][1];
		double H=z[2][0],Hpom=z[2][1];
		double g=z[3][0],gpom=z[3][1];
		double val,var;
		val=f*B*Math.sqrt(2.0*g)/Math.sqrt(H);
		var=Math.pow(0.5*fpom,2)*Math.pow(B*Math.sqrt(2.0*g)/Math.sqrt(H),2)+
				Math.pow(0.5*Bpom,2)*Math.pow(f*Math.sqrt(2.0*g)/Math.sqrt(H),2)+
				Math.pow(0.5*Hpom,2)*Math.pow(0.5*f*B*Math.sqrt(2.0*g)*Math.pow(H,-1.5),2)+				
				Math.pow(0.5*gpom,2)*Math.pow(f*B*Math.pow(2.0*g*H,-0.5),2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_Channel_Rectangle(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double L=z[1][0],Lpom=z[1][1];
		double J=z[2][0],Jpom=z[2][1];
		double val,var,rac,racinv;
		rac=Math.sqrt(J);
		racinv=1.0/rac;
		val=f*L*rac;
		var=Math.pow(0.5*fpom,2)*Math.pow(L*rac,2)+
				Math.pow(0.5*Lpom,2)*Math.pow(f*rac,2)+
				Math.pow(0.5*Jpom,2)*Math.pow(f*L*0.5*racinv,2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_Channel_Triangle(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double u=z[1][0],upom=z[1][1];
		double J=z[2][0],Jpom=z[2][1];
		double k=(0.5*Math.PI)/180.0;
		double trigo=Math.tan(u*k)*Math.pow(0.5*Math.sin(u*k),2.0/3.0);
		double dtrigo=k*Math.pow(Math.sin(u*k),2.0/3.0)*( (2.0/3.0) + (1.0/Math.pow(Math.cos(u*k),2)) );
		double val,var,rac,racinv;
		rac=Math.sqrt(J);
		racinv=1/rac;
		val=f*trigo*rac;
		var=Math.pow(0.5*fpom,2)*Math.pow(trigo*rac,2)+
				Math.pow(0.5*upom,2)*Math.pow(f*rac*dtrigo,2)+
				Math.pow(0.5*Jpom,2)*Math.pow(f*trigo*0.5*racinv,2);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean propaAction_Channel_Parabola(){
		// check user entry is castable to double, and if so cast it
		double[][] z=propaStringToDouble();
		if(z==null){return false;}
		// start computation
		double f=z[0][0],fpom=z[0][1];
		double B=z[1][0],Bpom=z[1][1];
		double H=z[2][0],Hpom=z[2][1];
		double J=z[3][0],Jpom=z[3][1];
		double val,var,rac,racinv,BoH,k;
		rac=Math.sqrt(J);
		racinv=1.0/rac;
		BoH=B/Math.sqrt(H);
		k=Math.pow(2.0d/3.0d,5.0d/3.0d);
		val=k*f*BoH*rac;
		var=Math.pow(k,2)*(
				Math.pow(0.5*fpom,2)*Math.pow(BoH*rac,2)+
				Math.pow(0.5*Bpom,2)*Math.pow(f*rac*Math.pow(H,-0.5),2)+
				Math.pow(0.5*Hpom,2)*Math.pow(0.5*f*rac*B*Math.pow(H,-1.5),2)+
				Math.pow(0.5*Jpom,2)*Math.pow(BoH*f*0.5*racinv,2)
				);
		// fill textboxes
		this.a.setText(Double.toString(val));
		this.apom.setText(Double.toString(2*Math.sqrt(var)));		
		return true;
	}

	private boolean buttJAction(){
		// create dialog and decorate it
		dialog_J=new JDialog(this,true);
		dialog_J.setTitle(dico.entry("PriorAssistant"));
		dialog_J.setSize(Defaults.JAssistantSize);
		dialog_J.setLocationRelativeTo(null);
		dialog_J.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		ImageIcon icon=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconPriorAssistant));
		dialog_J.setIconImage(icon.getImage());
		JPanel pan=new JPanel();
		pan.setBackground(Defaults.bkgColor);
		dialog_J.setContentPane(pan);

		GridBag_Layout.SetGrid(pan,new int[] {0,0,0,0},new int[] {0,0},
				new double[] {0.0,0.0,0.0,0.0},new double[] {1.0,1.0});
		// textboxes
		u_J= new GridBag_TextField_Titled[3][2];
		int k=0;	
		doTextboxPair(k,pan,u_J,"","<html>z<sub>X</sub> [m]</html>",dico.entry("Explanation_altA"),"");
		k=k+1;		
		doTextboxPair(k,pan,u_J,"","<html>z<sub>Y</sub> [m]</html>",dico.entry("Explanation_altB"),"");
		k=k+1;		
		doTextboxPair(k,pan,u_J,"","dist(X,Y) [m]",dico.entry("Explanation_distAB"),"");
		// Apply/Cancel buttons
		butt_apply_J=new GridBag_Button(dialog_J.getContentPane(),this,"butt_apply",
				dico.entry("Apply"),Defaults.iconApply,
				0,3,1,1,false,false,dico.entry("Apply"));
		butt_cancel_J=new GridBag_Button(dialog_J.getContentPane(),this,"butt_cancel",
				dico.entry("Cancel"),Defaults.iconCancel,
				1,3,1,1,false,false,dico.entry("Cancel"));
		dialog_J.setVisible(true);
		return true;
	}

	private boolean buttMAction(){
		// create dialog and decorate it
		dialog_M=new JDialog(this,true);
		dialog_M.setTitle(dico.entry("PriorAssistant"));
		dialog_M.setSize(Defaults.MAssistantSize);
		dialog_M.setLocationRelativeTo(null);
		dialog_M.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		ImageIcon icon=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconPriorAssistant));
		dialog_M.setIconImage(icon.getImage());
		JPanel pan=new JPanel();
		pan.setBackground(Defaults.bkgColor);
		dialog_M.setContentPane(pan);

		GridBag_Layout.SetGrid(pan,new int[] {0,0},new int[] {0,0},
				new double[] {0.0,0.0},new double[] {1.0,1.0});
		// textboxes
		u_M= new GridBag_TextField_Titled[1][2];
		int k=0;
		if(config.isUseManning()){
			doTextboxPair(k,pan,u_M,"","<html>K<sub>S</sub> [m<sup>1/3</sup>.s<sup>-1</sup>]</html>",dico.entry("Explanation_K"),"");
		}
		else{
			doTextboxPair(k,pan,u_M,"","<html>n [s.m<sup>-1/3</sup>]</html>",dico.entry("Explanation_Manning"),"");
		}
		// Apply/Cancel buttons
		butt_apply_M=new GridBag_Button(dialog_M.getContentPane(),this,"butt_apply",
				dico.entry("Apply"),Defaults.iconApply,
				0,1,1,1,false,false,dico.entry("Apply"));
		butt_cancel_M=new GridBag_Button(dialog_M.getContentPane(),this,"butt_cancel",
				dico.entry("Cancel"),Defaults.iconCancel,
				1,1,1,1,false,false,dico.entry("Cancel"));
		dialog_M.setVisible(true);
		return true;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public GridBag_Text_Titled getA() {
		return a;
	}

	public void setA(GridBag_Text_Titled a) {
		this.a = a;
	}

	public GridBag_Text_Titled getApom() {
		return apom;
	}

	public void setApom(GridBag_Text_Titled apom) {
		this.apom = apom;
	}

	public GridBag_Text_Titled getK() {
		return k;
	}

	public void setK(GridBag_Text_Titled k) {
		this.k = k;
	}

	public GridBag_Text_Titled getKpom() {
		return kpom;
	}

	public void setKpom(GridBag_Text_Titled kpom) {
		this.kpom = kpom;
	}

	public GridBag_Text_Titled getC() {
		return c;
	}

	public void setC(GridBag_Text_Titled c) {
		this.c = c;
	}

	public GridBag_Text_Titled getCpom() {
		return cpom;
	}

	public void setCpom(GridBag_Text_Titled cpom) {
		this.cpom = cpom;
	}

	public GridBag_TextField_Titled[][] getUncertainties() {
		return uncertainties;
	}

	public void setUncertainties(GridBag_TextField_Titled[][] u) {
		this.uncertainties = u;
	}

	public ControlPanel getControl() {
		return control;
	}

	public void setControl(ControlPanel cp) {
		this.control = cp;
	}

	public ConfigHydrauPanel getHydrau() {
		return hydrau;
	}

	public void setHydrau(ConfigHydrauPanel hydrau) {
		this.hydrau = hydrau;
	}

}
