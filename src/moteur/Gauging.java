package moteur;

import commons.Time;

public class Gauging {
	
	private Double H;
	private Double uH;
	private Double Q;
	private Double uQ;
	private Boolean active;
	private Time t;
	
	/**
	 * full constructor
	 * @param t
	 * @param h
	 * @param uh
	 * @param q
	 * @param uq
	 * @param active
	 */
	public Gauging(Time t,Double h, Double uh,Double q, Double uq,boolean active) {
		this.t=t;
		this.active = active;		
		this.H = h;
		this.uH = uh;
		this.Q = q;
		this.uQ = uq;
	}

	/**
	 * default constructor
	 */
	public Gauging() {
		this(new Time(1,1,1,0,0,0),0.0,0.0,0.0,0.0,true);		
	}
	
	/**
	 * partial constructor
	 * @param h
	 * @param uh
	 * @param q
	 * @param uq
	 */
	public Gauging(Double h, Double uh,Double q, Double uq) {
		this(new Time(1,1,1,0,0,0),h,uh,q,uq,true);
	}

	/**
	 * partial constructor
	 * @param h
	 * @param uh
	 * @param q
	 * @param uq
	 * @param active
	 */
	public Gauging(Double h, Double uh,Double q, Double uq, boolean active) {
		this(new Time(1,1,1,0,0,0),h,uh,q,uq,active);
	}
	
	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public Gauging(Gauging x){
		if(x==null){return;}
		if(x.getT()!=null) this.t=new Time(x.getT());
		if(x.getActive()!=null) this.active = new Boolean(x.getActive());		
		if(x.getH()!=null) this.H = new Double(x.getH());
		if(x.getuH()!=null) this.uH = new Double(x.getuH());
		if(x.getQ()!=null) this.Q = new Double(x.getQ());
		if(x.getuQ()!=null) this.uQ = new Double(x.getuQ());
	}

	//update of the fields of the gauging, principally used by the controller
	public void Update(Boolean actif,Double h, Double uh,Double q, Double uq) {
		this.active = actif;
		
		this.H = h;
		this.uH = uh;
		
		this.Q = q;
		this.uQ = uq;
		
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Double getH() {
		return H;
	}

	public void setH(Double h) {
		H = h;
	}

	public Double getuH() {
		return uH;
	}

	public void setuH(Double uH) {
		this.uH = uH;
	}

	public Double getQ() {
		return Q;
	}

	public void setQ(Double q) {
		Q = q;
	}

	public Double getuQ() {
		return uQ;
	}

	public void setuQ(Double uQ) {
		this.uQ = uQ;
	}
	
	/**
	 * @return the t
	 */
	public Time getT() {
		return t;
	}

	/**
	 * @param t the t to set
	 */
	public void setT(Time t) {
		this.t = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((H == null) ? 0 : H.hashCode());
		result = prime * result + ((Q == null) ? 0 : Q.hashCode());
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result + ((uH == null) ? 0 : uH.hashCode());
		result = prime * result + ((uQ == null) ? 0 : uQ.hashCode());
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
		Gauging other = (Gauging) obj;
		if (H == null) {
			if (other.H != null)
				return false;
		} else if (!H.equals(other.H))
			return false;
		if (Q == null) {
			if (other.Q != null)
				return false;
		} else if (!Q.equals(other.Q))
			return false;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		if (uH == null) {
			if (other.uH != null)
				return false;
		} else if (!uH.equals(other.uH))
			return false;
		if (uQ == null) {
			if (other.uQ != null)
				return false;
		} else if (!uQ.equals(other.uQ))
			return false;
		return true;
	}

}
