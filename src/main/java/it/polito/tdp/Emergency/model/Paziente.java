package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

public class Paziente implements Comparable<Paziente> {
	
	public enum CodiceColore{
		UNKNOWN, //io potrei anche non conoscerlo perche il paziente è appena arrivato e non ha fatto il triage
		WHITE,
		YELLOW,
		RED,
		BLACK,
		OUT,
	}
	
	private LocalTime oraDiArrivo;
	private CodiceColore colore;
	/**
	 * @param oraDiArrivo
	 * @param colore
	 */
	public Paziente(LocalTime oraDiArrivo, CodiceColore colore) {
		super();
		this.oraDiArrivo = oraDiArrivo;
		this.colore = colore;
	}
	public LocalTime getOraDiArrivo() {
		return oraDiArrivo;
	}
	//tolgo il set del time, perchè non posso modificare l'ora di arrivo
	public CodiceColore getColore() {
		return colore;
	}
	public void setColore(CodiceColore colore) {
		this.colore = colore;
	}
	
	//logica di gestione della lista di attesa
	@Override
	public int compareTo(Paziente other) {
		//se hanno lo stesso colore passa prima chi aspetta da più tempo
		//se hanno codice colore diverso passa prima chi ha il codice più grave	
		if (this.colore==other.colore) {
			return this.oraDiArrivo.compareTo(other.oraDiArrivo);
		} else if (this.colore==CodiceColore.RED) {
			return -1; //se il primo è rosso vince questo
		} else if (other.colore==CodiceColore.RED) {
			return +1;
		} else if (this.colore==CodiceColore.YELLOW) {//se nessuno dei due è rosso andiamo a vedere se per caso uno è giallo
			return -1;
		} else if (other.colore==CodiceColore.YELLOW) {
			return +1;
		} 
		//se sono entrambi bianchi ho già controllato
		
		throw new RuntimeException("Comparator<Persona> failed");
	}
	@Override
	public String toString() {
		return "Paziente: oraDiArrivo=" + oraDiArrivo + ", colore=" + colore ;
	}
	
	
	
	

}
