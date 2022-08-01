package moteur;

import commons.Constants;

/**
 * Options applicable to prior rating curves
 * @author Ben Renard, Irstea Lyon
 */
public class PriorRatingCurveOptions extends RatingCurveOptions{

	private int nSim;
	// defaults
	private static final int nSim_def=1000;
	
	/**
	 * default constructor
	 */
	public PriorRatingCurveOptions(){
		super();
		this.nSim=nSim_def;
	}
	
	/**
	 * full constructor
	 * @param nsim
	 * @param hmin
	 * @param hmax
	 * @param hstep
	 * @param nstep
	 */
	public PriorRatingCurveOptions(int nsim,Double hmin,Double hmax,Double hstep,int nstep) {
		super(hmin,hmax,hstep,nstep);
		this.nSim=nsim;
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public PriorRatingCurveOptions(PriorRatingCurveOptions x) {
		super(x);
		if(x==null){return;}
		this.nSim=new Integer(x.getnSim());
	}

	@Override
	public String toString(){
		String out="";
		String s1,s2,s3;
		if(this.gethMin()==null){s1=Double.toString(Constants.D_MISSING);} else {s1=Double.toString(this.gethMin());}
		if(this.gethMax()==null){s2=Double.toString(Constants.D_MISSING);} else {s2=Double.toString(this.gethMax());}
		if(this.gethStep()==null){s3=Double.toString(Constants.D_MISSING);} else {s3=Double.toString(this.gethStep());}
		out=Integer.toString(this.nSim)+System.getProperty("line.separator")+
				s1+SEP+s2+SEP+s3;
		return out;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public int getnSim() {
		return nSim;
	}

	public void setnSim(int nSim) {
		this.nSim = nSim;
	}
}
