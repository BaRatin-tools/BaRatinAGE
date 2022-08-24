package moteur;

import commons.Distribution;
import commons.Parameter;

/**
 * Remnant error model object
 * @author Sylvain Vigneau & Ben Renard, Irstea Lyon
 */
public class RemnantError extends Item {

	private String function;
	private int npar;
	private Parameter[] par;


	//TODO: faire les hashcode et equals quand classe plus avanc�e

	/**
	 * Full Constructor
	 * @param name name of the error model
	 * @param description description
	 * @param function function linking sdev and prediction
	 * @param npar number of parameters of function above
	 * @param par parameters
	 */
	public RemnantError(String name,String description,String function,int npar,Parameter[] par) {
		super(name,description);
		this.function=function;
		this.npar=npar;
		this.par=par;
	}

	/**
	 * constructor by copy
	 * @param x copied object
	 */
	public RemnantError(RemnantError x) {
		super(x);
		if(x==null){return;}
		if(x.getFunction()!=null) this.function=new String(x.getFunction());
		this.npar= Integer.valueOf(x.getNpar());
		this.par=new Parameter[this.npar];
		if(x.getPar()!=null) {
			for(int i=0;i<this.npar;i++){
				if(x.getPar()[i]!=null) this.par[i]=new Parameter(x.getPar()[i]);
			}
		}
	}

	/**
	 * Default constructor
	 */
	public RemnantError() {
		this(Remnant_Linear);
		/*
		super("Default","Default model: sdev=g1+g2*predicted");
		this.function="Linear";
		this.npar=2;
		this.par=new Parameter[] {
				new Parameter("intercept",1.0,Distribution.D_Uniform),
				new Parameter("slope",0.1,Distribution.D_Uniform)};
		this.par[0].getPrior().setParval(new Double[] {0.0,100000.});
		this.par[1].getPrior().setParval(new Double[] {0.0,1000.});
		 */
	}

	/**
	 * Partial Constructor
	 * @param name
	 */	
	public RemnantError(String name) {
		super(name);
	}

	// Definition of basic remnant error models
	final private static Distribution gamma1_prior=new Distribution("Uniform",2,new String[] {"lower_bound","higher_bound"},new Double[] {0.d,10000.d});
	final private static Distribution gamma2_prior=new Distribution("Uniform",2,new String[] {"lower_bound","higher_bound"},new Double[] {0.d,1.d});
	final private static Distribution gamma_prior=new Distribution("Uniform",2,new String[] {"lower_bound","higher_bound"},new Double[] {0.d,10000.d});
	final private static Parameter gamma1=new Parameter("intercept",1.0,gamma1_prior);
	final private static Parameter gamma2=new Parameter("slope",0.1,gamma2_prior);
	final private static Parameter gamma=new Parameter("constant",1.0,gamma_prior);
	final public static RemnantError Remnant_Linear=new RemnantError("Remnant_Linear","Default model: sdev=g1+g2*predicted","Linear",2,new Parameter[]{gamma1,gamma2});
	final public static RemnantError Remnant_Proportional=new RemnantError("Remnant_Proportional","Purely proportional model: sdev=g1*predicted","Proportional",1,new Parameter[]{gamma2});
	final public static RemnantError Remnant_Constant=new RemnantError("Remnant_Constant","simple model: sdev=g1","Constant",1,new Parameter[]{gamma});

	@Override
	public String toString(){
		String out="";
		out=out+function+System.getProperty("line.separator");
		out=out+Integer.toString(npar)+System.getProperty("line.separator");
		for(int i=0;i<npar;i++){
			if(par[i]!=null){out=out+par[i].toString();}
			if(i+1<npar){out=out+System.getProperty("line.separator");}
		}
		return out;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public int getNpar() {
		return npar;
	}

	public void setNpar(int npar) {
		this.npar = npar;
	}

	public Parameter[] getPar() {
		return par;
	}

	public void setPar(Parameter[] par) {
		this.par = par;
	}

}
