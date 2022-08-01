package moteur;

public class RatingPoints {
	
	private int hauteur;
	private int debit;
	private int qualif;
	
	public RatingPoints (final int h, final int q, final int qual) 
	{
		this.setHauteur(h);
		this.setDebit(q);
		this.setQualif(qual);
	}

	/**
	 * @return the hauteur
	 */
	public int getHauteur() {
		return hauteur;
	}

	/**
	 * @param hauteur the hauteur to set
	 */
	public void setHauteur(int hauteur) {
		this.hauteur = hauteur;
	}

	/**
	 * @return the debit
	 */
	public int getDebit() {
		return debit;
	}

	/**
	 * @param debit the debit to set
	 */
	public void setDebit(int debit) {
		this.debit = debit;
	}

	/**
	 * @return the qualif
	 */
	public int getQualif() {
		return qualif;
	}

	/**
	 * @param qualif the qualif to set
	 */
	public void setQualif(int qualif) {
		this.qualif = qualif;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + debit;
		result = prime * result + hauteur;
		result = prime * result + qualif;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RatingPoints other = (RatingPoints) obj;
		if (debit != other.debit)
			return false;
		if (hauteur != other.hauteur)
			return false;
		if (qualif != other.qualif)
			return false;
		return true;
	}
	
}
