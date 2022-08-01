package moteur;

import java.util.Arrays;

import commons.Constants;
import commons.Parameter;

/**
 * Class representing hydraulic controls: Q=a(h-b)^c
 * @author Sylvain Vigneau - Benjamin Renard, Irstea Lyon *
 */
public class HydrauControl {
	private Parameter a=new Parameter();
	private Parameter b=new Parameter();
	private Parameter c=new Parameter();
	private Parameter k=new Parameter(); // Stage at which the control gets activated
	private int type; // section or channel? - see class Combo_ControlType
	private String description="";
	// type-specific parameters
	private Parameter[] specifix;

	// default values
	private static final int type_def=-1;
	private static final String description_def=Constants.S_EMPTY;

	/**
	 * full constructor
	 * @param a
	 * @param b
	 * @param c
	 * @param k
	 * @param specifix
	 * @param type
	 * @param description
	 */
	public HydrauControl(Parameter a,Parameter b,Parameter c,Parameter k,
			Parameter[] specifix,
			int type,String description) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.k = k;
		this.type=type;
		this.description=description;
		this.specifix=specifix;
	}

	/**
	 * partial constructor
	 * @param a
	 * @param b
	 * @param c
	 * @param k
	 * @param type
	 * @param description
	 */
	public HydrauControl(Parameter a,Parameter b,Parameter c,Parameter k,int type,String description) {
		this(a,b,c,k,null,type,description);
	}

	/**
	 * partial constructor
	 * @param a
	 * @param b
	 * @param c
	 * @param k
	 * @param type
	 */
	public HydrauControl(Parameter a,Parameter b,Parameter c,Parameter k,int type) {
		this(a,b,c,k,null,type,description_def);
	}

	/**
	 * partial constructor
	 * @param a
	 * @param b
	 * @param c
	 * @param k
	 */
	public HydrauControl(Parameter a,Parameter b,Parameter c,Parameter k) {
		this(a,b,c,k,null,type_def,description_def);
	}

	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public HydrauControl(HydrauControl x) {
		if(x==null){return;}
		this.type=x.getType();
		if(x.getA()!=null) this.a = new Parameter(x.getA());
		if(x.getB()!=null) this.b = new Parameter(x.getB());
		if(x.getC()!=null) this.c = new Parameter(x.getC());
		if(x.getK()!=null) this.k = new Parameter(x.getK());
		if(x.getDescription()!=null) this.description=new String(x.getDescription());
		if(x.getSpecifix()!=null) {
			this.specifix=new Parameter[x.getSpecifix().length];
			for(int i=0;i<x.getSpecifix().length;i++){
				if(x.getSpecifix()[i]!=null) this.specifix[i]=new Parameter(x.getSpecifix()[i]);
			}
		}
	}

	public HydrauControl() {
		this(new Parameter(),new Parameter(),new Parameter(),new Parameter(),null,type_def,description_def);
	}

	@Override
	public String toString(){
		String out="";
		out=out+type+System.getProperty("line.separator");
		out=out+description+System.getProperty("line.separator");
		if(a!=null){out=out+a.toString();}
		out=out+System.getProperty("line.separator");
		if(b!=null){out=out+b.toString();}
		out=out+System.getProperty("line.separator");
		if(c!=null){out=out+c.toString();}
		out=out+System.getProperty("line.separator");
		if(k!=null){out=out+k.toString();}
		return out;
	}

	public String toString_abc(){
		String out="";
		if(a!=null){out=out+a.toString();}
		out=out+System.getProperty("line.separator");
		if(b!=null){out=out+b.toString();}
		out=out+System.getProperty("line.separator");
		if(c!=null){out=out+c.toString();}
		return out;
	}

	public String toString_akc(){
		String out="";
		if(a!=null){out=out+a.toString();}
		out=out+System.getProperty("line.separator");
		if(k!=null){out=out+k.toString();}
		out=out+System.getProperty("line.separator");
		if(c!=null){out=out+c.toString();}
		return out;
	}

	public String toString_kac(){
		String out="";
		if(k!=null){out=out+k.toString();}
		out=out+System.getProperty("line.separator");
		if(a!=null){out=out+a.toString();}
		out=out+System.getProperty("line.separator");
		if(c!=null){out=out+c.toString();}
		return out;
	}

	public Parameter getA() {
		return a;
	}

	public void setA(Parameter a) {
		this.a = a;
	}

	public Parameter getB() {
		return b;
	}

	public void setB(Parameter b) {
		this.b = b;
	}

	public Parameter getC() {
		return c;
	}

	public void setC(Parameter c) {
		this.c = c;
	}

	public Parameter getK() {
		return k;
	}

	public void setK(Parameter k) {
		this.k = k;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Parameter[] getSpecifix() {
		return specifix;
	}

	public void setSpecifix(Parameter[] specifix) {
		this.specifix = specifix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		result = prime * result + Arrays.hashCode(specifix);
		result = prime * result + type;
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
		HydrauControl other = (HydrauControl) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		if (!Arrays.equals(specifix, other.specifix))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
