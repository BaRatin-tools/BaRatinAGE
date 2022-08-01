package moteur;

import commons.TimeSerie;

public class BaRatinHydrograph {
	
	private TimeSerie limni;
	private BaRatinRatingCurve RC;
	private TimeSerie hydro;
	
	public BaRatinHydrograph(){
		this.limni= new TimeSerie();
		this.RC=new BaRatinRatingCurve();
		this.hydro= new TimeSerie();		
	}

	public TimeSerie getLimni() {
		return limni;
	}

	public void setLimni(TimeSerie limni) {
		this.limni = limni;
	}

	public BaRatinRatingCurve getRC() {
		return RC;
	}

	public void setRC(BaRatinRatingCurve rC) {
		RC = rC;
	}

	public TimeSerie getHydro() {
		return hydro;
	}

	public void setHydro(TimeSerie hydro) {
		this.hydro = hydro;
	}
	

}
