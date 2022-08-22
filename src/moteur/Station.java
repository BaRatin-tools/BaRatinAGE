package moteur;

import java.util.List;

/**
 * Station object, Master class that will consolidate all datasets and data on the station itself
 * @author Sylvain Vigneau, Ben Renard, Irstea Lyon
 */

public class Station {

	private static Station instance;

	private String ID;
	private String name;

	private Dataset<GaugingSet> gauging = new Dataset<GaugingSet>();
	private Dataset<ConfigHydrau> config = new Dataset<ConfigHydrau>();
	private Dataset<RemnantError> remnant = new Dataset<RemnantError>();
	private Dataset<Limnigraph> limni = new Dataset<Limnigraph>();
	private Dataset<RatingCurve> rc= new Dataset<RatingCurve>();
	private Dataset<Hydrograph> hydrograph = new Dataset<Hydrograph>();
	// defaults
	private static final String def_ID="id";
	private static final String def_name="station";
	
	public static synchronized Station getInstance() {
		if (instance == null){
			try {
				instance = new Station();
			} catch (Exception e) {
			}
		}
		return instance;
	}

	public static synchronized Station getInstance(String nom) {
		if (instance == null){
			try {
				instance = new Station(nom);
			} catch (Exception e) {
			}
		}
		return instance;
	}

	/**
	 * Full Constructor
	 * @param name name
	 * @param id ID
	 * @throws Exception 
	 */
	private Station(String name, String ID) throws Exception{
		this.setName(name);
		this.setID(ID);
		this.getRemnant().add(RemnantError.Remnant_Linear);
		this.getRemnant().add(RemnantError.Remnant_Constant);
		this.getRemnant().add(RemnantError.Remnant_Proportional);
	}

	/**
	 * default constructor
	 * @throws Exception 
	 */
	private Station() throws Exception {
		this(def_name,def_ID);
	}

	/**
	 * Partial constructor
	 * @param name name
	 * @throws Exception 
	 */
	private Station(String name) throws Exception{
		this(name,"id");
	}
	
	public void clear() throws Exception{
		this.setID(def_ID);
		this.setName(def_name);
		this.setConfig(new Dataset<ConfigHydrau>());
		this.setGauging(new Dataset<GaugingSet>());
		this.setLimni(new Dataset<Limnigraph>());
		this.setRc(new Dataset<RatingCurve>());
		this.setHydrograph(new Dataset<Hydrograph>());		
	}
	
	public boolean isEmpty(){
		boolean out = this.getConfig().getSize()==0 & this.getGauging().getSize()==0 & this.getLimni().getSize()==0 
				      & this.getRc().getSize()==0 & this.getHydrograph().getSize()==0;
		return out;
		
	}

	/////////////////////////////////////////////////////////
	// ADD STUFF
	/////////////////////////////////////////////////////////

	public void addGaugingSet(String file, String id) throws Exception{
		this.gauging.add(new GaugingSet(file, id));
	}

	public void addGaugingSet(String id) throws Exception{
		this.gauging.add(new GaugingSet(id));
	}

	public void addConfig(String id) throws Exception{
		this.config.add(new ConfigHydrau(id));
	}

	public void addRatingCurve(String id) throws Exception{
		this.rc.add(new RatingCurve(id));
	}

	public void addLimnigraph(String id) throws Exception{
		this.limni.add(new Limnigraph(id));
	}

	public void addHydrograph(String id) throws Exception{
		this.hydrograph.add(new Hydrograph(id));
	}

	/////////////////////////////////////////////////////////
	// DELETE STUFF
	/////////////////////////////////////////////////////////

	public void deleteConfig(String id){
		this.config.delete(id);
	}
	
	public void deleteGaugingSet(String id){
		this.gauging.delete(id);
	}

	public void deleteLimnigraph(String id){
		this.limni.delete(id);
	}

	public void deleteRatingCurve(String id){
		this.rc.delete(id);
	}
	
	public void deleteHydrograph(String id){
		this.hydrograph.delete(id);
	}


	
	/////////////////////////////////////////////////////////
	// GET STUFF FROM NAME
	/////////////////////////////////////////////////////////

	public int getGaugingIndex(String id){
		return this.getGauging().getIndxByName(id);		
	}
	
	public GaugingSet getGauging(String id){
		return this.getGaugingAt(getGaugingIndex(id));		
	}
	
	public int getHydrauConfigIndex(String id){
		return this.getConfig().getIndxByName(id);		
	}
	
	public ConfigHydrau getHydrauConfig(String id){
		return this.getConfigAt(getHydrauConfigIndex(id));		
	}
	
	public int getHydrauConfigNcontrol(String id){
		ConfigHydrau hydrau = this.getHydrauConfig(id);
		List<HydrauControl> HydrauControl = hydrau.getControls();
		if(HydrauControl==null) {return 0;}
		else {return HydrauControl.size();}
	}
	
	public int getRemnantIndex(String id){
		return this.getRemnant().getIndxByName(id);		
	}
	
	public RemnantError getRemnant(String id){
		return this.getRemnantAt(getRemnantIndex(id));		
	}

	public int getRatingCurveIndex(String id){
		return this.getRc().getIndxByName(id);
	}
	
	public RatingCurve getRatingCurve(String id){
		return this.getRatingCurveAt(getRatingCurveIndex(id));		
	}

	public int getLimnigraphIndex(String id){
		return this.getLimni().getIndxByName(id);
	}
	
	public Limnigraph getLimnigraph(String id){
		return this.getLimnigraphAt(getLimnigraphIndex(id));		
	}

	public int getHydrographIndex(String id){
		return this.getHydrograph().getIndxByName(id);
	}
	
	public Hydrograph getHydrograph(String id){
		return this.getHydrographAt(getHydrographIndex(id));		
	}

	/////////////////////////////////////////////////////////
	// GET STUFF FROM POSITION
	/////////////////////////////////////////////////////////

	public ConfigHydrau getConfigAt(int indx) {
		ConfigHydrau config=this.getConfig().getItemAt(indx);
		return config;
	}
	
	public GaugingSet getGaugingAt(int indx) {
		GaugingSet gauging=this.getGauging().getItemAt(indx);
		return gauging;
	}
		
	public RemnantError getRemnantAt(int indx) {
		RemnantError remnant=this.getRemnant().getItemAt(indx);
		return remnant;
	}
	
	public RatingCurve getRatingCurveAt(int indx) {
		RatingCurve gauging=this.getRc().getItemAt(indx);
		return gauging;
	}

	public Limnigraph getLimnigraphAt(int indx) {
		Limnigraph limni=this.getLimni().getItemAt(indx);
		return limni;
	}

	public Hydrograph getHydrographAt(int indx) {
		Hydrograph hyd=this.getHydrograph().getItemAt(indx);
		return hyd;
	}

	/////////////////////////////////////////////////////////
	// SET STUFF AT POSITION
	/////////////////////////////////////////////////////////

	public void setConfigAt(int indx,ConfigHydrau config) {
		this.config.setItemAt(indx,config);
	}
	
	public void setGaugingAt(int indx,GaugingSet gauging) {
		this.gauging.setItemAt(indx,gauging);
	}

	public void setRatingCurveAt(int indx,RatingCurve rc) {
		this.rc.setItemAt(indx,rc);
	}

	public void setLimnigraphAt(int indx,Limnigraph limni) {
		this.limni.setItemAt(indx,limni);
	}

	public void setHydrographAt(int indx,Hydrograph hyd) {
		this.hydrograph.setItemAt(indx,hyd);
	}

	/////////////////////////////////////////////////////////
	// BASIC GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getID() {
		return ID;
	}
	
	public void setID(String iD) {
		ID = iD;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Dataset<GaugingSet> getGauging() {
		return gauging;
	}
	
	public void setGauging(Dataset<GaugingSet> gauging) {
		this.gauging = gauging;
	}
	
	public Dataset<ConfigHydrau> getConfig() {
		return config;
	}
	
	public void setConfig(Dataset<ConfigHydrau> config) {
		this.config = config;
	}
	
	public Dataset<RemnantError> getRemnant() {
		return remnant;
	}
	
	public void setRemnant(Dataset<RemnantError> remnant) {
		this.remnant = remnant;
	}
	
	public Dataset<Limnigraph> getLimni() {
		return limni;
	}
	
	public void setLimni(Dataset<Limnigraph> limni) {
		this.limni = limni;
	}

	public static void setInstance(Station instance) {
		Station.instance = instance;
	}
	
	public Dataset<RatingCurve> getRc() {
		return rc;
	}
	
	public void setRc(Dataset<RatingCurve> rc) {
		this.rc = rc;
	}

	
	/**
	 * @return the hydrograph
	 */
	public Dataset<Hydrograph> getHydrograph() {
		return hydrograph;
	}

	
	/**
	 * @param hydrograph the hydrograph to set
	 */
	public void setHydrograph(Dataset<Hydrograph> hydrograph) {
		this.hydrograph = hydrograph;
	}

}
