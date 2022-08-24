package commons;


public class Observation {

	private Double value=Constants.D_MISSING;

	private Time obsDate;

	private Integer qualityCode=Constants.I_MISSING;	

	/**
	 * empty constructor
	 */
	public Observation() {}

	/**
	 * full constructor
	 * @param value
	 * @param obsDate
	 * @param qualityCode
	 */
	public Observation(Double value, Time obsDate, Integer qualityCode) {
		this.value = value;

		this.obsDate = obsDate;

		this.qualityCode = qualityCode;
	}

	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public Observation(Observation x){
		if(x==null){return;}
		if(x.getValue()!=null){
			this.value=Double.valueOf(x.getValue());
		}
		if(x.getObsDate()!=null){		
			this.obsDate=new Time(x.getObsDate());
		}
		if(x.getQualityCode()!=null){		
			this.qualityCode=Integer.valueOf(x.getQualityCode());
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Time getObsDate() {
		return obsDate;
	}

	public void setObsDate(Time obsDate) {
		this.obsDate = obsDate;
	}

	public Integer getQualityCode() {
		return qualityCode;
	}

	public void setQualityCode(Integer qualityCode) {
		this.qualityCode = qualityCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obsDate == null) ? 0 : obsDate.hashCode());
		result = prime * result
				+ ((qualityCode == null) ? 0 : qualityCode.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof Observation)) {
			return false;
		}
		Observation other = (Observation) obj;
		if (obsDate == null) {
			if (other.obsDate != null) {
				return false;
			}
		} else if (!obsDate.equals(other.obsDate)) {
			return false;
		}
		if (qualityCode == null) {
			if (other.qualityCode != null) {
				return false;
			}
		} else if (!qualityCode.equals(other.qualityCode)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
