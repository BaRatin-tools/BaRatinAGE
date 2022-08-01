package Utils;


public class Dictionnary {

	private static Dictionnary instance;

	private String fileNotFound = "Le fichier n'a pas été trouvé";

	private String iOException = "Une erreur est survenue dans la gestion du fichier";

	private String numFormatException = "Donnée erronéee dans le fichier";
	
	private String errorOccurred = "Une erreur est survenue";
	
	private String dataSet = "Catalogues";
	
	private String gauging = "Jeux de jaugeages";
	
	private String configHydrau = "Configurations hydrauliques";
	
	private String limni = "Limnigrammes";
	
	private String errorData = "Erreurs";
	
	private String importationFrameTitle = "Importation de données";
	
	private String nameLabel = "Nom";
	
	private String fileLabel = "Fichier";
	
	private String validate = "Valider";
	
	private String browse = "Parcourir";
	
	private String cancel = "Annuler";
	
	private String describeGauginFile = "Fichiers de jaugeages";


	public String getDataSet() {
		return dataSet;
	}

	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}

	public String getGauging() {
		return gauging;
	}

	public void setGauging(String gauging) {
		this.gauging = gauging;
	}

	public String getConfigHydrau() {
		return configHydrau;
	}

	public void setConfigHydrau(String configHydrau) {
		this.configHydrau = configHydrau;
	}

	public static synchronized Dictionnary getInstance(){

		if (instance == null){
			instance = new Dictionnary();
		}
		return instance;
	}

	private Dictionnary() {
		this.load(Config.getInstance().getLanguage());

	}
	
	private void load(String language){
		
	}

	/**
	 * @return the fILE_NOT_FOUND
	 */
	public String getFileNotFound() {
		return this.fileNotFound;
	}

	/**
	 * @return the iO_EXCEPTION
	 */
	public String getIOException() {
		return this.iOException;
	}

	/**
	 * @return the nUM_FORMAT_EXCEPTION
	 */
	public String getNumFormatException() {
		return this.numFormatException;
	}

	/**
	 * @return the lIMNI
	 */
	public String getLimni() {
		return this.limni;
	}

	/**
	 * @param limni the lIMNI to set
	 */
	public void setLimni(String limni) {
		this.limni = limni;
	}

	/**
	 * @return the eRROR
	 */
	public String getErrorData() {
		return this.errorData;
	}

	/**
	 * @param error the eRROR to set
	 */
	public void setErrorData(String error) {
		this.errorData = error;
	}

	/**
	 * @return the eRROR_OCCURED
	 */
	public String getErrorOccurred() {
		return this.errorOccurred;
	}

	/**
	 * @param eRROR_OCCURED the eRROR_OCCURED to set
	 */
	public void setErrorOccurred(String errorOccurred) {
		this.errorOccurred = errorOccurred;
	}

	/**
	 * @return the importationFrameTitle
	 */
	public String getImportationFrameTitle() {
		return importationFrameTitle;
	}

	/**
	 * @param importationFrameTitle the importationFrameTitle to set
	 */
	public void setImportationFrameTitle(String importationFrameTitle) {
		this.importationFrameTitle = importationFrameTitle;
	}

	/**
	 * @return the nameLabel
	 */
	public String getNameLabel() {
		return nameLabel;
	}

	/**
	 * @param nameLabel the nameLabel to set
	 */
	public void setNameLabel(String nameLabel) {
		this.nameLabel = nameLabel;
	}

	/**
	 * @return the nameFile
	 */
	public String getFileLabel() {
		return fileLabel;
	}

	/**
	 * @param nameFile the nameFile to set
	 */
	public void setFileLabel(String nameFile) {
		this.fileLabel = nameFile;
	}

	/**
	 * @return the validate
	 */
	public String getValidate() {
		return validate;
	}

	/**
	 * @param validate the validate to set
	 */
	public void setValidate(String validate) {
		this.validate = validate;
	}

	/**
	 * @return the browse
	 */
	public String getBrowse() {
		return browse;
	}

	/**
	 * @param browse the browse to set
	 */
	public void setBrowse(String browse) {
		this.browse = browse;
	}

	/**
	 * @return the cancel
	 */
	public String getCancel() {
		return cancel;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(String cancel) {
		this.cancel = cancel;
	}

	/**
	 * @return the describeGauginFile
	 */
	public String getDescribeGauginFile() {
		return describeGauginFile;
	}

	/**
	 * @param describeGauginFile the describeGauginFile to set
	 */
	public void setDescribeGauginFile(String describeGauginFile) {
		this.describeGauginFile = describeGauginFile;
	}
	

}
