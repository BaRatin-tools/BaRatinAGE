package moteur;

public class IntervalRatingPoints {

	private int minLimit;
	private int maxLimit;
	
	public IntervalRatingPoints(final int min, final int max) throws Exception
	{
		if (min < max) {
			this.minLimit = min;
			this.maxLimit = max;
		}
		else 
		{
			throw new Exception("Veuillez donner les valeurs de l'intervalle comme suit : "
					+ "\n <min>, <max>.");
		}
	}

	/**
	 * @return the minLimit
	 */
	public int getMinLimit() {
		return minLimit;
	}

	/**
	 * @param minLimit the minLimit to set
	 */
	public void setMinLimit(int minLimit) {
		this.minLimit = minLimit;
	}

	/**
	 * @return the maxLimit
	 */
	public int getMaxLimit() {
		return maxLimit;
	}

	/**
	 * @param maxLimit the maxLimit to set
	 */
	public void setMaxLimit(int maxLimit) {
		this.maxLimit = maxLimit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxLimit;
		result = prime * result + minLimit;
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
		IntervalRatingPoints other = (IntervalRatingPoints) obj;
		if (maxLimit != other.maxLimit)
			return false;
		if (minLimit != other.minLimit)
			return false;
		return true;
	}
	
	
}
