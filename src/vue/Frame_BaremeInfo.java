package vue;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.Constants;
import commons.GridBag_Button;
import commons.GridBag_Label;
import commons.GridBag_Layout;
import commons.GridBag_TextField_Titled;
import commons.Time;
import controleur.Control;

@SuppressWarnings("serial")
public class Frame_BaremeInfo extends JDialog implements ActionListener{

	private String code;
	private String name;
	private Time start;
	private Time end;
	private GridBag_TextField_Titled tcode;
	private GridBag_TextField_Titled tname;
	private GridBag_TextField_Titled tstart_Y;
	private GridBag_TextField_Titled tstart_M;
	private GridBag_TextField_Titled tstart_D;
	private GridBag_TextField_Titled tstart_h;
	private GridBag_TextField_Titled tstart_m;
	private GridBag_TextField_Titled tend_Y;
	private GridBag_TextField_Titled tend_M;
	private GridBag_TextField_Titled tend_D;
	private GridBag_TextField_Titled tend_h;
	private GridBag_TextField_Titled tend_m;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_cancel;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public Frame_BaremeInfo(Frame parent){
		super(parent,true);
		this.setTitle(dico.entry("BaremeInfo"));
		this.setMinimumSize(Defaults.baremeInfoSize);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(parent);
		JPanel pan =new JPanel();
		GridBag_Layout.SetGrid(pan, new int[] {0,0,0,0,0,0,0}, new int[] {0,0,0,0,0}, new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0},new double[] {1.0,1.0,1.0,1.0,1.0});
		pan.setBackground(Defaults.bkgColor);

		int k=0;
		tcode=new GridBag_TextField_Titled(pan,"",dico.entry("HydroCode"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,5,1,dico.entry("Explanation_HydroCode"));
		
		k=k+1;
		tname=new GridBag_TextField_Titled(pan,dico.entry("RC"),dico.entry("Name"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,5,1,dico.entry("Explanation_Name"));

		k=k+1;
		new GridBag_Label(pan,dico.entry("Validity_start"),
				config.getFontLbl(),Defaults.txtColor,SwingConstants.LEFT,
				0,k,5,1,true,true);
		
		k=k+1;
		tstart_Y=new GridBag_TextField_Titled(pan,"",dico.entry("YYYY"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("YYYY"));
		tstart_M=new GridBag_TextField_Titled(pan,"",dico.entry("MM"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("MM"));
		tstart_D=new GridBag_TextField_Titled(pan,"",dico.entry("DD"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				2,k,1,1,dico.entry("DD"));
		tstart_h=new GridBag_TextField_Titled(pan,"",dico.entry("hh"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				3,k,1,1,dico.entry("hh"));
		tstart_m=new GridBag_TextField_Titled(pan,"",dico.entry("mm"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				4,k,1,1,dico.entry("mm"));
		tstart_h.setText("00");tstart_m.setText("00");

		k=k+1;
		new GridBag_Label(pan,dico.entry("Validity_end"),
				config.getFontLbl(),Defaults.txtColor,SwingConstants.LEFT,
				0,k,5,1,true,true);

		k=k+1;
		tend_Y=new GridBag_TextField_Titled(pan,"",dico.entry("YYYY"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("YYYY"));
		tend_M=new GridBag_TextField_Titled(pan,"",dico.entry("MM"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("MM"));
		tend_D=new GridBag_TextField_Titled(pan,"",dico.entry("DD"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				2,k,1,1,dico.entry("DD"));
		tend_h=new GridBag_TextField_Titled(pan,"",dico.entry("hh"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				3,k,1,1,dico.entry("hh"));
		tend_m=new GridBag_TextField_Titled(pan,"",dico.entry("mm"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				4,k,1,1,dico.entry("mm"));
		tend_h.setText("00");tend_m.setText("00");

		k=k+1;
		butt_apply=new GridBag_Button(pan,this,"foo",dico.entry("Apply"),Defaults.iconApply,0,k,2,1,false,false,"");
		butt_cancel=new GridBag_Button(pan,this,"foo",dico.entry("Cancel"),Defaults.iconCancel,3,k,2,1,false,false,"");
		this.setContentPane(pan);
		this.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(butt_cancel)){
			this.dispose();
		}
		if (ae.getSource().equals(butt_apply)){
			String txt;
			int Y,M,D,h,m;
			// name and code
			txt=this.tcode.getText();
			if(txt.length()>8){txt=txt.substring(0,8);}
			this.code=txt;
			txt=this.tname.getText();
			if(txt.length()>6){txt=txt.substring(0,6);}
			this.name=txt;
			// validity start
			Y=controller.safeParse_i(this.tstart_Y.getText());
			if(Y==Constants.I_MISSING | Y==Constants.I_UNFEAS) return;
			M=controller.safeParse_i(this.tstart_M.getText());
			if(M==Constants.I_MISSING | M==Constants.I_UNFEAS) return;
			D=controller.safeParse_i(this.tstart_D.getText());
			if(D==Constants.I_MISSING | D==Constants.I_UNFEAS) return;
			h=controller.safeParse_i(this.tstart_h.getText());
			if(h==Constants.I_MISSING | h==Constants.I_UNFEAS) return;
			m=controller.safeParse_i(this.tstart_m.getText());
			if(m==Constants.I_MISSING | m==Constants.I_UNFEAS) return;
			this.start=new Time(Y,M,D,h,m,0);
			// validity end
			Y=controller.safeParse_i(this.tend_Y.getText());
			if(Y==Constants.I_MISSING | Y==Constants.I_UNFEAS) return;
			M=controller.safeParse_i(this.tend_M.getText());
			if(M==Constants.I_MISSING | M==Constants.I_UNFEAS) return;
			D=controller.safeParse_i(this.tend_D.getText());
			if(D==Constants.I_MISSING | D==Constants.I_UNFEAS) return;
			h=controller.safeParse_i(this.tend_h.getText());
			if(h==Constants.I_MISSING | h==Constants.I_UNFEAS) return;
			m=controller.safeParse_i(this.tend_m.getText());
			if(m==Constants.I_MISSING | m==Constants.I_UNFEAS) return;
			this.end=new Time(Y,M,D,h,m,0);
			this.dispose();
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Time getStart() {
		return start;
	}

	public void setStart(Time start) {
		this.start = start;
	}

	public Time getEnd() {
		return end;
	}

	public void setEnd(Time end) {
		this.end = end;
	}

}
