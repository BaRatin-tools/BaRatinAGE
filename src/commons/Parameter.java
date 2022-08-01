package commons;

/**
 * Parameter object
 * @author Ben Renard, Irstea Lyon
 */
public class Parameter {
	private String name;
	private Double value;
	private Distribution prior;
	// default values
	private static final String name_def=Constants.S_UNKNOWN;
	private static final Double value_def=Constants.D_MISSING;
	private static final Distribution prior_def=Distribution.D_FlatPrior;

	/**
	 * Full constructor
	 * @param name name
	 * @param value value
	 * @param dist prior distribution
	 */
	public Parameter(String name, Double value,Distribution dist){
		this.setName(name);
		this.setValue(value);
		this.setPrior(dist);
	}

	/**
	 * Default constructor
	 */
	public Parameter() {
		this(name_def,value_def,prior_def);
	}

	/**
	 * Partial constructor
	 * @param value parameter value
	 */
	public Parameter(Double value){
		this(name_def,value,prior_def);
	}

	/**
	 * Partial constructor
	 * @param name parameter name
	 * @param value parameter value
	 */
	public Parameter(String name, Double value){
		this(name,value,prior_def);
	}

	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public Parameter(Parameter x) {
		if(x==null){return;}
		if(x.getName()!=null){
			this.name=new String(x.getName());
		}
		if(x.getValue()!=null){
			this.value=new Double(x.getValue());
		}
		if(x.getPrior()!=null){
			this.prior=new Distribution(x.getPrior());	
		}
	}

	/**
	 * String representation of a parameter
	 */
	@Override
	public String toString() {
		String out="";
		if(this.name!=null){out=out+this.name;}
		out=out+System.getProperty("line.separator");
		if(this.value!=null){out=out+this.value.toString();}
		out=out+System.getProperty("line.separator");
		if(this.prior!=null){out=out+this.prior.toString();}
		return out;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Distribution getPrior() {
		return prior;
	}

	public void setPrior(Distribution prior) {
		this.prior = prior;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((prior == null) ? 0 : prior.hashCode());
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
		if (!(obj instanceof Parameter)) {
			return false;
		}
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (prior == null) {
			if (other.prior != null) {
				return false;
			}
		} else if (!prior.equals(other.prior)) {
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
