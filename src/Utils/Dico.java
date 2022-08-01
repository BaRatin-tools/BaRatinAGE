package Utils;

import java.util.HashMap;
import java.util.Iterator;

import commons.ReadWrite;

@SuppressWarnings("serial")
public class Dico extends HashMap<String,String> {

	private static Dico instance;
	public static synchronized Dico getInstance(String lang){
		if (instance == null){
			instance = new Dico(lang);
		}
		return instance;
	}
	private static final String unknown="Unknown dictionary entry";
	private static final int indx_fr=0;
	private static final int indx_en=1;
	private static final int indx_es=2;
	private static final int indx_de=3;
	private static final int indx_it=4;
	private static final int indx_br=5;
	private static final int indx_def=indx_en;

	public Dico(String lang){
		int indx=indx_def;
		HashMap<String,String[]> ML=GetMLdico();
		Iterator<String> key = ML.keySet().iterator();		
		if(lang.equals("fr")){indx=indx_fr;}
		else if(lang.equals("en")){indx=indx_en;}
		else if(lang.equals("es")){indx=indx_es;}
		else if(lang.equals("de")){indx=indx_de;}
		else if(lang.equals("it")){indx=indx_it;}
		else if(lang.equals("br")){indx=indx_br;}
		this.clear();
		while(key.hasNext()){
			String k = key.next();
			this.put(k, ML.get(k)[indx]);
		}
	}

	public String entry(String key){
		String out;
		if(this.get(key) == null){out=unknown;}
		else {out=this.get(key);}	
		return out;}

	public String[] entry(String[] keys){
		int n=keys.length;
		String[] out=new String[n];
		for(int i=0;i<n;i++){
			if(this.get(keys[i]) == null){out[i]=unknown;}
			else {out[i]=this.get(keys[i]);}
		}		
		return out;}

	private HashMap<String,String[]> GetMLdico(){
		HashMap<String,String[]> dico=new HashMap<String,String[]>();
		dico.clear();
		try {
			dico=ReadWrite.readDico(Defaults.dicoFile, ";");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return dico;
		
		/*
		// General
		dico.put("Open", new String[] {"Ouvrir","Open"});
		dico.put("Save", new String[] {"Enregistrer","Save"});
		dico.put("SaveAs", new String[] {"Enregistrer sous","Save as"});
		dico.put("New", new String[] {"Nouveau","New"});
		dico.put("Quit", new String[] {"Quitter","Quit"});
		dico.put("File", new String[] {"Fichier","File"});
		dico.put("Browse", new String[] {"Parcourir","Browse"});
		dico.put("Help", new String[] {"Aide","Help"});
		dico.put("About", new String[] {"A propos","About"});
		dico.put("Apply", new String[] {"Appliquer","Apply"});
		dico.put("Cancel", new String[] {"Annuler","Cancel"});
		dico.put("Add", new String[] {"Ajouter","Add"});
		dico.put("Delete", new String[] {"Effacer","Delete"});
		dico.put("DeleteItem", new String[] {"Effacer l'objet s�lectionn�","Delete selected item"});
		dico.put("DeleteAll", new String[] {"Tout effacer","Delete all"});
		dico.put("Rename", new String[] {"Renommer","Rename"});
		dico.put("Export", new String[] {"Exporter","Export"});
		dico.put("ExportValues", new String[] {"Exporter les valeurs","Export values"});
		dico.put("Error", new String[] {"Erreur","Error"});
		dico.put("Message", new String[] {"Message","Message"});
		dico.put("Unknown", new String[] {"Inconnu","Unknown"});
		dico.put("Warning", new String[] {"Attention","Warning"});
		dico.put("Information", new String[] {"Information","Information"});
		dico.put("Options", new String[] {"Options","Options"});
		dico.put("AdvancedOptions", new String[] {"Options avanc�es","Advanced options"});
		dico.put("Language", new String[] {"Langue","Language"});
		dico.put("Name", new String[] {"Nom","Name"});
		dico.put("Description", new String[] {"Description","Description"});
		dico.put("Xplorer", new String[] {"Explorateur","Xplorer"});
		dico.put("Item", new String[] {"Objet","Item"});
		dico.put("TypeName", new String[] {"Merci de taper un nom","Please type a name"});
		dico.put("FormatErrorMessage", new String[] {"Erreur de format - merci de v�rifier votre saisie","Format error - please double-check what you typed"});
		dico.put("ImportErrorMessage", new String[] {"Probl�me lors de l'importation du fichier - merci de v�rifier le format","Problem importing file - please check format"});
		dico.put("Run", new String[] {"Ex�cuter","Run"});
		dico.put("FileNotFound", new String[] {"Fichier non trouv�","File not found"});
		dico.put("ConfigWriteProblem", new String[] {"Probl�me d'�criture d'un fichier de configuration","Problem writing a configuration file"});
		dico.put("RunProblem", new String[] {"Probl�me d'�x�cution","Problem running the executable"});
		dico.put("UnidentifiedProblem", new String[] {"Probl�me non identifi�","Unidentified problem"});
		dico.put("OpenProblem", new String[] {"Probl�me lors de l'ouverture du fichier","Problem opening the file"});
		dico.put("SaveProblem", new String[] {"Probl�me lors de la sauvergarde","Saving problem"});
		dico.put("ExeAborted", new String[] {"L'ex�cutable a plant�","Executable aborted"});
		dico.put("Graph", new String[] {"Graphique","Graph"});
		dico.put("Active", new String[] {"Actif","Active"});
		dico.put("AllDone", new String[] {"Calcul termin� !","All Done!"});
		dico.put("AllDone_CheckPar", new String[] {"Calcul termin� !\nPensez � v�rifier la coh�rence des param�tres estim�s en utilisant le bouton [Autres graphiques]",
				"All Done!\nPlease monitor estimated parameters using [More plots] button"});
		dico.put("Legend", new String[] {"L�gende","Legend"});
		dico.put("ShowLegend", new String[] {"Afficher la l�gende","Show legend"});
		dico.put("Date", new String[] {"Date","Date"});
		dico.put("SelectItem", new String[] {"S�lectionner","Select item"});
		dico.put("SelectItemToDelete", new String[] {"S�lectionner l'objet � effacer","Select item to delete"});
		dico.put("SelectPlot", new String[] {"S�lectionner le graphique","Select plot"});
		dico.put("Yes", new String[] {"Oui","Yes"});
		dico.put("No", new String[] {"Non","No"});
		dico.put("Prior", new String[] {"A priori","Prior"});
		dico.put("Posterior", new String[] {"A posteriori","Posterior"});
		dico.put("Iteration", new String[] {"It�ration","Iteration"});
		dico.put("Compute", new String[] {"Calculer","Compute"});
		dico.put("Preferences", new String[] {"Pr�f�rences","Preferences"});
		dico.put("DefaultDirectory", new String[] {"R�pertoire par d�faut","Default directory"});
		dico.put("SaveOptions", new String[] {"Options de sauvegarde","Save options"});
		dico.put("ChangeSaved", new String[] {"Changement enregistr�","Change saved"});
		dico.put("Duplicate", new String[] {"Dupliquer","Duplicate"});
		dico.put("YYYY", new String[] {"AAAA","YYYY"});
		dico.put("MM", new String[] {"MM","MM"});
		dico.put("DD", new String[] {"JJ","DD"});
		dico.put("hh", new String[] {"hh","hh"});
		dico.put("mm", new String[] {"mm","mm"});
		dico.put("ss", new String[] {"ss","ss"});

		// BaRatinAGE-specific
		dico.put("HydrauConf", new String[] {"Configuration hydraulique","Hydraulic Configuration"}); //{"Contr�les hydrauliques","Hydraulic Controls"}); //
		dico.put("Gaugings", new String[] {"Jaugeages","Gaugings"});
		dico.put("RemnantError", new String[] {"Erreur restante","Remnant Error"});
		dico.put("RatingCurve", new String[] {"Courbe de tarage","Rating Curve"});
		dico.put("RC", new String[] {"CT","RC"});
		dico.put("Limnigraph", new String[] {"Limnigramme","Stage series"});
		dico.put("Hydrograph", new String[] {"Hydrogramme","Flow series"});
		dico.put("Catalogues", new String[] {"Catalogues","Catalogues"});
		dico.put("Segment", new String[] {"Segment","Segment"});
		dico.put("Control", new String[] {"Contr�le","Control"});
		dico.put("Ncontrol", new String[] {"Nombre de contr�les","Number of controls"});
		dico.put("Type", new String[] {"Type","Type"});
		dico.put("apar", new String[] {"a (coefficient)","a (coefficient)"});
		dico.put("cpar", new String[] {"c (exposant)","c (exponent)"});
		dico.put("kpar", new String[] {"k (hauteur d'activation)","k (activation stage)"});
		dico.put("+/-", new String[] {"+/- (incertitude �largie)","+/- (uncertainty)"});
		dico.put("+/-_long", new String[] {"Incertitude �largie : demi-longueur d'un intervalle � 95% = 1.96*�cart-type pour une distribution gaussienne)","Uncertainty expressed as the half-width of a 95% interval = 1.96*standard deviation for a gaussian distribution"});
		dico.put("AddConfig", new String[] {"Ajouter une configuration hydraulique","Add hydraulic configuration"});
		dico.put("AddGauging", new String[] {"Ajouter un jeu de jaugeages","Add gauging dataset"});
		dico.put("AddRemnant", new String[] {"Ajouter un mod�le d'erreur","Add error model"});
		dico.put("AddLimni", new String[] {"Ajouter un limnigramme","Add stage series"});
		dico.put("AddRC", new String[] {"Ajouter une courbe de tarage","Add rating curve"});
		dico.put("AddHydro", new String[] {"Ajouter un hydrogramme","Add flow series"});
		dico.put("NameIsAlreadyUsed", new String[] {"Le nom est d�j� utilis�","Name is already being used"});
		dico.put("Channel", new String[] {"Chenal","Channel"});
		dico.put("RectangularChannel", new String[] {"Chenal rectangulaire","Rectangular channel"});
		dico.put("TriangularChannel", new String[] {"Chenal triangulaire","Triangular channel"});
		dico.put("ParabolicChannel", new String[] {"Chenal parabolique","Parabolic channel"});
		dico.put("RectangularSill", new String[] {"D�versoir rectangulaire","Rectangular weir"});
		dico.put("TriangularSill", new String[] {"D�versoir triangulaire","Triangular weir"});
		dico.put("ParabolicSill", new String[] {"D�versoir parabolique","Parabolic weir"});
		dico.put("Orifice", new String[] {"Orifice","Orifice"});
		dico.put("PriorAssistant", new String[] {"Assistant a priori","Prior assistant"});
		dico.put("OpenPriorAssistant", new String[] {"Ouvrir l'assistant a priori","Open prior assistant"});
		dico.put("PropagateUncertainty", new String[] {"Calcul des param�tres et propagation de l'incertitude","Compute parameters and propagate uncertainty"});
		dico.put("Explanation_frectangle", new String[] {"Coefficient d'ouvrage ~ 0.4","Coefficient ~ 0.4"});
		dico.put("Explanation_ftriangle", new String[] {"Coefficient d'ouvrage ~ 0.31","Coefficient ~ 0.31"});
		dico.put("Explanation_forifice", new String[] {"Coefficient d'ouvrage ~ 0.6","Coefficient ~ 0.6"});
		dico.put("Explanation_fparabola", new String[] {"Coefficient d'ouvrage ~ 0.22","Coefficient ~ 0.22"});
		dico.put("Explanation_Lrectangle", new String[] {"Largeur du d�versoir","Weir width"});
		dico.put("Explanation_Bparabola", new String[] {"Largeur de la parabole au plein bord","Parabola width at bankful stage"});
		dico.put("Explanation_Hparabola", new String[] {"Hauteur de la parabole au plein bord","Parabola height at bankful stage"});
		dico.put("Explanation_Lchannel", new String[] {"Largeur du chenal","Channel width"});
		dico.put("Explanation_Vchannel", new String[] {"Angle d'ouverture du triangle (en degr�s)","Angle (in degrees)"});
		dico.put("Explanation_Bpchannel", new String[] {"Largeur de la parabole au plein bord","Parabola width at bankful stage"});
		dico.put("Explanation_Hpchannel", new String[] {"Hauteur de la parabole au plein bord","Parabola height at bankful stage"});
		dico.put("Explanation_J", new String[] {"Pente du chenal","Channel slope"});
		dico.put("butt_J_text", new String[] {"<html>|z<sub>X</sub>-z<sub>Y</sub>|/d(X,Y)</html>","<html>|z<sub>X</sub>-z<sub>Y</sub>|/d(X,Y)</html>"});
		dico.put("butt_J_info", new String[] {"Sp�cifier la pente � partir de deux points X et Y","Specify slope using two points X and Y"});
		dico.put("Manning", new String[] {"Manning","Manning"});
		dico.put("Strickler", new String[] {"Strickler","Strickler"});
		dico.put("butt_M_info_n", new String[] {"Utiliser le coefficient de Manning","Use Manning coefficient"});
		dico.put("butt_M_info_K", new String[] {"Utiliser le coefficient de Strickler","Use Strickler coefficient"});
		dico.put("Explanation_u", new String[] {"Angle d'ouverture du triangle (en degr�s)","Angle (in degrees)"});
		dico.put("Explanation_S", new String[] {"Surface de l'orifice","Orifice area"});
		dico.put("Explanation_g", new String[] {"Acc�l�ration de gravit� ~ 9.81","Gravity acceleration ~ 9.81"});
		dico.put("Explanation_K", new String[] {"Coefficient de Strickler (1/Manning)","Strickler coefficient (1/Manning)"});
		dico.put("Explanation_Manning", new String[] {"Coefficient de Manning (1/Strickler)","Manning coefficient (1/Strickler)"});
		dico.put("Explanation_crectangle", new String[] {"Exposant ~ 1.5","Exponent ~ 1.5"});
		dico.put("Explanation_ctriangle", new String[] {"Exposant ~ 2.5","Exponent ~ 2.5"});
		dico.put("Explanation_corifice", new String[] {"Exposant ~ 0.5","Exponent ~ 0.5"});
		dico.put("Explanation_cparabola", new String[] {"Exposant ~ 2.0","Exponent ~ 2.0"});
		dico.put("Explanation_cchannel", new String[] {"Exposant ~ 5/3","Exponent ~ 5/3"});
		dico.put("Explanation_k", new String[] {"Hauteur d'activation du contr�le","Control activation stage"});
		dico.put("Explanation_b", new String[] {"offset [d�duit par continuit�, rien � sp�cifier]","offset [deduced by continuity, no specification required]"});
		dico.put("Explanation_altA", new String[] {"Altitude du point X","Elevation of X"});
		dico.put("Explanation_altB", new String[] {"Altitude du point Y","Elevation of Y"});
		dico.put("Explanation_distAB", new String[] {"Distance entre X et Y","Distance between X and Y"});
		dico.put("PriorRC", new String[] {"Courbe de tarage a priori","Prior rating curve"});
		dico.put("PriorRC_env", new String[] {"CT a priori - enveloppe","Prior RC - envelop"});
		dico.put("PriorRC_spag", new String[] {"CT a priori - spaghetti","Prior RC - spaghetti"});
		dico.put("PostRC", new String[] {"Courbe de tarage a posteriori","Posterior rating curve"});
		dico.put("PostRC_env", new String[] {"CT a posteriori - enveloppe","Posterior RC - envelop"});
		dico.put("PostRC_spag", new String[] {"CT a posteriori - spaghetti","Posterior RC - spaghetti"});
		dico.put("KickPlot", new String[] {"Figure ext�rieure","Plot outside"});
		dico.put("PlotInExternalWindow", new String[] {"Tracer la figure dans une fen�tre externe","Plot in external window"});
		dico.put("NMCsim_long", new String[] {"Nombre de simulations Monte Carlo","Number of Monte Carlo simulations"});
		dico.put("Hmin_long", new String[] {"Grille de hauteurs sur laquelle la CT est �valu�e : hauteur minimale","Stage grid on which the RC is evaluated: minimum stage"});
		dico.put("Hmax_long", new String[] {"Grille de hauteurs sur laquelle la CT est �valu�e : hauteur maximale","Stage grid on which the RC is evaluated: maximum stage"});
		dico.put("Hstep_long", new String[] {"Grille de hauteurs sur laquelle la CT est �valu�e : pas de la grille","Stage grid on which the RC is evaluated: grid step"});
		dico.put("Nstep_long", new String[] {"Grille de hauteurs sur laquelle la CT est �valu�e : taille de la grille","Stage grid on which the RC is evaluated: grid size"});
		dico.put("h2n", new String[] {"Calculer la taille de la grille � partir de son pas","Compute grid size from grid step"});
		dico.put("n2h", new String[] {"Calculer le pas de la grille � partir de sa taille","Compute grid step from grid size"});
		dico.put("Ylog_on", new String[] {"Ylog:on","Ylog:on"});
		dico.put("Ylog_off", new String[] {"Ylog:off","Ylog:off"});
		dico.put("ApplyYlog", new String[] {"Appliquer une �chelle logarithmique sur l'axe des ordonn�es","Apply logarithmic scale on y-axis"});
		dico.put("H", new String[] {"H [m]","H [m]"});
		dico.put("uH", new String[] {"uH [m]","uH [m]"});
		dico.put("bH", new String[] {"bH [m]","bH [m]"});
		dico.put("bHindx", new String[] {"bHindx [-]","bHindx [-]"});
		dico.put("uH", new String[] {"uH [m]","uH [m]"});
		dico.put("Q", new String[] {"Q [m3/s]","Q [m3/s]"});
		dico.put("uQ", new String[] {"uQ [%]","uQ [%]"});
		dico.put("Remnant_Linear", new String[] {"Lin�aire : sigma=g1+g2*Q","Linear: sigma=g1+g2*Q"});
		dico.put("Remnant_Constant", new String[] {"Constante : sigma=g1","Constant: sigma=g1"});
		dico.put("MorePlot", new String[] {"Autres graphiques","More plots"});
		dico.put("MorePlotTip", new String[] {"Ouvre une nouvelle fen�tre pour des graphiques avanc�s","Open a new window for more advanced plots"});
		dico.put("TimeInYears", new String[] {"Temps [ann�es]","Time [years]"});
		dico.put("Qunit", new String[] {"Q [m3/s]","Q [m3/s]"});
		dico.put("Hunit", new String[] {"H [m]","H [m]"});
		dico.put("MCMCoptions", new String[] {"Options MCMC","MCMC options"});
		dico.put("RoughnessCoefficient", new String[] {"Coefficient de frottement","Roughness coefficient"});
		dico.put("SaveHydroSpag", new String[] {"Sauvegarder les spaghetti d'hydrogrammes ?","Save hydrograph spaghetti?"});
		dico.put("CloseWarning", new String[] {"Etes-vous s�r de vouloir quitter BaRatinAGE ?","Are you sure you want to close BaRatinAGE?"});
		dico.put("OpenWarning", new String[] {"Attention : cette op�ration �crasera tous les objets existant dans BaRatinAGE\n Voulez-vous continuer ?",
				"Warning: this operation will overwrite all current objects in BaRatinAGE.\n Do you wish to continue?"});
		dico.put("NewWarning", new String[] {"Attention : si vous n'avez pas sauvegard� l'�tude en cours, cette op�ration entrainera une perte de donn�es.\n Voulez-vous continuer ?",
		"Warning: if you didn't save the current study, this operation will result in a loss of data.\n Do you wish to continue?"});
		dico.put("DeleteHydrauWarning", new String[] {"L'effacement de cette configuration hydraulique entra�nera l'effacement des courbes de tarage et des hydrogrammes qui l'utilisent : confirmez-vous l'effacement ?",
		"Deleting this hydraulic configuration will also remove all rating curves and flow series using it: do you wish to proceed?"});
		dico.put("DeleteGaugingsWarning", new String[] {"L'effacement de ce jeu de jaugeages entra�nera l'effacement des courbes de tarage et des hydrogrammes qui l'utilisent : confirmez-vous l'effacement ?",
		"Deleting this gauging set will also remove all rating curves and flow series using it: do you wish to proceed?"});
		dico.put("DeleteLimniWarning", new String[] {"L'effacement de ce limnigramme entra�nera l'effacement des hydrogrammes qui l'utilisent : confirmez-vous l'effacement ?",
		  "Deleting this stage series will also remove all flow series using it: do you wish to proceed?"});
		dico.put("DeleteRCWarning", new String[] {"L'effacement de cette courbe de tarage entra�nera l'effacement des hydrogrammes qui l'utilisent : confirmez-vous l'effacement ?",
		  "Deleting this rating curve will also remove all flow series using it: do you wish to proceed?"});
		dico.put("DeleteHydroWarning", new String[] {"Confirmez-vous l'effacement de cet hydrogramme ?",
		  "Are you sure you want to delete this flow series?"});
		dico.put("BaRatinAGEFiles", new String[] {"Fichiers BaRatinAGE","BaRatinAGE files"});
		dico.put("Density", new String[] {"Densit�","Density"});
		dico.put("PriorVsPost_Par", new String[] {"Comparer les param�tres a priori et a posteriori","Compare prior and posterior parameters"});
		dico.put("PriorVsPost_ParTable", new String[] {"Comparer les param�tres a priori et a posteriori (tableau)","Compare prior and posterior parameters (table)"});
		dico.put("PriorVsPost_RC", new String[] {"Comparer les courbes de tarage a priori et a posteriori","Compare prior and posterior rating curves"});
		dico.put("MCMCtrace", new String[] {"Traces des simulations MCMC","MCMC simulation traces"});
		dico.put("Spaghetti", new String[] {"Spaghetti","Spaghetti"});
		dico.put("PhysicalParameters", new String[] {"<html>Param�tres physiques <hr> </html>","<html>Physical parameters <hr> </html>"});
		dico.put("RatingCurveParameters", new String[] {"<html>Param�tres de la courbe de tarage <hr> </html>","<html>Rating curve parameters <hr> </html>"});
		dico.put("SetLanguageInfo", new String[] {"Le changement sera effectif au prochain d�marrage de BaRatinAGE","Change will apply next time you start BaRatinAGE"});
		dico.put("nAdapt", new String[] {"nAdapt","nAdapt"});
		dico.put("Explanation_nAdapt", new String[] {"Nombre d'it�rations entre deux adaptations","Number of iterations between two adaptions"});
		dico.put("nCycles", new String[] {"nCycles","nCycles"});
		dico.put("Explanation_nCycles", new String[] {"Nombre de cycles d'adaptation","Number of adaption cycles"});
		dico.put("minMR", new String[] {"minTA","minMR"});
		dico.put("Explanation_minMR", new String[] {"Taux d'acceptation minimal","Minimum move rate"});
		dico.put("maxMR", new String[] {"maxTA","maxMR"});
		dico.put("Explanation_maxMR", new String[] {"Taux d'acceptation maximal","Maximum move rate"});
		dico.put("downMult", new String[] {"Facteur-","downMult"});
		dico.put("Explanation_downMult", new String[] {"Facteur pour diminuer la variance de saut","Multiplier to decrease jump variance"});
		dico.put("upMult", new String[] {"Facteur+","upMult"});
		dico.put("Explanation_upMult", new String[] {"Facteur pour augmenter la variance de saut","Multiplier to increase jump variance"});
		dico.put("nSlim", new String[] {"nAffinage","nSlim"});
		dico.put("Explanation_nSlim", new String[] {"Affinage des simulations MCMC (une valeur toutes les nAffinage)","Slim MCMC simulations (one value every nSlim)"});
		dico.put("burn", new String[] {"br�lage","burn"});
		dico.put("Explanation_burn", new String[] {"Br�lage des simulations MCMC (effacement des premi�res [br�lage*nSim] it�rations)","Burn MCMC simulations (delete the initial [burn*nSim] iterations)"});
		dico.put("BaremeInfo", new String[] {"Informations Bar�me","Bar�me information"});
		dico.put("BaremeNmaxInfo", new String[] {"Attention : le format Bar�me limite les courbes de tarage � 100 points.\n Seules les 100 premi�res valeurs seront export�es.","Warning: the Bar�me format restricts rating curves to 100 points.\n Only the 100 first values will be exported."});
		dico.put("Validity_start", new String[] {"D�but de la p�riode de validit�","Start of validity period"});
		dico.put("Validity_end", new String[] {"Fin de la p�riode de validit�","End of validity period"});
		dico.put("Explanation_Name", new String[] {"Nom (6 charact�res max !)","Name (maximum 6 characters!)"});
		dico.put("HydroCode", new String[] {"Code HYDRO","HYDRO code"});
		dico.put("Explanation_HydroCode", new String[] {"Code HYDRO (8 charact�res)","HYDRO code (8 characters)"});

		dico.put("AboutText", new String[] {
				System.getProperty("line.separator")+
				"BaRatinAGE 2.0 - une interface Java pour l'estimation des courbes de tarage."+
				System.getProperty("line.separator")+
				"--------------------------------------------------------------------------------------"+
				System.getProperty("line.separator")+
				System.getProperty("line.separator")+
				"Programmeurs : Sylvain Vigneau, Kevin Mokili, Benjamin Renard, Irstea Lyon-Villeurbanne"+
				System.getProperty("line.separator")+
				"avec l'aide de DMSL, librairie Fortran d�velopp�e par Dmitri Kavetski, University of Adelaide, Australie"
				,
				System.getProperty("line.separator")+
				"BaRatinAGE 2.0 - a Java interface for rating curve estimation"+
				System.getProperty("line.separator")+
				"--------------------------------------------------------------------------------------"+
				System.getProperty("line.separator")+
				System.getProperty("line.separator")+
				"Programmers: Sylvain Vigneau, Kevin Mokili, Benjamin Renard, Irstea Lyon-Villeurbanne"+
				System.getProperty("line.separator")+
				"with the assistance of DMSL, a Fortran numerical library developped by Dmitri Kavetski, University of Adelaide, Australia"});
		dico.put("RemnantErrorText", new String[] {
				System.getProperty("line.separator")+
				"Pour plus d'information sur les mod�les d'erreur restante, merci de consulter l'aide"+
				System.getProperty("line.separator")
				,
				System.getProperty("line.separator")+
				"For more information on remnant error models, please open help"+
				System.getProperty("line.separator")});
				*/
		}

}
