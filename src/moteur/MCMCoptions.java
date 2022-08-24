package moteur;

public class MCMCoptions {

	private int nAdapt;
	private int nCycles;
	private double burn;
	private int nSlim;
	private double minMR;
	private double maxMR;
	private double downMult;
	private double upMult;
	private String filename;
	private static final String filename_def="Results_MCMC.txt";

	/**
	 * full constructor
	 * @param nAdapt
	 * @param nCycles
	 * @param burn
	 * @param nSlim
	 * @param minMR
	 * @param maxMR
	 * @param downMult
	 * @param upMult
	 * @param filename
	 */
	public MCMCoptions(int nAdapt, int nCycles, double burn, int nSlim, double minMR, double maxMR, double downMult, double upMult, String filename){
		this.nAdapt=nAdapt;
		this.nCycles=nCycles;
		this.burn=burn;
		this.nSlim=nSlim;
		this.minMR=minMR;
		this.maxMR=maxMR;
		this.downMult=downMult;
		this.upMult=upMult;
		this.filename=filename;
	}
	
	/**
	 * partial constructor
	 * @param nAdapt
	 * @param nCycles
	 * @param burn
	 * @param nSlim
	 * @param minMR
	 * @param maxMR
	 * @param downMult
	 * @param upMult
	 */
	public MCMCoptions(int nAdapt, int nCycles, double burn, int nSlim, double minMR, double maxMR, double downMult, double upMult){
		this.nAdapt=nAdapt;
		this.nCycles=nCycles;
		this.burn=burn;
		this.nSlim=nSlim;
		this.minMR=minMR;
		this.maxMR=maxMR;
		this.downMult=downMult;
		this.upMult=upMult;
		this.filename=filename_def;
	}
	
	/**
	 * default constructor
	 */
	public MCMCoptions(){
		this(100,100,0.5,10,0.1,0.5,0.9,1.1,filename_def);
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public MCMCoptions(MCMCoptions x){
		if(x==null){return;}
		this.nAdapt= Integer.valueOf(x.getnAdapt());
		this.nCycles= Integer.valueOf(x.getnCycles());
		this.burn= Double.valueOf(x.getBurn());
		this.nSlim= Integer.valueOf(x.getnSlim());
		this.minMR= Double.valueOf(x.getMinMR());
		this.maxMR= Double.valueOf(x.getMaxMR());
		this.downMult= Double.valueOf(x.getDownMult());
		this.upMult= Double.valueOf(x.getUpMult());
		if(x.getFilename()!=null) this.filename=new String(x.getFilename());
	}
	

	@Override
	public String toString(){
		String out="";
		out=toString_mini()+System.getProperty("line.separator")+this.filename;
		return out;
	}

	public String toString_mini(){
		String out="";
		out=Integer.toString(this.nAdapt)+System.getProperty("line.separator")+
				Integer.toString(this.nCycles)+System.getProperty("line.separator")+
				Double.toString(this.burn)+System.getProperty("line.separator")+
				Integer.toString(this.nSlim)+System.getProperty("line.separator")+
				Double.toString(this.minMR)+System.getProperty("line.separator")+
				Double.toString(this.maxMR)+System.getProperty("line.separator")+
				Double.toString(this.downMult)+System.getProperty("line.separator")+
				Double.toString(this.upMult);
		return out;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public int getnAdapt() {
		return nAdapt;
	}

	public void setnAdapt(int nAdapt) {
		this.nAdapt = nAdapt;
	}

	public int getnCycles() {
		return nCycles;
	}

	public void setnCycles(int nCycles) {
		this.nCycles = nCycles;
	}

	public double getBurn() {
		return burn;
	}

	public void setBurn(double burn) {
		this.burn = burn;
	}

	public int getnSlim() {
		return nSlim;
	}

	public void setnSlim(int nSlim) {
		this.nSlim = nSlim;
	}

	public double getMinMR() {
		return minMR;
	}

	public void setMinMR(double minMR) {
		this.minMR = minMR;
	}

	public double getMaxMR() {
		return maxMR;
	}

	public void setMaxMR(double maxMR) {
		this.maxMR = maxMR;
	}

	public double getDownMult() {
		return downMult;
	}

	public void setDownMult(double downMult) {
		this.downMult = downMult;
	}

	public double getUpMult() {
		return upMult;
	}

	public void setUpMult(double upMult) {
		this.upMult = upMult;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


}
