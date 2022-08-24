package commons;

import java.util.Arrays;

/**
 * Distribution object
 * @author Sylvain Vigneau & Ben Renard, Irstea Lyon
 */
public class Distribution {
	private String name;
	private int npar;
	private String[] parname;
	private Double[] parval;

	/**
	 * Empty constructor - initialization to default values (standard Gaussian)
	 */
	public Distribution(){
		this.name="Gaussian";
		this.npar=2;
		this.parname=new String[] {"mean","standard_deviation"};
		this.parval=new Double[] {0.d,1.d};
	}

	/**
	 * Full Constructor
	 * @param name Name of the distribution
	 * @param npar Number of parameters
	 * @param parname parameters names
	 * @param parval parameters values
	 */
	public Distribution(String name,int npar,String[] parname, Double[] parval){
		this.name=name;
		this.npar=npar;
		this.parname=parname;
		this.parval=parval;		
	}

	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public Distribution(Distribution x) {
		if(x==null){return;}
		if(x.getName()!=null){
			this.name=new String(x.getName());
		}
		this.npar=Integer.valueOf(x.getNpar());
		this.parname=new String[this.npar];
		this.parval=new Double[this.npar];
		if(x.getParname()!=null){
			for(int i=0;i<this.npar;i++){
				if(x.getParname()[i]!=null) this.parname[i]=new String(x.getParname()[i]);
			}
		}
		if(x.getParval()!=null){		
			for(int i=0;i<this.npar;i++){
				if(x.getParval()[i]!=null) this.parval[i]=Double.valueOf(x.getParval()[i]);
			}
		}
	}

	/**
	 * String representation of the distribution
	 */
	@Override
	public String toString(){
		String out="";
		if(this.name!=null){out=out+this.name;}
		out=out+System.getProperty("line.separator");
		for(int i=0;i<this.parval.length;i++){
			if(this.parval[i]!=null) {out=out+this.parval[i].toString();}
			if(i+1<this.parval.length){out=out+",";}
		}
		return out;}


	// Standard distributions
	final public static Distribution D_Gaussian=new Distribution("Gaussian",2,new String[] {"mean","standard_deviation"},new Double[] {0.d,1.d});
	final public static Distribution D_LogNormal=new Distribution("LogNormal",2,new String[] {"mean_log","standard_deviation_log"},new Double[] {0.d,1.d});
	final public static Distribution D_Uniform=new Distribution("Uniform",2,new String[] {"lower_bound","higher_bound"},new Double[] {0.d,1.d});
	final public static Distribution D_Gumbel=new Distribution("Gumbel",2,new String[] {"location","scale"},new Double[] {0.d,1.d});
	final public static Distribution D_GEV=new Distribution("GEV",3,new String[] {"location","scale","shape"},new Double[] {0.d,1.d,-0.13d});
	final public static Distribution D_GPD=new Distribution("GPD",3,new String[] {"threshold","scale","shape"},new Double[] {0.d,1.d,-0.13d});
	final public static Distribution D_PearsonIII=new Distribution("PearsonIII",3,new String[] {"location","scale","shape"},new Double[] {1.d,1.d,4.0d});
	final public static Distribution D_Triangle=new Distribution("Triangle",3,new String[] {"peak","lower_bound","higher_bound"},new Double[] {0.d,-1.d,1.d});
	final public static Distribution D_FlatPrior=new Distribution("FlatPrior",0,new String[] {},new Double[] {});
	final public static Distribution D_FlatPriorPlus=new Distribution("FlatPrior+",0,new String[] {},new Double[] {});
	final public static Distribution D_FlatPrioMinus=new Distribution("FlatPrior-",0,new String[] {},new Double[] {});
	final public static Distribution D_Poisson=new Distribution("Poisson",1,new String[] {"lambda"},new Double[] {1.d});

	final public static Distribution[] DList={D_Gaussian,D_LogNormal,D_Uniform,D_Gumbel,D_GEV,D_GPD,D_PearsonIII,D_Triangle,D_Poisson};
	final public static Distribution[] PriorList={D_FlatPrior,D_FlatPriorPlus,D_FlatPrioMinus,D_Gaussian,D_Uniform,D_Triangle,D_LogNormal,D_Gumbel,D_GEV,D_GPD,D_PearsonIII,D_Poisson};

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNpar() {
		return npar;
	}
	public void setNpar(int npar) {
		this.npar = npar;
	}
	public String[] getParname() {
		return parname;
	}
	public void setParname(String[] parname) {
		this.parname = parname;
	}
	public Double[] getParval() {
		return parval;
	}
	public void setParval(Double[] parval) {
		this.parval = parval;
	}

	/////////////////////////////////////////////////////////
	// CREATEURS SPECIFIQUES PAR DISTRIBUTION
	/////////////////////////////////////////////////////////

	public static  Distribution createGaussian(Double mean, Double standardDeviation){
		return new Distribution("Gaussian",2,new String[] {"mean","standard_deviation"},new Double[] {mean, standardDeviation});
	}

	public static Distribution createLogNormal(Double meanLog, Double standardDeviationLog){
		return new Distribution("LogNormal",2,new String[] {"mean_log","standard_deviation_log"},new Double[] {meanLog, standardDeviationLog});
	}

	public static Distribution createUniform(Double lowerBound, Double higherBound){
		return new Distribution("Uniform",2,new String[] {"lower_bound","higher_bound"},new Double[] {lowerBound, higherBound});
	}

	public static Distribution createGumble(Double location, Double scale){
		return new Distribution("Gumbel",2,new String[] {"location","scale"},new Double[] {location, scale});
	}

	public static Distribution createGEV(Double location, Double scale, Double shape){
		return new Distribution("GEV",3,new String[] {"location","scale","shape"},new Double[] {location, scale, shape});
	}

	public static Distribution createGPD(Double threshold, Double scale, Double shape){
		return new Distribution("GPD",3,new String[] {"threshold","scale","shape"},new Double[] {threshold, scale, shape});
	}

	public static Distribution createPearsonIII(Double location, Double scale, Double shape){
		return new Distribution("PearsonIII",3,new String[] {"location","scale","shape"},new Double[] {location, scale, shape});
	}

	public static Distribution createTriangle(Double peak, Double lowerBound, Double higherBound){
		return new Distribution("Triangle",3,new String[] {"peak","lower_bound","higher_bound"},new Double[] {peak, lowerBound, higherBound});
	}

	public static Distribution createFlatPrior(){
		return new Distribution("FlatPrior",0,new String[] {},new Double[] {});
	}

	public static Distribution createFlatPriorPlus(){
		return new Distribution("FlatPrior+",0,new String[] {},new Double[] {});
	}

	public static Distribution createFlatPriorMinus(){
		return new Distribution("FlatPrior-",0,new String[] {},new Double[] {});
	}

	public static Distribution createPoisson(Double lambda){
		return new Distribution("Poisson",1,new String[] {"lambda"},new Double[] {lambda});
	}

	/////////////////////////////////////////////////////////
	// EQUALS & HASHCODE
	/////////////////////////////////////////////////////////

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + npar;
		result = prime * result + Arrays.hashCode(parname);
		result = prime * result + Arrays.hashCode(parval);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Distribution)) {
			return false;
		}
		Distribution other = (Distribution) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (npar != other.npar) {
			return false;
		}
		if (!Arrays.equals(parname, other.parname)) {
			return false;
		}
		if (!Arrays.equals(parval, other.parval)) {
			return false;
		}
		return true;
	}


}