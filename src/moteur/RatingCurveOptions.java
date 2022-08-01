package moteur;

import commons.Constants;

/**
 * Options applicable to posterior rating curves
 * @author Ben Renard, Irstea Lyon
 */
public class RatingCurveOptions {

	private Double hMin;
	private Double hMax;
	private Double hStep;
	private int nStep;

	// constants
	public static final String SEP=",";
	private static final int nStep_def=101;

	/**
	 * default constructor
	 */
	public RatingCurveOptions(){
		this.hMin=Constants.D_MISSING;
		this.hMax=Constants.D_MISSING;
		this.hStep=Constants.D_MISSING;
		this.nStep=nStep_def;
	}

	/**
	 * full constructor
	 * @param hmin
	 * @param hmax
	 * @param hstep
	 * @param nstep
	 */
	public RatingCurveOptions(Double hmin,Double hmax,Double hstep,int nstep) {
		this.hMin=hmin;
		this.hMax=hmax;
		this.hStep=hstep;
		this.nStep=nstep;
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public RatingCurveOptions(RatingCurveOptions x) {
		if(x==null){return;}
		if(x.gethMin()!=null){
			this.hMin=new Double(x.gethMin());
		}
		if(x.gethMax()!=null){
			this.hMax=new Double(x.gethMax());
		}
		if(x.gethStep()!=null){
			this.hStep=new Double(x.gethStep());
		}
		this.nStep=new Integer(x.getnStep());
	}

	@Override
	public String toString(){
		String out="";
		out=Double.toString(this.hMin)+SEP+
				Double.toString(this.hMax)+SEP+
				Double.toString(this.hStep);
		return out;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public Double gethMin() {
		return hMin;
	}

	public void sethMin(Double hMin) {
		this.hMin = hMin;
	}

	public Double gethMax() {
		return hMax;
	}

	public void sethMax(Double hMax) {
		this.hMax = hMax;
	}

	public Double gethStep() {
		return hStep;
	}

	public void sethStep(Double hStep) {
		this.hStep = hStep;
	}

	public int getnStep() {
		return nStep;
	}

	public void setnStep(int nStep) {
		this.nStep = nStep;
	}

}
