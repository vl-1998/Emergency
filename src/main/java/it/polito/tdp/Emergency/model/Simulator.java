package it.polito.tdp.Emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.Emergency.model.Event.EventType;
import it.polito.tdp.Emergency.model.Paziente.CodiceColore;

public class Simulator {
	//PARAMETRI DI SIMULAZIONE, costanti e dall'esterno
	private int NS = 5; //numero di studi medici
	
	//Valori sensati che permettono di modellare l'andamento della giornata
	private int NP= 150; //numero di pazienti che possiamo impostare dall'esterno
	private Duration T_ARRIVAL = Duration.ofMinutes(5); //intervallo tra i pazienti che posso impostare dall'esterno
	
	//Oggetti final che sono costanti e non più modificabili
	private final Duration DURATION_TRIAGE= Duration.ofMinutes(5);
	private final Duration DURATION_WHITE= Duration.ofMinutes(10);
	private final Duration DURATION_YELLOW= Duration.ofMinutes(15);
	private final Duration DURATION_RED= Duration.ofMinutes(30);
	
	private final Duration TIMEOUT_WHITE= Duration.ofMinutes(90);
	private final Duration TIMEOUT_YELLOW= Duration.ofMinutes(30);
	private final Duration TIMEOUT_RED= Duration.ofMinutes(60);
	
	private final LocalTime oraInizio = LocalTime.of(8, 0);
	private final LocalTime oraFine = LocalTime.of(20, 0);
	
	private final Duration TICK_TIME = Duration.ofMinutes(5); //ogni 5 minuti qualcuno controlla se ci sono studi liberi
		
	//OUTPUT DA CALCOLARE
	private int pazientiTot;
	private int pazientiDimessi;
	private int pazientiAbbandonano;
	private int pazientiMorti;
		
	//STATO DEL SISTEMA
	//pazienti presenti nella mia simulazione, struttura dati che contiene degli oggetti di tipo paziente, tutti 
	//diversi l'uno dall'altro
	private List<Paziente> pazienti;
	//è utile avere una lista apposita in cui ho solo i pazienti in attesa
	private PriorityQueue<Paziente> listaAttesa; //solo quelli dopo il triage prima di essere chiamati, con la PriorityQueue e con un 
										//comparator su paziente, sara la priority queue stessa a dirmi chi deve passare
	private int studiLiberi;
	//ultimo colore assegnato
	private CodiceColore coloreAssegnato;

	//CODA DEGLI EVENTI DA GESTIRE
	private PriorityQueue <Event> queue;
	
	//INIZIALIZZAZIONE
	public void init() {
		//costruisco tutte le strutture dati e azzero tutti i contatori
		this.queue= new PriorityQueue<>();
		this.listaAttesa= new PriorityQueue<>();
		this.pazienti = new ArrayList<>();
		this.pazientiTot=this.pazientiDimessi=this.pazientiMorti=this.pazientiAbbandonano=0;
		
		this.studiLiberi= this.NS;
		
		this.coloreAssegnato=CodiceColore.WHITE;
		
		//genero gli eventi iniziali con i parametri che abbiamo 
		int nPazienti =0;
		LocalTime oraArrivo = this.oraInizio;
		
		while (nPazienti < this.NP && oraArrivo.isBefore(oraFine)) {
			//aggiungo nuovi pazienti
			Paziente p = new Paziente(oraArrivo, CodiceColore.UNKNOWN);
			//il nuovo paziente va aggiunto all'elenco di pazienti
			this.pazienti.add(p);
			
			Event e = new Event (oraArrivo, EventType.ARRIVO, p);
			queue.add(e);
			
			nPazienti++;
			oraArrivo = oraArrivo.plus(T_ARRIVAL);
		}
		//genero il TICK iniziale
		this.queue.add(new Event(this.oraInizio,EventType.TICK,null));
	}
	
	//ESECUZIONE
	public void run() {
		//prende la coda degli eventi e la elabora
		while(!this.queue.isEmpty()) {
			//estraggo l'evento
			Event e = this.queue.poll();
			System.out.println(e);
			processEvent(e);
		}
	}
	
	public void processEvent(Event e) {
		Paziente paz = e.getPaziente();
		switch(e.getType()) {
		case ARRIVO:
			//arriva un paziente che tra 5 minuti avrà finito il triage
			queue.add(new Event(e.getTime().plus(DURATION_TRIAGE), EventType.TRIAGE, paz));
			//conto il nuovo paziente arrivato
			this.pazientiTot++;
			break;
			
			
			
		case TRIAGE:
			//assegnare il codice colore
			paz.setColore(nuovoCodiceColore());
			
			//mette in lista di attesa
			listaAttesa.add(paz); //lui qui andra a valutare il colore
			
			//schedula eventuali timeout
			//aggiungo un nuovo evento con il tempo attuale piu il tempo del timeout che dipendera dal colore
			if (paz.getColore()==CodiceColore.WHITE)
				queue.add(new Event(e.getTime().plus(TIMEOUT_WHITE), EventType.TIMEOUT, paz));
			else if (paz.getColore()==CodiceColore.YELLOW)
				queue.add(new Event(e.getTime().plus(TIMEOUT_YELLOW), EventType.TIMEOUT, paz));
			else if (paz.getColore()==CodiceColore.RED)
				queue.add(new Event(e.getTime().plus(TIMEOUT_RED), EventType.TIMEOUT, paz));
			break;
			
			
			
		case FREE_STUDIO:
			if (this.studiLiberi==0) //non ci sono studi liberi
				break;
			
			//generato quando si libera una posizione in uno degli studi del nostro PS
			Paziente prossimo = listaAttesa.poll();
			if (prossimo != null) {
				//lo faccio entrare 
				this.studiLiberi--;
				//esiste un prossimo, la lista di attesa non è vuota, devo farlo entrare nello studio
				//schedula l'uscita dallo studio
				if (prossimo.getColore()==CodiceColore.WHITE)
					queue.add(new Event(e.getTime().plus(DURATION_WHITE), EventType.TREATED, prossimo));
				else if (prossimo.getColore()==CodiceColore.YELLOW)
					queue.add(new Event(e.getTime().plus(DURATION_YELLOW), EventType.TREATED, prossimo));
				else if (prossimo.getColore()==CodiceColore.RED)
					queue.add(new Event(e.getTime().plus(DURATION_RED), EventType.TREATED, prossimo));
			}
			break;
			
			
			
		case TREATED:
			//libero lo studio
			this.studiLiberi++;
			paz.setColore(CodiceColore.OUT); //il paziente è uscito
			//il paziente esce dallo studio del medico e lo studio diventa libero
			this.pazientiDimessi++;
			//uno studio si è liberato, devo chiamare il prossimo paziente in coda
			this.queue.add(new Event(e.getTime(), EventType.FREE_STUDIO, null)); //non ti dico il paziente perche tanto non c'è
			break;
			
			
			
		case TIMEOUT:
			//quel paziente cambierà colore e se cambia colore devo ridefinire la lista di attesa ed eventualmente
			//rimandarlo a casa. Il timeout dipende dal colore del paziente
			//esci dalla lista di attesa
			listaAttesa.remove(paz);
			if (paz.getColore()==CodiceColore.OUT) //il paziente è già uscito
				break;
			
			switch(paz.getColore()) {
			case WHITE:
				//va a casa
				this.pazientiAbbandonano++;
				break;
			case YELLOW:
				//diventa red
				paz.setColore(CodiceColore.RED);
				listaAttesa.add(paz);//andrà a finire in una posizione diversa e devo impostare il TIMEOUT del rosso
				queue.add(new Event(e.getTime().plus(DURATION_RED), EventType.TIMEOUT, paz));
				break;
			case RED:
				//muore
				this.pazientiMorti++;
				paz.setColore(CodiceColore.OUT);
				break;
			}
			break;
			
			
			
		case TICK:
			if (this.studiLiberi>0) {//c'è almeno uno studio libero, schedula un ingresso di un paziente
				this.queue.add(new Event(e.getTime(), EventType.FREE_STUDIO, null));
			}
			
			if (e.getTime().isBefore(LocalTime.of(23, 30)))
					this.queue.add(new Event(e.getTime().plus(this.TICK_TIME), EventType.TICK, null));

			break;
			
		}
		
	}
	
	//funzione privata del simulatore che ogni volta mi restituisce un nuovo colore
	private CodiceColore nuovoCodiceColore() {
		//si dovra ricordare l'ultimo colore assegnato
		CodiceColore nuovo = coloreAssegnato;
		
		if (coloreAssegnato==CodiceColore.WHITE)
			coloreAssegnato=CodiceColore.YELLOW;
		else if(coloreAssegnato==CodiceColore.YELLOW)
			coloreAssegnato=CodiceColore.RED;
		else
			coloreAssegnato=CodiceColore.WHITE;
		
		return nuovo;
	}

	//permetta all'utente di impostare il numero di studi medici con cui voglio effettuare la simulazione
		public int getNS() {
			return NS;
		}
		public void setNS(int nS) {
			NS = nS;
		}
		
		public int getNP() {
			return NP;
		}
		public void setNP(int nP) {
			NP = nP;
		}
		
		public Duration getT_ARRIVAL() {
			return T_ARRIVAL;
		}
		public void setT_ARRIVAL(Duration t_ARRIVAL) {
			T_ARRIVAL = t_ARRIVAL;
		}

		public int getPazientiTot() {
			return pazientiTot;
		}

		public int getPazientiDimessi() {
			return pazientiDimessi;
		}

		public int getPazientiAbbandonano() {
			return pazientiAbbandonano;
		}

		public int getPazientiMorti() {
			return pazientiMorti;
		}
		
		

}
