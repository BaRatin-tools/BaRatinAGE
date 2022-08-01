package moteur;

public class BaRatinRatingCurve {
	
	private RatingCurve RC;
	
	public BaRatinRatingCurve(){
		this.setRC(new RatingCurve());
	}

	/**
	 * @return the rC
	 */
	public RatingCurve getRC() {
		return RC;
	}

	/**
	 * @param rC the rC to set
	 */
	public void setRC(RatingCurve rC) {
		RC = rC;
	};

}
