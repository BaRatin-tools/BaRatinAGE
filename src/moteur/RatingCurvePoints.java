package moteur;

import java.util.ArrayList;
import java.util.List;

import commons.Period;


public class RatingCurvePoints {
	
	private int code;
	private String label;
	private int curveType;
	private IntervalRatingPoints itv;  //gamme de validité de la courbe
	private String codeStationHydro;
	
	private List<RatingPoints> points = new ArrayList<RatingPoints>();
	
	private Period periodeUtilCourbe;
	
	public RatingCurvePoints(final int code, final String label, 
			final int courbeType, final IntervalRatingPoints itv, 
			final String codeStatHydro, final Period periodeUtilCourbe,
			final List<RatingPoints> ltp) 
	{
		this.setCode(code);
		this.setLabel(label);
		this.setCurveType(courbeType);
		this.setItv(itv);
		this.setCodeStationHydro(codeStatHydro);
		this.setPeriodeUtilCourbe(periodeUtilCourbe);
		this.setPoints(ltp);
	}

	public RatingCurvePoints() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the curveType
	 */
	public int getCurveType() {
		return curveType;
	}

	/**
	 * @param curveType the curveType to set
	 */
	public void setCurveType(int courbeType) {
		this.curveType = courbeType;
	}

	/**
	 * @return the itv
	 */
	public IntervalRatingPoints getItv() {
		return itv;
	}

	/**
	 * @param itv the itv to set
	 */
	public void setItv(IntervalRatingPoints itv) {
		this.itv = itv;
	}

	/**
	 * @return the codeStationHydro
	 */
	public String getCodeStationHydro() {
		return codeStationHydro;
	}

	/**
	 * @param codeStationHydro the codeStationHydro to set
	 */
	public void setCodeStationHydro(String codeStationHydro) {
		this.codeStationHydro = codeStationHydro;
	}

	/**
	 * @return the periodeUtilCourbe
	 */
	public Period getPeriodeUtilCourbe() {
		return periodeUtilCourbe;
	}

	/**
	 * @param periodeUtilCourbe the periodeUtilCourbe to set
	 */
	public void setPeriodeUtilCourbe(Period periodeUtilCourbe) {
		this.periodeUtilCourbe = periodeUtilCourbe;
	}

	/**
	 * @return the points
	 */
	public List<RatingPoints> getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(List<RatingPoints> lesPoints) {
		this.points = lesPoints;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime
				* result
				+ ((codeStationHydro == null) ? 0 : codeStationHydro.hashCode());
		result = prime * result + curveType;
		result = prime * result + ((itv == null) ? 0 : itv.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime
				* result
				+ ((periodeUtilCourbe == null) ? 0 : periodeUtilCourbe
						.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RatingCurvePoints other = (RatingCurvePoints) obj;
		if (code != other.code)
			return false;
		if (codeStationHydro == null) {
			if (other.codeStationHydro != null)
				return false;
		} else if (!codeStationHydro.equals(other.codeStationHydro))
			return false;
		if (curveType != other.curveType)
			return false;
		if (itv == null) {
			if (other.itv != null)
				return false;
		} else if (!itv.equals(other.itv))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (periodeUtilCourbe == null) {
			if (other.periodeUtilCourbe != null)
				return false;
		} else if (!periodeUtilCourbe.equals(other.periodeUtilCourbe))
			return false;
		return true;
	}
	
	
	
}
