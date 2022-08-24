package commons;

/**
 * object for handling dates
 * @author Sylvain Vigneau & Kevin Mokili & Ben Renard, Irstea Lyon
 */
public class Time {

	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;

	public static final String TIME_DELIMITER = ":";
	public static final String DATE_DELIMITER = "/";

	/**
	 * Full constructor
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public Time(int year, int month, int day, int hour, int minute, int second) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	/**
	 * partial constructor
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public Time(int day, int hour, int minute, int second) {
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	/**
	 * default constructor
	 */
	public Time(){
		this.year = 0;
		this.month = 0;
		this.day = 0;
		this.hour = 0;
		this.minute = 0;
		this.second = 0;
	}

	/**
	 * cosntructor based on a duration expressed in seconds
	 * @param seconds
	 */
	public Time(int seconds){
		//TODO: fix for leap years
		this.second = seconds % 60;
		this.minute = 0 + seconds / 60;
		this.hour = 0 + this.minute / 60;
		this.minute = this.minute % 60;
		this.day = 0 + this.hour / 24;
		this.hour = this.hour % 24;
		this.year = this.day / 365;
		this.day = this.day % 365;
		this.month = this.day / 30;
		this.day = this.day % 30;		
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Time(Time x) {
		if(x==null){return;}
		this.year = Integer.valueOf(x.getYear());
		this.month = Integer.valueOf(x.getMonth());
		this.day = Integer.valueOf(x.getDay());
		this.hour = Integer.valueOf(x.getHour());
		this.minute = Integer.valueOf(x.getMinute());
		this.second = Integer.valueOf(x.getSecond());
	}

	/**
	 * Conversion from date to years
	 * WARNING: not a proper time conversion - all years have duration 365.25 days!
	 * @return the date converted into years
	 */
	public Double toYear(){
		Double out=this.year+
				(this.month-1)/12.D+
				(this.day-1)/365.25D+
				this.hour/(365.25D*24.D)+
				this.minute/(365.25D*24.D*60.D)+
				this.minute/(365.25D*24.D*60.D*60.D);
		return(out);
	}

	public int convertToSeconds(){
		//TODO: fix by adding month/year
		return (this.second + this.minute*60 + this.hour*3600 + this.day*86400);
	}

	public boolean isOlder(Time date){
		return(this.convertToSeconds() - date.convertToSeconds() < 0);
	}

	public Time delai(Time fin){
		if (this.isOlder(fin))
			return new Time(fin.convertToSeconds()-this.convertToSeconds());
		else
			return new Time(this.convertToSeconds()-fin.convertToSeconds());
	}

	public String toString(){
		return (this.toString("YYYY/MM/DD hh:mm:ss"));
	}

	private String TwoChar(Integer i){
		String out=i.toString();
		if(out.length()==1){out="0"+out;}
		return(out);
	}

	private String FourChar(Integer i){
		String out=i.toString();
		if(out.length()==1){out="000"+out;}
		else if(out.length()==2){out="00"+out;}
		else if(out.length()==3){out="0"+out;}
		return(out);
	}

	
	public String toString(String fmt){
		String s = null;
		if(fmt.equalsIgnoreCase("YYYY/MM/DD hh:mm:ss")){
			s=FourChar((Integer)this.year)+
					DATE_DELIMITER+TwoChar((Integer)this.month) +
					DATE_DELIMITER+TwoChar((Integer)this.day) + 
					" " + TwoChar((Integer)this.hour) + 
					TIME_DELIMITER + TwoChar((Integer)this.minute) + 
					TIME_DELIMITER + TwoChar((Integer)this.second);
		}
		else if(fmt.equalsIgnoreCase("YYYYMMDDhhmmss")){
			s=FourChar((Integer)this.year)+
					TwoChar((Integer)this.month) +
					TwoChar((Integer)this.day) + 
					TwoChar((Integer)this.hour) + 
					TwoChar((Integer)this.minute) + 
					TwoChar((Integer)this.second);			
		}
		else if(fmt.equalsIgnoreCase("YYYYMMDD")){
			s=FourChar((Integer)this.year)+
					TwoChar((Integer)this.month) +
					TwoChar((Integer)this.day);			
		}
		else if(fmt.equalsIgnoreCase("YYYY")){
			s=FourChar((Integer)this.year);			
		}
		else if(fmt.equals("MM")){
			s=TwoChar((Integer)this.month);			
		}
		else if(fmt.equalsIgnoreCase("DD")){
			s=TwoChar((Integer)this.day);			
		}
		else if(fmt.equalsIgnoreCase("hhmmss")){
			s=TwoChar((Integer)this.hour) + 
					TwoChar((Integer)this.minute) + 
					TwoChar((Integer)this.second);			
		}
		else if(fmt.equalsIgnoreCase("hh")){
			s=TwoChar((Integer)this.hour);			
		}
		else if(fmt.equals("mm")){
			s=TwoChar((Integer)this.minute);			
		}
		else if(fmt.equalsIgnoreCase("hhmmss")){
			s=TwoChar((Integer)this.second);			
		}
		return s;
	}

	//------------------------------------------------------------------------------------------------------------------------//

	/**
	 * Affecte les champs dans un tableau
	 */
	public int[] toTab() {
		int[] tab = new int[6];
		tab[0] = this.year;
		tab[1] = this.month;
		tab[2] = this.day;
		tab[3] = this.hour;
		tab[4] = this.minute;
		tab[5] = this.second;
		return tab;
	}

	//------------------------------------------------------------------------------------------------------------------------//

	/**
	 * Affiche un certain nombre de champs successifs sous la forme 
	 * valeur1unité1:valeur2unité2:etc à partir du premier champ non nul
	 */
	public String toStringField(int pFieldsNumber) {
		String[] unitsTab = {"a", "mth", "j", "h", "m", "sec"};
		String res = "";
		int[] tab = this.toTab();
		int i = 0, nb = 0;
		while(i < tab.length && tab[i] == 0)
			i++;
		for(int j = i ; j < tab.length && nb < pFieldsNumber ; j++) {
			if(nb != 0)
				res += ":";
			res += tab[j] + unitsTab[j];
			nb++;
		}	
		return res;
	}

	//------------------------------------------------------------------------------------------------------------------------//

	/**
	 * Convertit en jours d�cimaux
	 */
	public double convertToDays() {
		//TODO: fix for leap years
		return this.year * 365 + this.month * 30 + this.day + 
				this.hour/24.0 + this.minute/1440.0 + this.second/86400.0;
	}

	//------------------------------------------------------------------------------------------------------------------------//

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + year;
		result = prime * result + hour;
		result = prime * result + day;
		result = prime * result + minute;
		result = prime * result + month;
		result = prime * result + second;
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
		if (!(obj instanceof Time)) {
			return false;
		}
		Time other = (Time) obj;
		if (year != other.year) {
			return false;
		}
		if (hour != other.hour) {
			return false;
		}
		if (day != other.day) {
			return false;
		}
		if (minute != other.minute) {
			return false;
		}
		if (month != other.month) {
			return false;
		}
		if (second != other.second) {
			return false;
		}
		return true;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

}
