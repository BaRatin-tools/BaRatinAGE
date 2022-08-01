package moteur;

/**
 * Options applicable to posterior rating curves
 * @author Ben Renard, Irstea Lyon
 */
public class PostRatingCurveOptions extends RatingCurveOptions{


	/**
	 * default constructor
	 */
	public PostRatingCurveOptions(){
		super();
	}

	/**
	 * full constructor
	 * @param hmin
	 * @param hmax
	 * @param hstep
	 * @param nstep
	 */
	public PostRatingCurveOptions(Double hmin,Double hmax,Double hstep,int nstep) {
		super(hmin,hmax,hstep,nstep);
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public PostRatingCurveOptions(PostRatingCurveOptions x) {
		super(x);
		if(x==null){return;}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////


}

