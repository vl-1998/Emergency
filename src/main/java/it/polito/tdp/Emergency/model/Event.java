package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

public class Event implements Comparable<Event>{
	
	public enum EventType {
		ARRIVO, //nuovo paziente arriva
		TRIAGE, //Al paziente è stato assegnato un codice colore e va in sala di attesa
		FREE_STUDIO, //si libera uno studio e chiamo un paziente
		TREATED, //Il paziente è stato trattato e dimesso
		//evento timeout
		TIMEOUT, //attesa eccessiva in sala di aspetto
		TICK, //evento periodico per verificare se ci sono studi vuoti
	}
	
	//l'oggetto evento ha i campi classici degli event
	private LocalTime time;
	private EventType type;
	private Paziente paziente; //qualè il paziente collgato a questo evento
	
	/**
	 * @param time
	 * @param type
	 */
	public Event(LocalTime time, EventType type, Paziente paziente) {
		super();
		this.time = time;
		this.type = type;
		this.paziente=paziente;
	}

	//I SET è meglio non metterli, perchè se cambio il time, rischio di modificare il parametro di un oggetto che lo rende
	//incoerente, perche se i tempi vengono modificati, allora la coda non è più prioritaria perche i tempi non sono veri
	//è meglio avere un oggetto immutabile
	public LocalTime getTime() {
		return time;
	}

	public EventType getType() {
		return type;
	}
	

	public Paziente getPaziente() {
		return paziente;
	}

	@Override
	public int compareTo(Event o) {
		return this.time.compareTo(o.time);
	}

	@Override
	public String toString() {
		return "Event time=" + time + ", type=" + type +" "+ paziente;
	}
	
	
	
	
	
}
