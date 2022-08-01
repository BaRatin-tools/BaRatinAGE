package commons;

import java.util.ArrayList;
import java.util.List;

import moteur.Item;

/**
 * Generic class for time series - limnigraphs and hydrographs will be extended from this
 * @author Sylvain Vigneau & Ben Renard, Irstea Lyon
 */
public class TimeSerie extends Item {

	private String type="";	
	private List<Observation> observations=new ArrayList<Observation>();

	/**
	 * full constructor
	 * @param name
	 * @param description
	 * @param type
	 * @param observations
	 */
	public TimeSerie(String name,String description,String type,List<Observation> observations) {
		super(name,description);
		this.type=type;
		this.observations=observations;
	}

	/**
	 * partial constructor
	 * @param name
	 * @param description
	 * @param observations
	 */
	public TimeSerie(String name,String description,List<Observation> observations) {
		this(name,description,"",observations);
	}

	/**
	 * empty constructor
	 */
	public TimeSerie() {
		super();
	}

	/**
	 * partial constructor
	 * @param name
	 */
	public TimeSerie(String name) {
		super(name);
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public TimeSerie(TimeSerie x) {
		super(x);
		if(x==null){return;}
		if(x.getType()!=null){
			this.type=new String(x.getType());
		}
		if(x.getObservations()!=null){
			this.observations=new ArrayList<Observation>();
			for(int i=0;i<x.getObservations().size();i++){
				if(x.getObservations().get(i)!=null) 
					this.observations.add(new Observation(x.getObservations().get(i)));
			}
		}
	}

	public void addObservation(Observation obs){
		this.observations.add(obs);
	}

	public void addObservation(Double value, Time obsDate, Integer qualityCode){
		this.observations.add(new Observation(value, obsDate, qualityCode));
	}

	public int length(){
		return this.getObservations().size();
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Observation> getObservations() {
		return observations;
	}

	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((observations == null) ? 0 : observations.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSerie other = (TimeSerie) obj;
		if (observations == null) {
			if (other.observations != null)
				return false;
		} else if (!observations.equals(other.observations))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
