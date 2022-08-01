package commons;

/**
 * start-end period of time
 * @author Kevin Mokili, Irstea Lyon
 * TODO: complete
 */
public class Period {

	private Time start;
	private Time end;
	
	/**
	 * Full constructor
	 * @param initialDate
	 * @param finalDate
	 * @throws Exception
	 */
	public Period(Time initialDate, Time finalDate) throws Exception{
		if (!initialDate.isOlder(finalDate)) {
			this.start = initialDate;
			this.setEnd(finalDate);
		}
		else
		{
			throw new Exception("Initial date is after final date."
					+ "\nPlease give the dates in this following order: "
					+ "\n <initial-date>, <end-date>.");
		}
	}
	
	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the start
	 */
	public Time getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Time start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Time getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(Time end) {
		this.end = end;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Period other = (Period) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
}
